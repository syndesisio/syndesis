package action

import (
	"context"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"

	"sigs.k8s.io/controller-runtime/pkg/client"

	"k8s.io/client-go/kubernetes"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

// Initializes a Syndesis resource with no status and starts the installation process
type initializeAction struct {
	baseAction
}

func newInitializeAction(mgr manager.Manager, api kubernetes.Interface) SyndesisOperatorAction {
	return &initializeAction{
		newBaseAction(mgr, api, "initialize"),
	}
}

func (a *initializeAction) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		v1alpha1.SyndesisPhaseMissing,
		v1alpha1.SyndesisPhaseNotInstalled)
}

func (a *initializeAction) Execute(ctx context.Context, syndesis *v1alpha1.Syndesis) error {

	list := v1alpha1.SyndesisList{}
	err := a.client.List(ctx, &client.ListOptions{Namespace: syndesis.Namespace}, &list)
	if err != nil {
		return err
	}

	target := syndesis.DeepCopy()

	if len(list.Items) > 1 {
		// We want one instance per namespace at most
		target.Status.Phase = v1alpha1.SyndesisPhaseNotInstalled
		target.Status.Reason = v1alpha1.SyndesisStatusReasonDuplicate
		target.Status.Description = "Cannot install two Syndesis resources in the same namespace"
		a.log.Error(nil, "Cannot initialize Syndesis resource because its a duplicate", "name", syndesis.Name)
	} else {
		syndesisVersion, err := template.GetSyndesisVersionFromOperatorTemplate(a.scheme)
		if err != nil {
			return err
		}

		target.Status.Phase = v1alpha1.SyndesisPhaseInstalling
		target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
		target.Status.Description = ""
		target.Status.Version = syndesisVersion
		a.log.Info("Syndesis resource initialized", "name", syndesis.Name, "version", syndesisVersion)
	}

	return a.client.Update(ctx, target)
}
