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
	"sigs.k8s.io/controller-runtime/pkg/manager"
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

type baseAction struct {
	log    logr.Logger
	client client.Client
	scheme *runtime.Scheme
	api    kubernetes.Interface
	mgr    manager.Manager
}

var actionLog = logf.Log.WithName("action")

type SyndesisOperatorAction interface {
	CanExecute(syndesis *v1alpha1.Syndesis) bool
	Execute(ctx context.Context, syndesis *v1alpha1.Syndesis) error
}

func NewOperatorActions(mgr manager.Manager, api kubernetes.Interface) []SyndesisOperatorAction {
	return []SyndesisOperatorAction{
		newCheckUpdatesAction(mgr, api),
		newInitializeAction(mgr, api),
		newInstallAction(mgr, api),
		newStartupAction(mgr, api),
		newUpgradeAction(mgr, api),
		newUpgradeBackoffAction(mgr, api),
		newUpgradeLegacyAction(mgr, api),
	}
}

func newBaseAction(mgr manager.Manager, api kubernetes.Interface, typeS string) baseAction {
	return baseAction{
		actionLog.WithValues("type", typeS),
		mgr.GetClient(),
		mgr.GetScheme(),
		api,
		mgr,
	}
}

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

func createOrReplaceForce(ctx context.Context, client client.Client, res runtime.Object, force bool) error {
	if err := client.Create(ctx, res); err != nil && k8serrors.IsAlreadyExists(err) {
		if force || canResourceBeReplaced(res) {
			err = client.Delete(ctx, res)
			if err != nil {
				return err
			}
			return client.Create(ctx, res)
		} else {
			return nil
		}
	} else {
		return err
	}
}

type updateFunction func(runtime.Object)

func updateOnLatestRevision(ctx context.Context, cl client.Client, res runtime.Object, change updateFunction) error {
	change(res)
	err := cl.Update(ctx, res)
	if err != nil && k8serrors.IsConflict(err) {
		attempts := 1
		for attempts <= 5 && err != nil && k8serrors.IsConflict(err) {
			var key client.ObjectKey
			if key, err = client.ObjectKeyFromObject(res); err != nil {
				return err
			}
			err = cl.Get(ctx, key, res)
			if err != nil {
				return err
			}

			change(res)
			err = cl.Update(ctx, res)
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
