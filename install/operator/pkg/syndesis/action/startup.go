package action

import (
	"errors"
	"github.com/openshift/api/apps/v1"
	"github.com/operator-framework/operator-sdk/pkg/sdk"
	"github.com/sirupsen/logrus"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// Waits for all pods to startup, then mark Syndesis as "Running".
type Startup struct {}


func (a *Startup) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		v1alpha1.SyndesisPhaseStarting,
		v1alpha1.SyndesisPhaseStartupFailed)
}

func (a *Startup) Execute(syndesis *v1alpha1.Syndesis) error {

	options := sdk.WithListOptions(&metav1.ListOptions{
		LabelSelector: "syndesis.io/app=syndesis,syndesis.io/type=infrastructure",
	})
	list := metav1.List{
		TypeMeta: metav1.TypeMeta{
			Kind: "DeploymentConfig",
			APIVersion: "apps.openshift.io/v1",
		},
	}
	if err := sdk.List(syndesis.Namespace, &list, options); err != nil {
		return err
	}

	if len(list.Items) == 0 {
		return errors.New("no deployment configs detected in the namespace")
	}

	ready := true
	var failedDeployment *string
	for _, o := range list.Items {
		if deplObj, err := util.LoadKubernetesResource(o.Raw); err != nil {
			return err
		} else if depl, ok := deplObj.(*v1.DeploymentConfig); ok {
			if depl.Spec.Replicas != depl.Status.ReadyReplicas {
				ready = false
			}
			if depl.Spec.Replicas != depl.Status.Replicas && depl.Status.Replicas == 0 && !isProcessing(depl) {
				failedDeployment = &depl.Name
			}
		}
	}

	if ready {
		target := syndesis.DeepCopy()
		target.Status.Phase = v1alpha1.SyndesisPhaseInstalled
		target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
		target.Status.Description = ""
		logrus.Info("Syndesis resource ", syndesis.Name, " installed successfully")
		return sdk.Update(target)
	} else if failedDeployment != nil {
		target := syndesis.DeepCopy()
		target.Status.Phase = v1alpha1.SyndesisPhaseStartupFailed
		target.Status.Reason = v1alpha1.SyndesisStatusReasonDeploymentNotReady
		target.Status.Description = "Some Syndesis deployments failed to startup within the allowed time frame"
		logrus.Info("Startup failed for Syndesis resource ", syndesis.Name, ". Deployment ", *failedDeployment, " not ready")
		return sdk.Update(target)
	} else {
		target := syndesis.DeepCopy()
		target.Status.Phase = v1alpha1.SyndesisPhaseStarting
		target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
		target.Status.Description = ""
		logrus.Info("Waiting for Syndesis resource ", syndesis.Name, " to startup")
		return sdk.Update(target)
	}
}

func isProcessing(dc *v1.DeploymentConfig) bool {
	for _, condition := range dc.Status.Conditions {
		if condition.Type == v1.DeploymentProgressing {
			if condition.Status == corev1.ConditionFalse {
				return false
			}
		}
	}
	return true
}

