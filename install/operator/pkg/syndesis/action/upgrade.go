package action

import (
	"context"
	"errors"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/operation"
	syndesistemplate "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/api/core/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"strconv"
	"strings"
	"time"
)

const (
	UpgradePodPrefix = "syndesis-upgrade-"
)

// Upgrades Syndesis to the version supported by this operator using the upgrade template.
type upgrade struct {
	action
	operatorVersion string
}

var (
	UpgradeAction =  upgrade{action{actionLog.WithValues("type", "install")},""}
)


func (a upgrade) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis, v1alpha1.SyndesisPhaseUpgrading)
}

func (a *upgrade) Execute(scheme *runtime.Scheme, cl client.Client, syndesis *v1alpha1.Syndesis) error {
	if a.operatorVersion == "" {
		operatorVersion, err := configuration.GetSyndesisVersionFromOperatorTemplate(scheme)
		if err != nil {
			return err
		}
		a.operatorVersion = operatorVersion
	}

	namespaceVersion, err := configuration.GetSyndesisVersionFromNamespace(cl, syndesis.Namespace)
	if err != nil {
		return err
	}
	targetVersion := a.operatorVersion

	resources, err := a.getUpgradeResources(scheme, syndesis)
	if err != nil {
		return err
	}

	templateUpgradePod, err := a.findUpgradePod(resources)
	if err != nil {
		return err
	}

	upgradePod, err := a.getUpgradePodFromNamespace(cl, templateUpgradePod, syndesis)
	if err != nil && !k8serrors.IsNotFound(err) {
		return err
	}

	if syndesis.Status.ForceUpgrade || k8serrors.IsNotFound(err) {
		// Upgrade pod not found or upgrade forced

		if namespaceVersion != targetVersion {
			a.log.Info("Upgrading syndesis resource ", "name", syndesis.Name, "currentVersion", namespaceVersion, "targetVersion", targetVersion)

			for _, res := range resources {
				operation.SetNamespaceAndOwnerReference(res, syndesis)

				err = createOrReplaceForce(cl, res, true)
				if err != nil {
					return err
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

			return cl.Status().Update(context.TODO(), target)
		} else {
			// No upgrade pod, no version change: upgraded
			a.log.Info("Syndesis resource already upgraded to version ", "name", syndesis.Name, "targetVersion", targetVersion)
			return completeUpgrade(scheme, cl, syndesis, targetVersion)
		}
	} else {
		// Upgrade pod present, checking the status
		if upgradePod.Status.Phase == v1.PodSucceeded {
			// Upgrade finished (correctly)

			// Getting the namespace version again for double check
			newNamespaceVersion, err := configuration.GetSyndesisVersionFromNamespace(cl, syndesis.Namespace)
			if err != nil {
				return err
			}

			if newNamespaceVersion == targetVersion {
				a.log.Info("Syndesis resource upgraded", "name", syndesis.Name, "targetVersion", targetVersion)
				return completeUpgrade(scheme, cl, syndesis, targetVersion)
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

				return cl.Status().Update(context.TODO(), target)
			}
		} else if upgradePod.Status.Phase == v1.PodFailed {
			// Upgrade failed
			a.log.Error(nil,"Failure while upgrading Syndesis resource: upgrade pod failure", "name", syndesis.Name, "targetVersion", targetVersion)

			target := syndesis.DeepCopy()
			target.Status.Phase = v1alpha1.SyndesisPhaseUpgradeFailureBackoff
			target.Status.Reason = v1alpha1.SyndesisStatusReasonUpgradePodFailed
			target.Status.Description = "Syndesis upgrade from " + namespaceVersion + " to " + targetVersion + " failed (it will be retried again)"
			target.Status.LastUpgradeFailure = &metav1.Time{
				Time: time.Now(),
			}
			target.Status.UpgradeAttempts = target.Status.UpgradeAttempts + 1

			return cl.Status().Update(context.TODO(), target)
		} else {
			// Still running
			a.log.Info("Syndesis resource is currently being upgraded", "name", syndesis.Name, "targetVersion", targetVersion)
			return nil
		}

	}

}

func completeUpgrade(scheme *runtime.Scheme, cl client.Client, syndesis *v1alpha1.Syndesis, newVersion string) error {
	// After upgrade, pods may be detached
	if err := operation.AttachSyndesisToResource(scheme, cl, syndesis); err != nil {
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

	return cl.Status().Update(context.TODO(), target)
}

func (a *upgrade) getUpgradeResources(scheme *runtime.Scheme, syndesis *v1alpha1.Syndesis) ([]runtime.Object, error) {
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

func (a *upgrade) findUpgradePod(resources []runtime.Object) (*v1.Pod, error) {
	for _, res := range resources {
		if pod, ok := res.(*v1.Pod); ok {
			if strings.HasPrefix(pod.Name, UpgradePodPrefix) {
				return pod, nil
			}
		}
	}
	return nil, errors.New("upgrade pod not found")
}

func (a *upgrade) getUpgradePodFromNamespace(cl client.Client, podTemplate *v1.Pod, syndesis *v1alpha1.Syndesis) (*v1.Pod, error) {
	pod := v1.Pod{}
	key := client.ObjectKey {
		Namespace: syndesis.Namespace,
		Name:      podTemplate.Name,
	}
	err := cl.Get(context.TODO(), key, &pod)
	return &pod, err
}
