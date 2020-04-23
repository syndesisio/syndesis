package action

import (
	"context"
	"time"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/upgrade"

	"github.com/syndesisio/syndesis/install/operator/pkg"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
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

func (a *upgradeAction) CanExecute(syndesis *v1beta1.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		v1beta1.SyndesisPhaseUpgrading,
		v1beta1.SyndesisPhasePostUpgradeRunSucceed,
		v1beta1.SyndesisPhasePostUpgradeRun,
	)
}

func (a *upgradeAction) Execute(ctx context.Context, syndesis *v1beta1.Syndesis) error {
	targetVersion := pkg.DefaultOperatorTag

	if syndesis.Status.Phase == v1beta1.SyndesisPhaseUpgrading {
		// Normal upgrade workflow, we land here if action checkupdate detected that and
		// upgrade is needed

		// Initialize the upgrade object, this happens only once, this object lives throughout
		// the whole upgrade / rollback process
		if u == nil {
			var err error
			u, err = upgrade.Build(a.log, syndesis, a.clientTools, ctx)
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
			a.log.Error(nil, "Failure while upgrading Syndesis", "name", syndesis.Name, "target version", targetVersion)
			if err := u.Rollback(); err != nil {
				a.log.Error(nil, "Failure while rolling back Syndesis, some manual steps might be required", "name", syndesis.Name, "target version", targetVersion)
			}

			return a.setPhaseToFailureBackoff(ctx, syndesis, targetVersion)
		}
	} else if syndesis.Status.Phase == v1beta1.SyndesisPhasePostUpgradeRunSucceed {
		// We land here only if the install phase after upgrading finished correctly
		a.log.Info("syndesis resource post upgrade ran successfully", "name", syndesis.Name, "previous version", syndesis.Status.Version, "target version", targetVersion)
		return a.completeUpgrade(ctx, syndesis, targetVersion)
	} else if syndesis.Status.Phase == v1beta1.SyndesisPhasePostUpgradeRun {
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
				a.log.Error(nil, "failure while rolling back Syndesis, some manual steps might be required", "name", syndesis.Name, "target version", targetVersion)
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
func (a *upgradeAction) completeUpgrade(ctx context.Context, syndesis *v1beta1.Syndesis, newVersion string) (err error) {
	target := syndesis.DeepCopy()
	target.Status.Phase = v1beta1.SyndesisPhaseInstalled
	target.Status.TargetVersion = ""
	target.Status.Reason = v1beta1.SyndesisStatusReasonMissing
	target.Status.Description = ""
	target.Status.Version = newVersion
	target.Status.LastUpgradeFailure = nil
	target.Status.UpgradeAttempts = 0
	target.Status.ForceUpgrade = false

	rtClient, _ := a.clientTools.RuntimeClient()
	err = rtClient.Update(ctx, target)
	time.Sleep(3 * time.Second)
	return
}

func (a *upgradeAction) setPhaseToRun(ctx context.Context, syndesis *v1beta1.Syndesis) (err error) {
	target := syndesis.DeepCopy()
	target.Status.Phase = v1beta1.SyndesisPhasePostUpgradeRun
	target.Status.Reason = v1beta1.SyndesisStatusReasonPostUpgradeRun
	target.Status.Description = "Perform the first install run after syndesis resource was upgraded"

	rtClient, _ := a.clientTools.RuntimeClient()
	err = rtClient.Update(ctx, target)
	time.Sleep(3 * time.Second)
	return
}

func (a *upgradeAction) setPhaseToFailureBackoff(ctx context.Context, syndesis *v1beta1.Syndesis, targetVersion string) (err error) {
	target := syndesis.DeepCopy()
	target.Status.Phase = v1beta1.SyndesisPhaseUpgradeFailureBackoff
	target.Status.Reason = v1beta1.SyndesisStatusReasonUpgradeFailed
	target.Status.Description = "Syndesis upgrade from " + syndesis.Status.Version + " to " + targetVersion + " failed (it will be retried again)"
	target.Status.LastUpgradeFailure = &metav1.Time{
		Time: time.Now(),
	}
	target.Status.UpgradeAttempts = target.Status.UpgradeAttempts + 1

	rtClient, _ := a.clientTools.RuntimeClient()
	err = rtClient.Update(ctx, target)
	time.Sleep(3 * time.Second)
	return
}
