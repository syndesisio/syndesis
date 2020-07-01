package kubernetes

import (
	"context"
	"fmt"
	"github.com/RHsyseng/operator-utils/pkg/resource"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
	"strings"
)

type Finalizer interface {
	GetName() string
	OnFinalize(owner resource.KubernetesResource, service PlatformService) error
}

func (e *ExtendedReconciler) RegisterFinalizer(f Finalizer) error {
	err := validateFinalizerName(f.GetName())
	if err != nil {
		return err
	}
	e.Finalizers[f.GetName()] = f
	return nil
}

func (e *ExtendedReconciler) UnregisterFinalizer(finalizer string) error {
	err := validateFinalizerName(finalizer)
	if err != nil {
		return err
	}
	delete(e.Finalizers, finalizer)
	return nil
}

//IsFinalizing An object is considered to be finalizing when its deletionTimestamp is not null
func (e *ExtendedReconciler) isFinalizing(owner metav1.Object) bool {
	return owner.GetDeletionTimestamp() != nil
}

//RemoveFinalizer removes a finalizer and updates the owner object
func (e *ExtendedReconciler) removeFinalizer(owner resource.KubernetesResource, finalizer string) error {
	err := validateFinalizerName(finalizer)
	if err != nil {
		return err
	}
	controllerutil.RemoveFinalizer(owner, finalizer)
	return e.Service.Update(context.TODO(), owner)
}

//FinalizeOnDelete triggers all the finalizers registered for the given object in case it is being deleted
func (e *ExtendedReconciler) finalizeOnDelete(owner resource.KubernetesResource) error {
	if !e.isFinalizing(owner) {
		return nil
	}
	for _, f := range owner.GetFinalizers() {
		finalizer := e.Finalizers[f]
		if finalizer != nil {
			err := finalizer.OnFinalize(owner, e.Service)
			if err != nil {
				return err
			}
			err = e.removeFinalizer(owner, f)
			if err != nil {
				return err
			}
		} else {
			return fmt.Errorf("finalizer %s does not have a Finalizer handler registered", finalizer)
		}
	}
	return nil
}

func validateFinalizerName(name string) error {
	if len(strings.TrimSpace(name)) == 0 {
		return fmt.Errorf("the finalizer name must not be empty")
	}
	return nil
}
