package action

import (
	"context"
	"errors"
	"fmt"
	"strconv"
	"strings"
	"time"

	v1 "k8s.io/api/core/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/client-go/kubernetes"
	"sigs.k8s.io/controller-runtime/pkg/client"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/operation"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"sigs.k8s.io/controller-runtime/pkg/manager"
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
		operatorVersion, err := template.GetSyndesisVersionFromOperatorTemplate(a.scheme)
		if err != nil {
			return err
		}
		a.operatorVersion = operatorVersion
	}

	targetVersion := a.operatorVersion
	target := &v1alpha1.Syndesis{}
	if err := a.client.Get(ctx, client.ObjectKey{Namespace: syndesis.Namespace, Name: syndesis.Name}, target); err != nil {
		return err
	}

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

		if syndesis.Status.Version != targetVersion {
			a.log.Info("Upgrading syndesis resource ", "name", syndesis.Name, "currentVersion", syndesis.Status.Version, "targetVersion", targetVersion)

			for _, res := range resources {
				operation.SetNamespaceAndOwnerReference(res, target)

				err = createOrReplaceForce(ctx, a.client, res, true)
				if err != nil {
					return err
				}
			}

			var currentAttemptDescr string
			if syndesis.Status.UpgradeAttempts > 0 {
				currentAttemptDescr = " (attempt " + strconv.Itoa(int(syndesis.Status.UpgradeAttempts+1)) + ")"
			}

			target.Status.ForceUpgrade = false
			// Set to avoid stale information in case of operator version change
			target.Status.TargetVersion = targetVersion
			target.Status.Description = "Upgrading from " + syndesis.Status.Version + " to " + targetVersion + currentAttemptDescr

			return a.client.Update(ctx, target)
		} else {
			// No upgrade pod, no version change: upgraded
			a.log.Info("Syndesis resource already upgraded to version ", "name", syndesis.Name, "targetVersion", targetVersion)
			return a.completeUpgrade(ctx, target, targetVersion)
		}
	} else {
		// Upgrade pod present, checking the status
		if upgradePod.Status.Phase == v1.PodSucceeded {
			// Upgrade finished (correctly)

			a.log.Info("Syndesis resource upgraded", "name", syndesis.Name, "targetVersion", targetVersion)
			return a.completeUpgrade(ctx, target, targetVersion)
		} else if upgradePod.Status.Phase == v1.PodFailed {
			// Upgrade failed
			a.log.Error(nil, "Failure while upgrading Syndesis resource: upgrade pod failure", "name", syndesis.Name, "targetVersion", targetVersion)

			target.Status.Phase = v1alpha1.SyndesisPhaseUpgradeFailureBackoff
			target.Status.Reason = v1alpha1.SyndesisStatusReasonUpgradePodFailed
			target.Status.Description = "Syndesis upgrade from " + syndesis.Status.Version + " to " + targetVersion + " failed (it will be retried again)"
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

	c, err := template.GetTemplateContext()
	if err != nil {
		return nil, err
	}

	unstructured, err := template.GetUpgradeResources(scheme, syndesis, template.ResourceParams{
		OAuthClientSecret: "-",
		UpgradeRegistry:   c.Registry,
	})
	if err != nil {
		return nil, err
	}

	var structured []runtime.Object
	structured, unstructured = util.SeperateStructuredAndUnstructured(a.scheme, unstructured)

	if len(unstructured) > 0 {
		return nil, fmt.Errorf("Could not convert some objects to runtime.Object")
	}
	return structured, nil
}

func (a *upgradeAction) findUpgradePod(resources []runtime.Object) (*v1.Pod, error) {
	for _, res := range resources {
		if pod, ok := res.(*v1.Pod); ok {
			if strings.Contains(pod.Name, "upgrade") {
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
