package action

import (
	"context"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	runtime2 "k8s.io/apimachinery/pkg/runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

// Initializes a Syndesis resource with no status and starts the installation process
type initialize action

var (
	InitializeAction = initialize{actionLog.WithValues("type", "initialize")}
)

func (a *initialize) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		v1alpha1.SyndesisPhaseMissing,
		v1alpha1.SyndesisPhaseNotInstalled)
}

func (a *initialize) Execute(scheme *runtime2.Scheme, cl client.Client, syndesis *v1alpha1.Syndesis) error {

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
		a.log.Error(nil,"Cannot initialize Syndesis resource because its a duplicate","name", syndesis.Name)
	} else {
		syndesisVersion, err := configuration.GetSyndesisVersionFromOperatorTemplate(scheme)
		if err != nil {
			return err
		}

		target.Status.Phase = v1alpha1.SyndesisPhaseInstalling
		target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
		target.Status.Description = ""
		target.Status.Version = syndesisVersion
		a.log.Info("Syndesis resource initialized: installing version ", "name", syndesis.Name, "version", syndesisVersion)
	}

	return cl.Status().Update(context.TODO(), target)
}
