package action

import (
	"context"
	"errors"
	"strconv"
	"strings"
	"time"

	"k8s.io/api/core/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/client-go/kubernetes"
	"sigs.k8s.io/controller-runtime/pkg/client"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/addons"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/operation"
	syndesistemplate "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

const (
	UpgradePodPrefix = "syndesis-upgrade-"
)

// Upgrades Syndesis to the version supported by this operator using the upgrade template.
type upgradeAction struct {
	baseAction
	operatorVersion string
}

func newUpgradeAction(mgr manager.Manager, api kubernetes.Interface) SyndesisOperatorAction {
	return &upgradeAction{
		newBaseAction(mgr, api, "upgrade"),
		"",
	}
}

func (a *upgradeAction) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis, v1alpha1.SyndesisPhaseUpgrading)
}

func (a *upgradeAction) Execute(ctx context.Context, syndesis *v1alpha1.Syndesis) error {
	if a.operatorVersion == "" {
		operatorVersion, err := configuration.GetSyndesisVersionFromOperatorTemplate(a.scheme)
		if err != nil {
			return err
		}
		a.operatorVersion = operatorVersion
	}

	// Delete previously installed addons resources
	addonResources, err := a.getAddonsResources(ctx, syndesis)
	if err != nil {
		return err
	}
	for _, addonResource := range addonResources {
		err := a.client.Delete(ctx, &addonResource)
		if err != nil {
			return err
		}
	}

	namespaceVersion, err := configuration.GetSyndesisVersionFromNamespace(ctx, a.client, syndesis.Namespace)
	if err != nil {
		return err
	}
	targetVersion := a.operatorVersion

	resources, err := a.getUpgradeResources(a.scheme, syndesis)
	if err != nil {
		return err
	}

	templateUpgradePod, err := a.findUpgradePod(resources)
	if err != nil {
		return err
	}

	upgradePod, err := a.getUpgradePodFromNamespace(ctx, templateUpgradePod, syndesis)
	if err != nil && !k8serrors.IsNotFound(err) {
		return err
	}

	if syndesis.Status.ForceUpgrade || k8serrors.IsNotFound(err) {
		// Upgrade pod not found or upgrade forced

		if namespaceVersion != targetVersion {
			a.log.Info("Upgrading syndesis resource ", "name", syndesis.Name, "currentVersion", namespaceVersion, "targetVersion", targetVersion)

			for _, res := range resources {
				operation.SetNamespaceAndOwnerReference(res, syndesis)

				err = createOrReplaceForce(ctx, a.client, res, true)
				if err != nil {
					return err
				}
			}

			// Install addons
			if addonsDir := *configuration.AddonsDirLocation; len(addonsDir) > 0 {
				addons, err := addons.GetAddonsResources(addonsDir)
				if err != nil {
					return err
				}
				for _, addon := range addons {
					operation.SetLabel(addon, "syndesis.io/addon-resource", "true")

					operation.SetNamespaceAndOwnerReference(addon, syndesis)

					err = createOrReplaceForce(ctx, a.client, addon, true)
					if err != nil {
						return err
					}
				}
			}

			var currentAttemptDescr string
			if syndesis.Status.UpgradeAttempts > 0 {
				currentAttemptDescr = " (attempt " + strconv.Itoa(int(syndesis.Status.UpgradeAttempts+1)) + ")"
			}

			target := syndesis.DeepCopy()
			target.Status.ForceUpgrade = false
			// Set to avoid stale information in case of operator version change
			target.Status.TargetVersion = targetVersion
			target.Status.Description = "Upgrading from " + namespaceVersion + " to " + targetVersion + currentAttemptDescr

			return a.client.Update(ctx, target)
		} else {
			// No upgrade pod, no version change: upgraded
			a.log.Info("Syndesis resource already upgraded to version ", "name", syndesis.Name, "targetVersion", targetVersion)
			return a.completeUpgrade(ctx, syndesis, targetVersion)
		}
	} else {
		// Upgrade pod present, checking the status
		if upgradePod.Status.Phase == v1.PodSucceeded {
			// Upgrade finished (correctly)

			// Getting the namespace version again for double check
			newNamespaceVersion, err := configuration.GetSyndesisVersionFromNamespace(ctx, a.client, syndesis.Namespace)
			if err != nil {
				return err
			}

			if newNamespaceVersion == targetVersion {
				a.log.Info("Syndesis resource upgraded", "name", syndesis.Name, "targetVersion", targetVersion)
				return a.completeUpgrade(ctx, syndesis, targetVersion)
			} else {
				a.log.Info("Upgrade pod terminated successfully but Syndesis version does not reflect target version. Forcing upgrade", "newVersion", newNamespaceVersion, "targetVersion", targetVersion, "name", syndesis.Name)

				var currentAttemptDescr string
				if syndesis.Status.UpgradeAttempts > 0 {
					currentAttemptDescr = " (attempt " + strconv.Itoa(int(syndesis.Status.UpgradeAttempts+1)) + ")"
				}

				target := syndesis.DeepCopy()
				target.Status.ForceUpgrade = true
				target.Status.TargetVersion = targetVersion
				target.Status.Description = "Upgrading from " + namespaceVersion + " to " + targetVersion + currentAttemptDescr

				return a.client.Update(ctx, target)
			}
		} else if upgradePod.Status.Phase == v1.PodFailed {
			// Upgrade failed
			a.log.Error(nil, "Failure while upgrading Syndesis resource: upgrade pod failure", "name", syndesis.Name, "targetVersion", targetVersion)

			target := syndesis.DeepCopy()
			target.Status.Phase = v1alpha1.SyndesisPhaseUpgradeFailureBackoff
			target.Status.Reason = v1alpha1.SyndesisStatusReasonUpgradePodFailed
			target.Status.Description = "Syndesis upgrade from " + namespaceVersion + " to " + targetVersion + " failed (it will be retried again)"
			target.Status.LastUpgradeFailure = &metav1.Time{
				Time: time.Now(),
			}
			target.Status.UpgradeAttempts = target.Status.UpgradeAttempts + 1

			return a.client.Update(ctx, target)
		} else {
			// Still running
			a.log.Info("Syndesis resource is currently being upgraded", "name", syndesis.Name, "targetVersion", targetVersion)
			return nil
		}

	}

}

