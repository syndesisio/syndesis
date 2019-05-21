package action

import (
	"context"
	"errors"

	"sigs.k8s.io/controller-runtime/pkg/client"

	"k8s.io/client-go/kubernetes"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/operation"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

// Upgrade a legacy Syndesis installation (installed with template) using the operator.
type upgradeLegacyAction struct {
	baseAction
}

func newUpgradeLegacyAction(mgr manager.Manager, api kubernetes.Interface) SyndesisOperatorAction {
	return &upgradeLegacyAction{
		newBaseAction(mgr, api, "upgrade-legacy"),
	}
}

func (a *upgradeLegacyAction) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis, v1alpha1.SyndesisPhaseUpgradingLegacy)
}

func (a *upgradeLegacyAction) Execute(ctx context.Context, syndesis *v1alpha1.Syndesis) error {
	// Checking that there's only one installation to avoid stealing resources
	if anotherInstallation, err := a.isAnotherActiveInstallationPresent(ctx, syndesis); err != nil {
		return err
	} else if anotherInstallation {
		return errors.New("another syndesis installation active")
	}

	a.log.Info("Attaching Syndesis installation to resource", "name", syndesis.Name)

	err := operation.AttachSyndesisToResource(ctx, a.scheme, a.client, syndesis)
	if err != nil {
		return err
	}

	syndesisVersion, err := configuration.GetSyndesisVersionFromNamespace(ctx, a.client, syndesis.Namespace)
	if err != nil {
		return err
	}

	target := syndesis.DeepCopy()
	target.Status.Phase = v1alpha1.SyndesisPhaseStarting
	target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
	target.Status.Description = ""
	target.Status.Version = syndesisVersion

	a.log.Info("Syndesis installation attached to resource", "name", syndesis.Name)
	return a.client.Update(ctx, target)
}

func (a *upgradeLegacyAction) isAnotherActiveInstallationPresent(ctx context.Context, syndesis *v1alpha1.Syndesis) (bool, error) {
	lst := v1alpha1.SyndesisList{}
	err := a.client.List(ctx, &client.ListOptions{Namespace: syndesis.Namespace}, &lst)
	if err != nil {
		return false, err
	}

	for _, that := range lst.Items {
		if that.Name != syndesis.Name &&
			that.Status.Phase != v1alpha1.SyndesisPhaseNotInstalled &&
			that.Status.Phase != v1alpha1.SyndesisPhaseMissing {
			return true, nil
		}
	}

	return false, nil
}
