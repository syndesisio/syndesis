package action

import (
	"context"
	"time"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/olm"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/upgrade"

	"github.com/syndesisio/syndesis/install/operator/pkg"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"

	synapi "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta3"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

var u upgrade.Upgrader

const (
	UpgradePodPrefix = "syndesis-upgrade-"
)

// Upgrades Syndesis to the version supported by this operator using the upgrade template.
type upgradeAction struct {
	baseAction
	operatorVersion string
}

func newUpgradeAction(mgr manager.Manager, clientTools *clienttools.ClientTools) SyndesisOperatorAction {
	return &upgradeAction{
		newBaseAction(mgr, clientTools, "upgrade"),
		"",
	}
}

func (a *upgradeAction) CanExecute(syndesis *synapi.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		synapi.SyndesisPhaseUpgrading,
		synapi.SyndesisPhasePostUpgradeRunSucceed,
		synapi.SyndesisPhasePostUpgradeRun,
	)
}

func (a *upgradeAction) Execute(ctx context.Context, syndesis *synapi.Syndesis, operatorNamespace string, productName string) error {
	targetVersion := pkg.DefaultOperatorTag

	if syndesis.Status.Phase == synapi.SyndesisPhaseUpgrading {
		// Normal upgrade workflow, we land here if action checkupdate detected that and
		// upgrade is needed

		// Initialize the upgrade object, this happens only once, this object lives throughout
		// the whole upgrade / rollback process
		if u == nil {
			var err error
			u, err = upgrade.Build(ctx, a.log, syndesis, a.clientTools)
			if err != nil {
				return err
			}
		}

		a.log.Info("Upgrading syndesis resource ", "name", syndesis.Name, "current version", syndesis.Status.Version, "target version", targetVersion)
		err := u.Upgrade()
		if err == nil {
			// If upgrade finished correctly, we go to the post upgrade run, meaning we want to do an install
			// run and make sure it succeed
			a.log.Info("Syndesis resource upgraded", "name", syndesis.Name, "target version", targetVersion)
			return a.setPhaseToRun(ctx, syndesis)
		} else {
			a.log.Error(err, "Failure while upgrading Syndesis", "name", syndesis.Name, "target version", targetVersion)
			if err := u.Rollback(); err != nil {
				a.log.Error(err, "Failure while rolling back Syndesis, some manual steps might be required", "name", syndesis.Name, "target version", targetVersion)
			}
			return a.setPhaseToFailureBackoff(ctx, syndesis, targetVersion)
		}
	} else if syndesis.Status.Phase == synapi.SyndesisPhasePostUpgradeRunSucceed {
		// We land here only if the install phase after upgrading finished correctly
		a.log.Info("syndesis resource post upgrade ran successfully", "name", syndesis.Name, "previous version", syndesis.Status.Version, "target version", targetVersion)
		return a.completeUpgrade(ctx, syndesis, targetVersion, operatorNamespace, productName)
	} else if syndesis.Status.Phase == synapi.SyndesisPhasePostUpgradeRun {
		// If the first run of the install action failed, we land here. We need to retry
		// this few times to consider the cases where install action return error due to
		// race conditions or when the syndesis custom resource was changed in the meantime. 3 times seems
		// to be enough
		a.log.Info("failure while running post upgrade run", "name", syndesis.Name, "target version", targetVersion)
		if u.InstallFailed() < 4 {
			a.log.Info("attempting again to run post upgrade", "name", syndesis.Name)
		} else {
			a.log.Info("syndesis first run after upgrade failed repeatedly, attempting to rollback now")
			if err := u.Rollback(); err != nil {
				a.log.Error(err, "failure while rolling back Syndesis, some manual steps might be required", "name", syndesis.Name, "target version", targetVersion)
			} else {
				a.log.Info("syndesis successfully rolled back", "name", syndesis.Name)
			}
			return a.setPhaseToFailureBackoff(ctx, syndesis, targetVersion)
		}
	}

	return nil
}

/*
 * Following functions have a sleep after updating the custom resource. This is
 * needed to avoid race conditions where k8s wasn't yet able to update or
 * kubernetes didn't change the object yet
 */
func (a *upgradeAction) completeUpgrade(ctx context.Context, syndesis *synapi.Syndesis, newVersion string, operatorNamespace string, productName string) (err error) {
	// Declare the operator upgradeable, if applicable
	state := olm.ConditionState{
		Status:  metav1.ConditionTrue,
		Reason:  "CompletedUpgrade",
		Message: "Operator component state has been upgraded",
	}
	err = olm.SetUpgradeCondition(ctx, a.clientTools, operatorNamespace, productName, state)
	if err != nil {
		a.log.Error(err, "Failed to set the upgrade condition on the operator")
	}

	target := syndesis.DeepCopy()
	target.Status.Phase = synapi.SyndesisPhaseInstalled
	target.Status.TargetVersion = ""
	target.Status.Reason = synapi.SyndesisStatusReasonMissing
	target.Status.Description = ""
	target.Status.Version = newVersion
	target.Status.LastUpgradeFailure = nil
	target.Status.UpgradeAttempts = 0
	target.Status.ForceUpgrade = false

	rtClient, _ := a.clientTools.RuntimeClient()
	err = rtClient.Status().Update(ctx, target)
	time.Sleep(3 * time.Second)
	return
}

func (a *upgradeAction) setPhaseToRun(ctx context.Context, syndesis *synapi.Syndesis) (err error) {
	target := syndesis.DeepCopy()
	target.Status.Phase = synapi.SyndesisPhasePostUpgradeRun
	target.Status.Reason = synapi.SyndesisStatusReasonPostUpgradeRun
	target.Status.Description = "Perform the first install run after syndesis resource was upgraded"

	rtClient, _ := a.clientTools.RuntimeClient()
	err = rtClient.Status().Update(ctx, target)
	time.Sleep(3 * time.Second)
	return
}

func (a *upgradeAction) setPhaseToFailureBackoff(ctx context.Context, syndesis *synapi.Syndesis, targetVersion string) (err error) {
	target := syndesis.DeepCopy()
	target.Status.Phase = synapi.SyndesisPhaseUpgradeFailureBackoff
	target.Status.Reason = synapi.SyndesisStatusReasonUpgradeFailed
	target.Status.Description = "Syndesis upgrade from " + syndesis.Status.Version + " to " + targetVersion + " failed (it will be retried again)"
	target.Status.LastUpgradeFailure = &metav1.Time{
		Time: time.Now(),
	}
	target.Status.UpgradeAttempts = target.Status.UpgradeAttempts + 1

	rtClient, _ := a.clientTools.RuntimeClient()
	err = rtClient.Status().Update(ctx, target)
	time.Sleep(3 * time.Second)
	return
}
