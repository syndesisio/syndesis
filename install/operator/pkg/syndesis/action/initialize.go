package action

import (
	"context"

	"github.com/syndesisio/syndesis/install/operator/pkg"

	synapi "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta3"
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

func (a *initializeAction) CanExecute(syndesis *synapi.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		synapi.SyndesisPhaseMissing,
		synapi.SyndesisPhaseNotInstalled)
}

func (a *initializeAction) Execute(ctx context.Context, syndesis *synapi.Syndesis, operatorNamespace string) error {
	list := synapi.SyndesisList{}
	rtClient, _ := a.clientTools.RuntimeClient()
	err := rtClient.List(ctx, &list, &client.ListOptions{Namespace: syndesis.Namespace})
	if err != nil {
		return err
	}

	target := syndesis.DeepCopy()

	if len(list.Items) > 1 && syndesis.Status.Phase != synapi.SyndesisPhaseInstalled {
		// We want one instance per namespace at most
		target.Status.Phase = synapi.SyndesisPhaseNotInstalled
		target.Status.Reason = synapi.SyndesisStatusReasonDuplicate
		target.Status.Description = "Cannot install two Syndesis resources in the same namespace"
		a.log.Error(nil, "Cannot initialize Syndesis resource because its a duplicate", "name", syndesis.Name)
	} else {
		syndesisVersion := pkg.DefaultOperatorTag
		target.Status.Phase = synapi.SyndesisPhaseInstalling
		target.Status.Reason = synapi.SyndesisStatusReasonMissing
		target.Status.Description = ""
		target.Status.Version = syndesisVersion
		a.log.Info("Syndesis resource initialized", "name", syndesis.Name, "version", syndesisVersion)
	}

	return rtClient.Status().Update(ctx, target)
}
