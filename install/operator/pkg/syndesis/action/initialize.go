package action

import (
	"context"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

// Initializes a Syndesis resource with no status and starts the installation process
type Initialize struct{}

func (a *Initialize) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		v1alpha1.SyndesisPhaseMissing,
		v1alpha1.SyndesisPhaseNotInstalled)
}

func (a *Initialize) Execute(cl client.Client, syndesis *v1alpha1.Syndesis) error {

	list := v1alpha1.SyndesisList{}
	err := cl.List(context.TODO(), &client.ListOptions{Namespace: syndesis.Namespace}, &list)
	if err != nil {
		return err
	}

	target := syndesis.DeepCopy()

	if len(list.Items) > 1 {
		// We want one instance per namespace at most
		target.Status.Phase = v1alpha1.SyndesisPhaseNotInstalled
		target.Status.Reason = v1alpha1.SyndesisStatusReasonDuplicate
		target.Status.Description = "Cannot install two Syndesis resources in the same namespace"
		log.Error(nil,"Cannot initialize Syndesis resource because its a duplicate","name", syndesis.Name, "type", "initialize")
	} else {
		syndesisVersion, err := configuration.GetSyndesisVersionFromOperatorTemplate()
		if err != nil {
			return err
		}

		target.Status.Phase = v1alpha1.SyndesisPhaseInstalling
		target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
		target.Status.Description = ""
		target.Status.Version = syndesisVersion
		log.Info("Syndesis resource initialized: installing version ", "name", syndesis.Name, "version", syndesisVersion, "type", "initialize")
	}

	return cl.Update(context.TODO(), target)
}
