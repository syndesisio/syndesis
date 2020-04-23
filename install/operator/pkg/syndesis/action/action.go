package action

import (
	"context"

	"github.com/go-logr/logr"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	corev1 "k8s.io/api/core/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/client-go/kubernetes"
	"sigs.k8s.io/controller-runtime/pkg/client"
	logf "sigs.k8s.io/controller-runtime/pkg/log"
	"sigs.k8s.io/controller-runtime/pkg/manager"
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
	log       logr.Logger
	clientTools *clienttools.ClientTools
	scheme    *runtime.Scheme
	mgr       manager.Manager
}

var actionLog = logf.Log.WithName("action")

type SyndesisOperatorAction interface {
	CanExecute(syndesis *v1beta1.Syndesis) bool
	Execute(ctx context.Context, syndesis *v1beta1.Syndesis) error
}

func NewOperatorActions(mgr manager.Manager, clientTools *clienttools.ClientTools) []SyndesisOperatorAction {
	return []SyndesisOperatorAction{
		newCheckUpdatesAction(mgr, clientTools),
		newUpgradeAction(mgr, clientTools),
		newUpgradeBackoffAction(mgr, clientTools),
		newInitializeAction(mgr, clientTools),
		newInstallAction(mgr, clientTools),
		newBackupAction(mgr, clientTools),
		newStartupAction(mgr, clientTools),
	}
}

func newBaseAction(mgr manager.Manager, clientTools *clienttools.ClientTools, typeS string) baseAction {
	return baseAction{
		actionLog.WithValues("type", typeS),
		clientTools,
		mgr.GetScheme(),
		mgr,
	}
}

func syndesisPhaseIs(syndesis *v1beta1.Syndesis, statuses ...v1beta1.SyndesisPhase) bool {
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

func canResourceBeReplaced(res runtime.Object) bool {
	if !replaceResourcesIfPresent {
		return false
	}

	if _, blacklisted := res.(*corev1.PersistentVolumeClaim); blacklisted {
		return false
	}
	return true
}
