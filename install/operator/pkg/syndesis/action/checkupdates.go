package action

import (
	"github.com/operator-framework/operator-sdk/pkg/sdk"
	"github.com/sirupsen/logrus"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
)

// Checks if the syndesis installation should be upgraded and move to the "Upgrading" status.
type CheckUpdates struct {
	operatorVersion	string
}

func (a *CheckUpdates) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		v1alpha1.SyndesisPhaseInstalled,
		v1alpha1.SyndesisPhaseStartupFailed)
}

func (a *CheckUpdates) Execute(syndesis *v1alpha1.Syndesis) error {

	if a.operatorVersion == "" {
		operatorVersion, err := configuration.GetSyndesisVersionFromOperatorTemplate()
		if err != nil {
			return err
		}
		a.operatorVersion = operatorVersion
	}

	namespaceVersion, err := configuration.GetSyndesisVersionFromNamespace(syndesis.Namespace)
	if err != nil {
		return err
	}

	if namespaceVersion == a.operatorVersion {
		// Everything fine
		return nil
	} else {
		// Let's start the upgrade process
		target := syndesis.DeepCopy()
		target.Status.Phase = v1alpha1.SyndesisPhaseUpgrading
		target.Status.TargetVersion = a.operatorVersion
		target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
		target.Status.Description = "Upgrading from " + namespaceVersion + " to " + a.operatorVersion
		target.Status.LastUpgradeFailure = nil
		target.Status.UpgradeAttempts = 0
		target.Status.ForceUpgrade = false

		logrus.Info("Starting upgrade of Syndesis resource ", syndesis.Name, " from version ", namespaceVersion, " to version ", a.operatorVersion)
		return sdk.Update(target)
	}
}
