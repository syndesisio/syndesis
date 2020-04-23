package action

import (
	"context"

	"github.com/syndesisio/syndesis/install/operator/pkg"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

// Initializes a Syndesis resource with no status and starts the installation process
type initializeAction struct {
	baseAction
}

func newInitializeAction(mgr manager.Manager, clientTools *clienttools.ClientTools) SyndesisOperatorAction {
	return &initializeAction{
		newBaseAction(mgr, clientTools, "initialize"),
	}
}

func (a *initializeAction) CanExecute(syndesis *v1beta1.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		v1beta1.SyndesisPhaseMissing,
		v1beta1.SyndesisPhaseNotInstalled)
}

func (a *initializeAction) Execute(ctx context.Context, syndesis *v1beta1.Syndesis) error {
	list := v1beta1.SyndesisList{}
	rtClient, _ := a.clientTools.RuntimeClient()
	err := rtClient.List(ctx, &list, &client.ListOptions{Namespace: syndesis.Namespace})
	if err != nil {
		return err
	}

	target := syndesis.DeepCopy()

	if len(list.Items) > 1 && syndesis.Status.Phase != v1beta1.SyndesisPhaseInstalled {
		// We want one instance per namespace at most
		target.Status.Phase = v1beta1.SyndesisPhaseNotInstalled
		target.Status.Reason = v1beta1.SyndesisStatusReasonDuplicate
		target.Status.Description = "Cannot install two Syndesis resources in the same namespace"
		a.log.Error(nil, "Cannot initialize Syndesis resource because its a duplicate", "name", syndesis.Name)
	} else {
		syndesisVersion := pkg.DefaultOperatorTag
		target.Status.Phase = v1beta1.SyndesisPhaseInstalling
		target.Status.Reason = v1beta1.SyndesisStatusReasonMissing
		target.Status.Description = ""
		target.Status.Version = syndesisVersion
		a.log.Info("Syndesis resource initialized", "name", syndesis.Name, "version", syndesisVersion)
	}

	return rtClient.Update(ctx, target)
}
