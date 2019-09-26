package action

import (
	"context"
	"github.com/syndesisio/syndesis/install/operator/pkg"

	"k8s.io/client-go/kubernetes"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

// Checks if the syndesis installation should be upgraded and move to the "Upgrading" status.
type checkUpdatesAction struct {
	baseAction
	operatorVersion string
}

func newCheckUpdatesAction(mgr manager.Manager, api kubernetes.Interface) SyndesisOperatorAction {
	return checkUpdatesAction{
		newBaseAction(mgr, api, "check-updates"),
		"",
	}
}

func (a checkUpdatesAction) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		v1alpha1.SyndesisPhaseInstalled,
		v1alpha1.SyndesisPhaseStartupFailed)
}

func (a checkUpdatesAction) Execute(ctx context.Context, syndesis *v1alpha1.Syndesis) error {
	if a.operatorVersion == "" {
		a.operatorVersion = pkg.DefaultOperatorTag
	}

	if syndesis.Status.Version == a.operatorVersion {
		// Everything fine
		return nil
	} else {
		// Let's start the upgrade process
		target := syndesis.DeepCopy()
		target.Status.Phase = v1alpha1.SyndesisPhaseUpgrading
		target.Status.TargetVersion = a.operatorVersion
		target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
		target.Status.Description = "Upgrading from " + syndesis.Status.Version + " to " + a.operatorVersion
		target.Status.LastUpgradeFailure = nil
		target.Status.UpgradeAttempts = 0
		target.Status.ForceUpgrade = false

		a.log.Info("Starting upgrade of Syndesis resource", "name", syndesis.Name, "currentVersion", syndesis.Status.Version, "targetVersion", a.operatorVersion, "type", "checkUpdate")
		return a.client.Update(ctx, target)
	}
}
