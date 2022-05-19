package syndesis

import (
	"context"
	"reflect"
	"time"

	synpkg "github.com/syndesisio/syndesis/install/operator/pkg"
	"k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/controller"
	"sigs.k8s.io/controller-runtime/pkg/handler"
	logf "sigs.k8s.io/controller-runtime/pkg/log"
	"sigs.k8s.io/controller-runtime/pkg/manager"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
	"sigs.k8s.io/controller-runtime/pkg/source"

	synapi "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta3"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/action"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/olm"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
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

	clientTools := &clienttools.ClientTools{}
	clientTools.SetRuntimeClient(mgr.GetClient())

	return &ReconcileSyndesis{
		clientTools: clientTools,
		scheme:      mgr.GetScheme(),
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
	err = c.Watch(&source.Kind{Type: &synapi.Syndesis{}}, &handler.EnqueueRequestForObject{})
	if err != nil {
		return err
	}

	actions = action.NewOperatorActions(mgr, r.clientTools)
	return nil
}

var _ reconcile.Reconciler = &ReconcileSyndesis{}

// ReconcileSyndesis reconciles a Syndesis object
type ReconcileSyndesis struct {
	// This client kit contains a split client, initialized using mgr.Client() above,
	// that reads objects from the cache and writes to the apiserver
	clientTools *clienttools.ClientTools
	scheme      *runtime.Scheme
}

// Reconcile the state of the Syndesis infrastructure elements
// Note:
// The Controller will requeue the Request to be processed again if the returned error is non-nil or
// Result.Requeue is true, otherwise upon completion it will remove the work from the queue.
func (r *ReconcileSyndesis) Reconcile(ctx context.Context, request reconcile.Request) (reconcile.Result, error) {
	reqLogger := log.WithValues("Request.Namespace", request.Namespace, "Request.Name", request.Name)
	reqLogger.V(2).Info("Reconciling Syndesis")

	// Fetch the Syndesis syndesis
	syndesis := &synapi.Syndesis{}
	client, _ := r.clientTools.RuntimeClient()

	err := client.Get(ctx, request.NamespacedName, syndesis)
	if err != nil {
		if errors.IsNotFound(err) {

			//
			// Allow the operator to be upgradeable if no syndesis resource is installed
			//
			state := olm.ConditionState{
				Status:  metav1.ConditionTrue,
				Reason:  "Ready",
				Message: "No Syndesis Resource installed so operator can be upgraded",
			}
			if upgErr := olm.SetUpgradeCondition(ctx, r.clientTools, request.Namespace, state); upgErr != nil {
				log.Error(upgErr, "Failed to set the upgrade condition on the operator")
			}

			// Request object not found, could have been deleted after reconcile request.
			// Owned objects are automatically garbage collected. For additional cleanup logic use finalizers.
			// Return and don't requeue
			return reconcile.Result{}, nil
		}

		// Error reading the object - requeue the request.
		log.Error(err, "Cannot read object", request.NamespacedName)

		//
		// Stop the operator being upgradeable until a state can be read
		//
		state := olm.ConditionState{
			Status:  metav1.ConditionFalse,
			Reason:  "NotReady",
			Message: "Read error detecting syndesis resource",
		}
		if upgErr := olm.SetUpgradeCondition(ctx, r.clientTools, request.Namespace, state); upgErr != nil {
			log.Error(upgErr, "Failed to set the upgrade condition on the operator")
		}

		return reconcile.Result{
			Requeue:      true,
			RequeueAfter: 10 * time.Second,
		}, err
	}

	//
	// Check the syndesis resource allows for upgrades. Otherwise disable
	//
	stateMsg := ""
	if syndesis.Status.Version != synpkg.DefaultOperatorTag {
		stateMsg = "Not upgradeable due to version mismatch of operator and Syndesis Resource"
	} else if syndesis.Status.Phase != synapi.SyndesisPhaseInstalled {
		stateMsg = "Not upgradeable due to Syndesis Resource phase not reaching 'Installed'"
	}

	if len(stateMsg) > 0 {
		state := olm.ConditionState{
			Status:  metav1.ConditionFalse,
			Reason:  "NotReady",
			Message: stateMsg,
		}
		if upgErr := olm.SetUpgradeCondition(ctx, r.clientTools, request.Namespace, state); upgErr != nil {
			log.Error(upgErr, "Failed to set the upgrade condition on the operator")
		}
	}

	for _, a := range actions {
		// Don't want to do anything if the syndesis resource has been updated in the meantime
		// This happens when a processing takes more tha the resync period
		if latest, err := r.isLatestVersion(ctx, syndesis); err != nil || !latest {
			log.Info("syndesis resource changed in the meantime, requeue and rerun in 5 seconds", "name", syndesis.Name)
			return reconcile.Result{
				Requeue:      true,
				RequeueAfter: 5 * time.Second,
			}, nil
		}

		if a.CanExecute(syndesis) {
			log.V(synpkg.DEBUG_LOGGING_LVL).Info("Running action", "action", reflect.TypeOf(a))
			if err := a.Execute(ctx, syndesis, request.Namespace); err != nil {
				log.Error(err, "Error reconciling", "action", reflect.TypeOf(a), "phase", syndesis.Status.Phase)
				return reconcile.Result{
					Requeue:      true,
					RequeueAfter: 10 * time.Second,
				}, nil
			}
		}
	}

	// Requeuing because actions expect this behaviour
	return reconcile.Result{
		Requeue:      true,
		RequeueAfter: 15 * time.Second,
	}, nil
}

func (r *ReconcileSyndesis) isLatestVersion(ctx context.Context, syndesis *synapi.Syndesis) (bool, error) {
	refreshed := syndesis.DeepCopy()
	client, _ := r.clientTools.RuntimeClient()
	if err := client.Get(ctx, types.NamespacedName{Name: refreshed.Name, Namespace: refreshed.Namespace}, refreshed); err != nil {
		return false, err
	}
	return refreshed.ResourceVersion == syndesis.ResourceVersion, nil
}
