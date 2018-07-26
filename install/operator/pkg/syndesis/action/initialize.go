package action

import (
	"github.com/operator-framework/operator-sdk/pkg/sdk"
	"github.com/sirupsen/logrus"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
)

// Initializes a Syndesis resource with no status and starts the installation process
type Initialize struct {}


func (a *Initialize) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		v1alpha1.SyndesisPhaseMissing,
		v1alpha1.SyndesisPhaseNotInstalled)
}

func (a *Initialize) Execute(syndesis *v1alpha1.Syndesis) error {

	list := v1alpha1.NewSyndesisList()
	err := sdk.List(syndesis.Namespace, list)
	if err != nil {
		return err
	}

	target := syndesis.DeepCopy()

	if len(list.Items) > 1 {
		// We want one instance per namespace at most
		target.Status.Phase = v1alpha1.SyndesisPhaseNotInstalled
		target.Status.Reason = v1alpha1.SyndesisStatusReasonDuplicate
		target.Status.Description = "Cannot install two Syndesis resources in the same namespace"
		logrus.Error("Cannot initialize Syndesis resource ", syndesis.Name, ": duplicate")
	} else {
		syndesisVersion, err := configuration.GetSyndesisVersionFromOperatorTemplate()
		if err != nil {
			return err
		}

		target.Status.Phase = v1alpha1.SyndesisPhaseInstalling
		target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
		target.Status.Description = ""
		target.Status.Version = syndesisVersion
		logrus.Info("Syndesis resource ", syndesis.Name, " initialized: installing version ", syndesisVersion)
	}

	return sdk.Update(target)
}
