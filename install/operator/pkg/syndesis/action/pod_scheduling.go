package action

import (
	"context"
	"encoding/json"

	"reflect"

	oappsv1 "github.com/openshift/api/apps/v1"
	synapi "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta3"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
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
	currentIntegrationScheduling synapi.SchedulingSpec
	updateIntegrationScheduling  bool
}

func newPodSchedulingAction(mgr manager.Manager, clientTools *clienttools.ClientTools) SyndesisOperatorAction {
	return &podSchedulingAction{
		newBaseAction(mgr, clientTools, "pod_scheduling"), synapi.SchedulingSpec{}, false,
	}
}

func (a *podSchedulingAction) CanExecute(syndesis *synapi.Syndesis) bool {
	canExecute := syndesisPhaseIs(syndesis, synapi.SyndesisPhaseInstalled)
	if canExecute {
		a.updateIntegrationScheduling = !reflect.DeepEqual(a.currentIntegrationScheduling, syndesis.Spec.IntegrationScheduling)
		canExecute = a.updateIntegrationScheduling
	}
	return canExecute
}

func (a *podSchedulingAction) Execute(ctx context.Context, syndesis *synapi.Syndesis) error {
	if a.updateIntegrationScheduling {
		a.executeIntegrationScheduling(ctx, syndesis)
	}
	return nil
}

func (a *podSchedulingAction) executeIntegrationScheduling(ctx context.Context, syndesis *synapi.Syndesis) {
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
