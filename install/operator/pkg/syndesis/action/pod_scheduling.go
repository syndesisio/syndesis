package action

import (
	"context"
	"encoding/json"

	"reflect"

	oappsv1 "github.com/openshift/api/apps/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta2"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

type patchOp struct {
	Op    string          `json:"op"`
	Path  string          `json:"path"`
	Value json.RawMessage `json:"value,omitempty"`
}

type podSchedulingAction struct {
	baseAction
	currentInfraScheduling       v1beta2.SchedulingSpec
	currentIntegrationScheduling v1beta2.SchedulingSpec
	updateInfraScheduling        bool
	updateIntegrationScheduling  bool
}

func newPodSchedulingAction(mgr manager.Manager, clientTools *clienttools.ClientTools) SyndesisOperatorAction {
	return &podSchedulingAction{
		newBaseAction(mgr, clientTools, "pod_scheduling"), v1beta2.SchedulingSpec{}, v1beta2.SchedulingSpec{}, false, false,
	}
}

func (a *podSchedulingAction) CanExecute(syndesis *v1beta2.Syndesis) bool {
	canExecute := syndesisPhaseIs(syndesis, v1beta2.SyndesisPhaseInstalled)
	if canExecute {
		a.updateInfraScheduling = !reflect.DeepEqual(a.currentInfraScheduling, syndesis.Spec.InfraScheduling)
		a.updateIntegrationScheduling = !reflect.DeepEqual(a.currentIntegrationScheduling, syndesis.Spec.IntegrationScheduling)
		canExecute = a.updateInfraScheduling || a.updateIntegrationScheduling
	}
	return canExecute
}

func (a *podSchedulingAction) Execute(ctx context.Context, syndesis *v1beta2.Syndesis) error {
	if a.updateInfraScheduling {
		a.executeInfraScheduling(ctx, syndesis)
	}
	if a.updateIntegrationScheduling {
		a.executeIntegrationScheduling(ctx, syndesis)
	}
	return nil
}

func (a *podSchedulingAction) executeInfraScheduling(ctx context.Context, syndesis *v1beta2.Syndesis) {
	list := oappsv1.DeploymentConfigList{
		TypeMeta: metav1.TypeMeta{
			Kind: "DeploymentConfig",
		},
	}
	selector, _ := labels.Parse("syndesis.io/app=syndesis,syndesis.io/type=infrastructure")
	options := client.ListOptions{
		Namespace:     syndesis.Namespace,
		LabelSelector: selector,
	}

	rtClient, _ := a.clientTools.RuntimeClient()
	if err := rtClient.List(ctx, &list, &options); err != nil {
		a.log.Error(err, "Error listing DeploymentConfig to apply infra scheduling", "selector", selector)
	}
	if len(list.Items) == 0 {
		a.log.Error(nil, "There are no DeploymentConfig infra component to apply the infraScheduling", "label selector", selector)
	}

	ops := make([]patchOp, 0)
	if syndesis.Spec.InfraScheduling.Affinity != nil {
		affinityData, err := json.Marshal(syndesis.Spec.InfraScheduling.Affinity)
		if err != nil {
			a.log.Error(err, "Error unmarshalling", "syndesis.Spec.InfraScheduling.Affinity", syndesis.Spec.InfraScheduling.Affinity)
		}
		payload := patchOp{
			Op:    "add",
			Path:  "/spec/template/spec/affinity",
			Value: json.RawMessage(affinityData),
		}
		ops = append(ops, payload)
	} else {
		// replace instead of "remove", because using "remove" on a non-existing target throws an error
		payload := patchOp{
			Op:    "replace",
			Path:  "/spec/template/spec/affinity",
			Value: json.RawMessage(`null`),
		}
		ops = append(ops, payload)
	}

	if syndesis.Spec.InfraScheduling.Tolerations != nil {
		tolerationsData, err := json.Marshal(syndesis.Spec.InfraScheduling.Tolerations)
		if err != nil {
			a.log.Error(err, "Error unmarshalling", "syndesis.Spec.InfraScheduling.Tolerations", syndesis.Spec.InfraScheduling.Tolerations)
		}
		payload := patchOp{
			Op:    "add",
			Path:  "/spec/template/spec/tolerations",
			Value: json.RawMessage(tolerationsData),
		}
		ops = append(ops, payload)
	} else {
		payload := patchOp{
			Op:    "replace",
			Path:  "/spec/template/spec/tolerations",
			Value: json.RawMessage(`null`),
		}
		ops = append(ops, payload)
	}

	payload, _ := json.Marshal(ops)
	for _, depl := range list.Items {
		a.log.Info("Patching dc/" + depl.GetName() + " with new affinity/toleration values, this action will restart the pod.")

		dc := &oappsv1.DeploymentConfig{
			ObjectMeta: metav1.ObjectMeta{
				Name:      depl.Name,
				Namespace: syndesis.Namespace,
			}}
		err := rtClient.Patch(ctx, dc, client.RawPatch(types.JSONPatchType, payload))
		if err != nil {
			a.log.Error(err, "Error patching dc/"+depl.GetName(), "ReasonForError", errors.ReasonForError(err))
		}
	}
	a.currentInfraScheduling = syndesis.Spec.InfraScheduling
}

func (a *podSchedulingAction) executeIntegrationScheduling(ctx context.Context, syndesis *v1beta2.Syndesis) {
	list := oappsv1.DeploymentConfigList{
		TypeMeta: metav1.TypeMeta{
			Kind: "DeploymentConfig",
		},
	}
	selector, _ := labels.Parse("syndesis.io/type=integration")
	options := client.ListOptions{
		Namespace:     syndesis.Namespace,
		LabelSelector: selector,
	}

	rtClient, _ := a.clientTools.RuntimeClient()
	if err := rtClient.List(ctx, &list, &options); err != nil {
		a.log.Error(err, "Error listing DeploymentConfig to apply integration scheduling", "selector", selector)
	}

	a.currentIntegrationScheduling = syndesis.Spec.IntegrationScheduling
	if len(list.Items) == 0 {
		a.log.Info("There are no DeploymentConfig integrations with label 'syndesis.io/type=integration' to apply the integrationScheduling")
		return
	}

	ops := make([]patchOp, 0)
	if syndesis.Spec.IntegrationScheduling.Affinity != nil {
		affinityData, err := json.Marshal(syndesis.Spec.IntegrationScheduling.Affinity)
		if err != nil {
			a.log.Error(err, "Error unmarshalling", "syndesis.Spec.IntegrationScheduling.Affinity", syndesis.Spec.IntegrationScheduling.Affinity)
		}
		payload := patchOp{
			Op:    "add",
			Path:  "/spec/template/spec/affinity",
			Value: json.RawMessage(affinityData),
		}
		ops = append(ops, payload)
	} else {
		// "replace" instead of "remove", because using "remove" on a non-existing target throws an error
		payload := patchOp{
			Op:    "replace",
			Path:  "/spec/template/spec/affinity",
			Value: json.RawMessage(`null`),
		}
		ops = append(ops, payload)
	}

	if syndesis.Spec.IntegrationScheduling.Tolerations != nil {
		tolerationsData, err := json.Marshal(syndesis.Spec.IntegrationScheduling.Tolerations)
		if err != nil {
			a.log.Error(err, "Error unmarshalling", "syndesis.Spec.IntegrationScheduling.Tolerations", syndesis.Spec.IntegrationScheduling.Tolerations)
		}
		payload := patchOp{
			Op:    "add",
			Path:  "/spec/template/spec/tolerations",
			Value: json.RawMessage(tolerationsData),
		}
		ops = append(ops, payload)
	} else {
		payload := patchOp{
			Op:    "replace",
			Path:  "/spec/template/spec/tolerations",
			Value: json.RawMessage(`null`),
		}
		ops = append(ops, payload)
	}

	payload, _ := json.Marshal(ops)
	for _, depl := range list.Items {
		a.log.Info("Patching Integration: dc/" + depl.GetName() + " with new affinity/toleration values, this action will restart the integration pod.")
		dc := &oappsv1.DeploymentConfig{
			ObjectMeta: metav1.ObjectMeta{
				Name:      depl.Name,
				Namespace: syndesis.Namespace,
			}}
		err := rtClient.Patch(ctx, dc, client.RawPatch(types.JSONPatchType, payload))
		if err != nil {
			a.log.Error(err, "Error patching dc/"+depl.GetName())
		}
	}
}
