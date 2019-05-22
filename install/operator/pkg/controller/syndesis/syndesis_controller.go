package syndesis

import (
	"context"
	"reflect"
	"time"

	"k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/client-go/kubernetes"

	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller"
	"sigs.k8s.io/controller-runtime/pkg/handler"
	"sigs.k8s.io/controller-runtime/pkg/manager"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
	"sigs.k8s.io/controller-runtime/pkg/source"

	syndesisv1alpha1 "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/action"
)

var log = logf.Log.WithName("controller")

var (
	actions []action.SyndesisOperatorAction
)

// Add creates a new Syndesis Controller and adds it to the Manager. The Manager will set fields on the Controller
// and Start it when the Manager is Started.
func Add(mgr manager.Manager) error {
	reconciler, err := newReconciler(mgr)
	if err != nil {
		return err
	}
	return add(mgr, reconciler)
}

// newReconciler returns a new reconcile.Reconciler
func newReconciler(mgr manager.Manager) (*ReconcileSyndesis, error) {
	clientset, err := kubernetes.NewForConfig(mgr.GetConfig())
	if err != nil {
		return nil, err
	}
	return &ReconcileSyndesis{
		apis:   clientset,
		client: mgr.GetClient(),
		scheme: mgr.GetScheme(),
	}, nil
}

// add adds a new Controller to mgr with r as the reconcile.Reconciler
func add(mgr manager.Manager, r *ReconcileSyndesis) error {
	// Create a new controller
	c, err := controller.New("syndesis-controller", mgr, controller.Options{Reconciler: r})
	if err != nil {
		return err
	}

	// Watch for changes to primary resource Syndesis
	err = c.Watch(&source.Kind{Type: &syndesisv1alpha1.Syndesis{}}, &handler.EnqueueRequestForObject{})
	if err != nil {
		return err
	}

	actions = action.NewOperatorActions(mgr, r.apis)
	return nil
}

var _ reconcile.Reconciler = &ReconcileSyndesis{}

// ReconcileSyndesis reconciles a Syndesis object
type ReconcileSyndesis struct {
	// This client, initialized using mgr.Client() above, is a split client
	// that reads objects from the cache and writes to the apiserver
	client client.Client
	apis   kubernetes.Interface
	scheme *runtime.Scheme
}

// Reconcile the state of the Syndesis infrastructure elements
// Note:
// The Controller will requeue the Request to be processed again if the returned error is non-nil or
// Result.Requeue is true, otherwise upon completion it will remove the work from the queue.
func (r *ReconcileSyndesis) Reconcile(request reconcile.Request) (reconcile.Result, error) {
	reqLogger := log.WithValues("Request.Namespace", request.Namespace, "Request.Name", request.Name)
	reqLogger.V(2).Info("Reconciling Syndesis")

	// Fetch the Syndesis syndesis
	syndesis := &syndesisv1alpha1.Syndesis{}

	ctx := context.TODO()

	err := r.client.Get(ctx, request.NamespacedName, syndesis)
	if err != nil {
		if errors.IsNotFound(err) {
			// Request object not found, could have been deleted after reconcile request.
			// Owned objects are automatically garbage collected. For additional cleanup logic use finalizers.
			// Return and don't requeue
			return reconcile.Result{}, nil
		}
		// Error reading the object - requeue the request.
		log.Error(err, "Cannot read object", request.NamespacedName)
		return reconcile.Result{}, err
	}

	// Don't want to do anything if the syndesis resource has been updated in the meantime
	// This happens when a processing takes more tha the resync period
	if latest, err := r.isLatestVersion(ctx, syndesis); err != nil || !latest {
		log.Error(err, "Cannot get latest version")
		return reconcile.Result{}, err
	}

	for _, a := range actions {
		if a.CanExecute(syndesis) {
			log.V(2).Info("Running action", "action", reflect.TypeOf(a))
			if err := a.Execute(ctx, syndesis); err != nil {
				log.Error(err, "Error reconciling", "action", reflect.TypeOf(a), "phase", syndesis.Status.Phase)
				return reconcile.Result{}, err
			}
		}
	}

	// Requeuing because actions expect this behaviour
	return reconcile.Result{
		Requeue:      true,
		RequeueAfter: 15 * time.Second,
	}, nil
}

func (r *ReconcileSyndesis) isLatestVersion(ctx context.Context, syndesis *syndesisv1alpha1.Syndesis) (bool, error) {
	refreshed := syndesis.DeepCopy()
	if err := r.client.Get(ctx, types.NamespacedName{Name: refreshed.Name, Namespace: refreshed.Namespace}, refreshed); err != nil {
		return false, err
	}
	return refreshed.ResourceVersion == syndesis.ResourceVersion, nil
}
