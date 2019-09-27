package action

import (
	"context"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"

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
		operatorVersion, err := template.GetSyndesisVersionFromOperatorTemplate(a.scheme)
		if err != nil {
			return err
		}
		a.operatorVersion = operatorVersion
	}

	if syndesis.Status.Version == a.operatorVersion {
		// Everything fine
		return nil
	} else {
		// Let's start the upgrade process
		syndesis.Status.Phase = v1alpha1.SyndesisPhaseUpgrading
		syndesis.Status.TargetVersion = a.operatorVersion
		syndesis.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
		syndesis.Status.Description = "Upgrading from " + syndesis.Status.Version + " to " + a.operatorVersion
		syndesis.Status.LastUpgradeFailure = nil
		syndesis.Status.UpgradeAttempts = 0
		syndesis.Status.ForceUpgrade = false

		a.log.Info("Starting upgrade of Syndesis resource", "name", syndesis.Name, "currentVersion", syndesis.Status.Version, "targetVersion", a.operatorVersion, "type", "checkUpdate")
		return a.client.Update(ctx, syndesis)
	}
}
