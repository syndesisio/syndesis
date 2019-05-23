package action

import (
	"context"
	"errors"
	"github.com/openshift/api/apps/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

// Waits for all pods to startup, then mark Syndesis as "Running".
type startupAction struct {
	baseAction
}

func newStartupAction(mgr manager.Manager, api kubernetes.Interface) SyndesisOperatorAction {
	return &startupAction{
		newBaseAction(mgr, api, "startup"),
	}
}

func (a *startupAction) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		v1alpha1.SyndesisPhaseStarting,
		v1alpha1.SyndesisPhaseStartupFailed)
}

func (a *startupAction) Execute(ctx context.Context, syndesis *v1alpha1.Syndesis) error {

	list := v1.DeploymentConfigList{
		TypeMeta: metav1.TypeMeta{
			Kind:       "DeploymentConfig",
			APIVersion: "apps.openshift.io/v1",
		},
	}
	listOptions := client.ListOptions{Namespace: syndesis.Namespace}
	if err := listOptions.SetLabelSelector("syndesis.io/app=syndesis,syndesis.io/type=infrastructure"); err != nil {
		return err
	}

	if err := a.client.List(ctx, &listOptions, &list); err != nil {
		return err
	}

	if len(list.Items) == 0 {
		return errors.New("no deployment configs detected in the namespace")
	}

	ready := true
	var failedDeployment *string
	for _, depl := range list.Items {
		if depl.Spec.Replicas != depl.Status.ReadyReplicas {
			a.log.V(2).Info("Not ready", "desired", depl.Spec.Replicas, "actual", depl.Status.ReadyReplicas, "deployment", depl.Name)
			ready = false
		}
		if depl.Spec.Replicas != depl.Status.Replicas && depl.Status.Replicas == 0 && !isProcessing(&depl) {
			failedDeployment = &depl.Name
		}
	}

	if ready {
		target := syndesis.DeepCopy()
		target.Status.Phase = v1alpha1.SyndesisPhaseInstalled
		target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
		target.Status.Description = ""
		a.log.Info("Syndesis resource installed successfully", "name", syndesis.Name)
		return a.client.Update(ctx, target)
	} else if failedDeployment != nil {
		target := syndesis.DeepCopy()
		target.Status.Phase = v1alpha1.SyndesisPhaseStartupFailed
		target.Status.Reason = v1alpha1.SyndesisStatusReasonDeploymentNotReady
		target.Status.Description = "Some Syndesis deployments failed to startup within the allowed time frame"
		a.log.V(2).Info("Startup failed for Syndesis resource. Deployment not ready", "name", syndesis.Name, "deployment", *failedDeployment)
		return a.client.Update(ctx, target)
	} else {
		target := syndesis.DeepCopy()
		target.Status.Phase = v1alpha1.SyndesisPhaseStarting
		target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
		target.Status.Description = ""
		a.log.V(2).Info("Waiting for Syndesis resource to startup", "name", syndesis.Name)
		return a.client.Update(ctx, target)
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
