package action

import (
	"context"
	"time"

	"github.com/syndesisio/syndesis/install/operator/pkg"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
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

func (a checkUpdatesAction) CanExecute(syndesis *v1beta1.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		v1beta1.SyndesisPhaseInstalled,
		v1beta1.SyndesisPhaseStartupFailed)
}

func (a checkUpdatesAction) Execute(ctx context.Context, syndesis *v1beta1.Syndesis) error {
	if a.operatorVersion == "" {
		a.operatorVersion = pkg.DefaultOperatorTag
	}

	if syndesis.Status.Version == a.operatorVersion {
		// Everything fine
		return nil
	} else {
		return a.setPhaseToUpgrading(ctx, syndesis)
	}
}

/*
 * Following functions have a sleep after updating the custom resource. This is
 * needed to avoid race conditions where k8s wasn't able to update or
 * kubernetes didn't change the object yet
 */
func (a checkUpdatesAction) setPhaseToUpgrading(ctx context.Context, syndesis *v1beta1.Syndesis) (err error) {
	target := syndesis.DeepCopy()
	target.Status.Phase = v1beta1.SyndesisPhaseUpgrading
	target.Status.TargetVersion = a.operatorVersion
	target.Status.Reason = v1beta1.SyndesisStatusReasonMissing
	target.Status.Description = "Upgrading from " + syndesis.Status.Version + " to " + a.operatorVersion
	target.Status.LastUpgradeFailure = nil
	target.Status.UpgradeAttempts = 0
	target.Status.ForceUpgrade = false

	client, _ := a.clientTools.RuntimeClient()
	err = client.Update(ctx, target)
	time.Sleep(3 * time.Second)
	return
}