func (a *upgradeAction) completeUpgrade(ctx context.Context, syndesis *v1alpha1.Syndesis, newVersion string) error {
	// After upgrade, pods may be detached
	if err := operation.AttachSyndesisToResource(ctx, a.scheme, a.client, syndesis); err != nil {
		return err
	}

	target := syndesis.DeepCopy()
	target.Status.Phase = v1alpha1.SyndesisPhaseInstalled
	target.Status.TargetVersion = ""
	target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
	target.Status.Description = ""
	target.Status.Version = newVersion
	target.Status.LastUpgradeFailure = nil
	target.Status.UpgradeAttempts = 0
	target.Status.ForceUpgrade = false

	return a.client.Update(ctx, target)
}

func (a *upgradeAction) getUpgradeResources(scheme *runtime.Scheme, syndesis *v1alpha1.Syndesis) ([]runtime.Object, error) {
	rawResources, err := syndesistemplate.GetUpgradeResources(scheme, syndesis, syndesistemplate.UpgradeParams{
		InstallParams: syndesistemplate.InstallParams{
			OAuthClientSecret: "-",
		},
		UpgradeRegistry: configuration.Registry,
	})

	if err != nil {
		return nil, err
	}

	resources := make([]runtime.Object, 0)
	for _, obj := range rawResources {
		res, err := util.LoadResourceFromYaml(scheme, obj.Raw)
		if err != nil {
			return nil, err
		}
		resources = append(resources, res)
	}

	return resources, nil
}

func (a *upgradeAction) findUpgradePod(resources []runtime.Object) (*v1.Pod, error) {
	for _, res := range resources {
		if pod, ok := res.(*v1.Pod); ok {
			if strings.HasPrefix(pod.Name, UpgradePodPrefix) {
				return pod, nil
			}
		}
	}
	return nil, errors.New("upgrade pod not found")
}

func (a *upgradeAction) getUpgradePodFromNamespace(ctx context.Context, podTemplate *v1.Pod, syndesis *v1alpha1.Syndesis) (*v1.Pod, error) {
	pod := v1.Pod{}
	key := client.ObjectKey{
		Namespace: syndesis.Namespace,
		Name:      podTemplate.Name,
	}
	err := a.client.Get(ctx, key, &pod)
	return &pod, err
}

func (a *upgradeAction) getAddonsResources(ctx context.Context, syndesis *v1alpha1.Syndesis) ([]unstructured.Unstructured, error) {
	types, err := getTypes(a.api)
	if err != nil {
		return nil, err
	}

	selector, err := labels.Parse("syndesis.io/addon-resource=true")
	if err != nil {
		return nil, err
	}

	res := make([]unstructured.Unstructured, 0)
	for _, t := range types {
		options := client.ListOptions{
			Namespace:     syndesis.Namespace,
			LabelSelector: selector,
			Raw: &metav1.ListOptions{
				TypeMeta: t,
			},
		}
		list := unstructured.UnstructuredList{
			Object: map[string]interface{}{
				"apiVersion": t.APIVersion,
				"kind":       t.Kind,
			},
		}
		if err := a.client.List(ctx, &options, &list); err != nil {
			if k8serrors.IsNotFound(err) ||
				k8serrors.IsForbidden(err) ||
				k8serrors.IsMethodNotSupported(err) {
				continue
			}
			return nil, err
		}
		for _, item := range list.Items {
			res = append(res, item)
		}
	}

	return res, nil
}

func getTypes(api kubernetes.Interface) ([]metav1.TypeMeta, error) {
	resources, err := api.Discovery().ServerPreferredNamespacedResources()
	if err != nil {
		return nil, err
	}

	types := make([]metav1.TypeMeta, 0)
	for _, resource := range resources {
		for _, r := range resource.APIResources {
			types = append(types, metav1.TypeMeta{
				Kind:       r.Kind,
				APIVersion: resource.GroupVersion,
			})
		}
	}

	return types, nil
}
