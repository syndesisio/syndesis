package action

import (
	"context"
	"github.com/go-logr/logr"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	corev1 "k8s.io/api/core/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/client-go/kubernetes"
	"sigs.k8s.io/controller-runtime/pkg/client"
	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
)

const (
	replaceResourcesIfPresent = true
)

// Client is an abstraction for a k8s client
type Client struct {
	client.Client
	kubernetes.Interface
}

type action struct {
	log logr.Logger
}

var actionLog = logf.Log.WithName("action").WithValues("type", )

type InstallationAction interface {
	CanExecute(syndesis *v1alpha1.Syndesis) bool

	Execute(scheme *runtime.Scheme, cl Client, syndesis *v1alpha1.Syndesis) error
}

type updateFunction func(runtime.Object)

func syndesisPhaseIs(syndesis *v1alpha1.Syndesis, statuses ...v1alpha1.SyndesisPhase) bool {
	if syndesis == nil {
		return false
	}

	currentStatus := syndesis.Status.Phase
	for _, status := range statuses {
		if currentStatus == status {
			return true
		}
	}
	return false
}

func createOrReplace(client client.Client, res runtime.Object) error {
	return createOrReplaceForce(client, res, false)
}

func createOrReplaceForce(client client.Client, res runtime.Object, force bool) error {
	if err := client.Create(context.TODO(), res); err != nil && k8serrors.IsAlreadyExists(err) {
		if force || canResourceBeReplaced(res) {
			err = client.Delete(context.TODO(), res)
			if err != nil {
				return err
			}
			return client.Create(context.TODO(), res)
		} else {
			return nil
		}
	} else {
		return err
	}
}

func updateOnLatestRevision(cl client.Client, res runtime.Object, change updateFunction) error {
	change(res)
	err := cl.Update(context.TODO(), res)
	if err != nil && k8serrors.IsConflict(err) {
		attempts := 1
		for attempts <= 5 && err != nil && k8serrors.IsConflict(err) {
			var key client.ObjectKey
			if key, err = client.ObjectKeyFromObject(res); err != nil {
				return err
			}
			err = cl.Get(context.TODO(), key, res)
			if err != nil {
				return err
			}

			change(res)
			err = cl.Update(context.TODO(), res)
			attempts++
		}
	}
	return err
}

func canResourceBeReplaced(res runtime.Object) bool {
	if !replaceResourcesIfPresent {
		return false
	}

	if _, blacklisted := res.(*corev1.PersistentVolumeClaim); blacklisted {
		return false
	}
	return true
}
