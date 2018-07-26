package action

import (
	"errors"
	"github.com/operator-framework/operator-sdk/pkg/sdk"
	"github.com/sirupsen/logrus"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/operation"
	syndesistemplate "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/api/core/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"strconv"
	"strings"
	"time"
)

const (
	UpgradePodPrefix = "syndesis-upgrade-"
)

// Upgrades Syndesis to the version supported by this operator using the upgrade template.
type Upgrade struct {
	operatorVersion	string
}

func (a *Upgrade) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis, v1alpha1.SyndesisPhaseUpgrading)
}

func (a *Upgrade) Execute(syndesis *v1alpha1.Syndesis) error {
	if a.operatorVersion == "" {
		operatorVersion, err := configuration.GetSyndesisVersionFromOperatorTemplate()
		if err != nil {
			return err
		}
		a.operatorVersion = operatorVersion
	}

	namespaceVersion, err := configuration.GetSyndesisVersionFromNamespace(syndesis.Namespace)
	if err != nil {
		return err
	}
	targetVersion := a.operatorVersion

	resources, err := a.getUpgradeResources(syndesis)
	if err != nil {
		return err
	}

	templateUpgradePod, err := a.findUpgradePod(resources)
	if err != nil {
		return err
	}

	upgradePod, err := a.getUpgradePodFromNamespace(templateUpgradePod, syndesis)
	if err != nil && !k8serrors.IsNotFound(err) {
		return err
	}

	if syndesis.Status.ForceUpgrade || k8serrors.IsNotFound(err) {
		// Upgrade pod not found or upgrade forced

		if namespaceVersion != targetVersion {
			logrus.Info("Upgrading syndesis resource ", syndesis.Name, " from version ", namespaceVersion, " to ", targetVersion)

			for _, res := range resources {
				operation.SetNamespaceAndOwnerReference(res, syndesis)

				err = createOrReplaceForce(res, true)
				if err != nil {
					return err
				}
			}


			var currentAttemptDescr string
			if syndesis.Status.UpgradeAttempts > 0 {
				currentAttemptDescr = " (attempt " + strconv.Itoa(int(syndesis.Status.UpgradeAttempts + 1)) + ")"
			}

			target := syndesis.DeepCopy()
			target.Status.ForceUpgrade = false
			// Set to avoid stale information in case of operator version change
			target.Status.TargetVersion = targetVersion
			target.Status.Description = "Upgrading from " + namespaceVersion + " to " + targetVersion + currentAttemptDescr


			return sdk.Update(target)
		} else {
			// No upgrade pod, no version change: upgraded
			logrus.Info("Syndesis resource ", syndesis.Name, " already upgraded to version ", targetVersion)
			return completeUpgrade(syndesis, targetVersion)
		}
	} else {
		// Upgrade pod present, checking the status
		if upgradePod.Status.Phase == v1.PodSucceeded {
			// Upgrade finished (correctly)

			// Getting the namespace version again for double check
			newNamespaceVersion, err := configuration.GetSyndesisVersionFromNamespace(syndesis.Namespace)
			if err != nil {
				return err
			}

			if newNamespaceVersion == targetVersion {
				logrus.Info("Syndesis resource ", syndesis.Name, " upgraded to version ", targetVersion)
				return completeUpgrade(syndesis, targetVersion)
			} else {
				logrus.Warn("Upgrade pod terminated successfully but Syndesis version (", newNamespaceVersion, ") does not reflect target version (", targetVersion, ") for resource ", syndesis.Name, ". Forcing upgrade.")

				var currentAttemptDescr string
				if syndesis.Status.UpgradeAttempts > 0 {
					currentAttemptDescr = " (attempt " + strconv.Itoa(int(syndesis.Status.UpgradeAttempts + 1)) + ")"
				}

				target := syndesis.DeepCopy()
				target.Status.ForceUpgrade = true
				target.Status.TargetVersion = targetVersion
				target.Status.Description = "Upgrading from " + namespaceVersion + " to " + targetVersion + currentAttemptDescr

				return sdk.Update(target)
			}
		} else if upgradePod.Status.Phase == v1.PodFailed {
			// Upgrade failed
			logrus.Warn("Failure while upgrading Syndesis resource ", syndesis.Name, " to version ", targetVersion, ": upgrade pod failure")

			target := syndesis.DeepCopy()
			target.Status.Phase = v1alpha1.SyndesisPhaseUpgradeFailureBackoff
			target.Status.Reason = v1alpha1.SyndesisStatusReasonUpgradePodFailed
			target.Status.Description = "Syndesis upgrade from " + namespaceVersion + " to " + targetVersion + " failed (it will be retried again)"
			target.Status.LastUpgradeFailure = &metav1.Time{
				Time: time.Now(),
			}
			target.Status.UpgradeAttempts = target.Status.UpgradeAttempts + 1

			return sdk.Update(target)
		} else {
			// Still running
			logrus.Info("Syndesis resource ", syndesis.Name, " is currently being upgraded to version ", targetVersion)
			return nil
		}

	}

}

func completeUpgrade(syndesis *v1alpha1.Syndesis, newVersion string) error {
	// After upgrade, pods may be detached
	if err := operation.AttachSyndesisToResource(syndesis); err != nil {
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

	return sdk.Update(target)
}

func (a *Upgrade) getUpgradeResources(syndesis *v1alpha1.Syndesis) ([]runtime.Object, error) {
	rawResources, err := syndesistemplate.GetUpgradeResources(syndesis, syndesistemplate.UpgradeParams{
		InstallParams: syndesistemplate.InstallParams{
			OAuthClientSecret: "-",
		},
		SyndesisVersion: a.operatorVersion,
	})
	if err != nil {
		return nil, err
	}

	resources := make([]runtime.Object, 0)
	for _, obj := range rawResources {
		res, err := util.LoadKubernetesResource(obj.Raw)
		if err != nil {
			return nil, err
		}
		resources = append(resources, res)
	}

	return resources, nil
}

func (a *Upgrade) findUpgradePod(resources []runtime.Object) (*v1.Pod, error) {
	for _, res := range resources {
		if pod, ok := res.(*v1.Pod); ok {
			if strings.HasPrefix(pod.Name, UpgradePodPrefix) {
				return pod, nil
			}
		}
	}
	return nil, errors.New("upgrade pod not found")
}

func (a *Upgrade) getUpgradePodFromNamespace(podTemplate *v1.Pod, syndesis *v1alpha1.Syndesis) (*v1.Pod, error) {
	pod := v1.Pod{
		TypeMeta: metav1.TypeMeta{
			APIVersion: podTemplate.APIVersion,
			Kind: podTemplate.Kind,
		},
		ObjectMeta: metav1.ObjectMeta{
			Namespace: syndesis.Namespace,
			Name: podTemplate.Name,
		},
	}

	err := sdk.Get(&pod)
	return &pod, err
}