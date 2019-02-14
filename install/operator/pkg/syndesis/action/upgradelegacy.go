package action

import (
	"context"
	"errors"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/operation"
	runtime2 "k8s.io/apimachinery/pkg/runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

// Upgrade a legacy Syndesis installation (installed with template) using the operator.
type upgradeLegacy action


var (
	UpgradeLegacy =  upgradeLegacy{
		actionLog.WithValues("type", "upgrade-legacy"),
	}
)

func (a *upgradeLegacy) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis, v1alpha1.SyndesisPhaseUpgradingLegacy)
}

func (a *upgradeLegacy) Execute(scheme *runtime2.Scheme, cl client.Client, syndesis *v1alpha1.Syndesis) error {
	// Checking that there's only one installation to avoid stealing resources
	if anotherInstallation, err := isAnotherActiveInstallationPresent(cl, syndesis); err != nil {
		return err
	} else if anotherInstallation {
		return errors.New("another syndesis installation active")
	}

	a.log.Info("Attaching Syndesis installation to resource","name", syndesis.Name)

	err := operation.AttachSyndesisToResource(scheme, cl, syndesis)
	if err != nil {
		return err
	}

	syndesisVersion, err := configuration.GetSyndesisVersionFromNamespace(cl, syndesis.Namespace)
	if err != nil {
		return err
	}

	target := syndesis.DeepCopy()
	target.Status.Phase = v1alpha1.SyndesisPhaseStarting
	target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
	target.Status.Description = ""
	target.Status.Version = syndesisVersion

	a.log.Info("Syndesis installation attached to resource", "name", syndesis.Name)
	return cl.Update(context.TODO(), target)
}

func isAnotherActiveInstallationPresent(cl client.Client, syndesis *v1alpha1.Syndesis) (bool, error) {
	lst := v1alpha1.SyndesisList{}
	err := cl.List(context.TODO(), &client.ListOptions{Namespace: syndesis.Namespace}, &lst)
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
