package action

import (
	"context"
	"time"

	"github.com/syndesisio/syndesis/install/operator/pkg"

	synapi "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta3"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/olm"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

// Checks if the syndesis installation should be upgraded and move to the "Upgrading" status.
type checkUpdatesAction struct {
	baseAction
	operatorVersion string
}

func newCheckUpdatesAction(mgr manager.Manager, clientTools *clienttools.ClientTools) SyndesisOperatorAction {
	return checkUpdatesAction{
		newBaseAction(mgr, clientTools, "check-updates"),
		"",
	}
}

func (a checkUpdatesAction) CanExecute(syndesis *synapi.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		synapi.SyndesisPhaseInstalled,
		synapi.SyndesisPhaseStartupFailed)
}

func (a checkUpdatesAction) Execute(ctx context.Context, syndesis *synapi.Syndesis, operatorNamespace string) error {
	if a.operatorVersion == "" {
		a.operatorVersion = pkg.DefaultOperatorTag
	}

	if syndesis.Status.Version == a.operatorVersion {
		// Everything fine
		return nil
	} else {
		return a.setPhaseToUpgrading(ctx, syndesis, operatorNamespace)
	}
}

/*
 * Following functions have a sleep after updating the custom resource. This is
 * needed to avoid race conditions where k8s wasn't able to update or
 * kubernetes didn't change the object yet
 */
func (a checkUpdatesAction) setPhaseToUpgrading(ctx context.Context, syndesis *synapi.Syndesis, operatorNamespace string) (err error) {

	// Declare an upgradeable Condition as false if applicable
	state := olm.ConditionState{
		Status:  metav1.ConditionFalse,
		Reason:  "Upgrading",
		Message: "Operator is upgrading the components",
	}
	err = olm.SetUpgradeCondition(ctx, a.clientTools, operatorNamespace, state)
	if err != nil {
		a.log.Error(err, "Failed to set the upgrade condition on the operator")
	}

	target := syndesis.DeepCopy()
	target.Status.Phase = synapi.SyndesisPhaseUpgrading
	target.Status.TargetVersion = a.operatorVersion
	target.Status.Reason = synapi.SyndesisStatusReasonMissing
	target.Status.Description = "Upgrading from " + syndesis.Status.Version + " to " + a.operatorVersion
	target.Status.LastUpgradeFailure = nil
	target.Status.UpgradeAttempts = 0
	target.Status.ForceUpgrade = false

	client, _ := a.clientTools.RuntimeClient()
	err = client.Status().Update(ctx, target)
	time.Sleep(3 * time.Second)
	return
}
