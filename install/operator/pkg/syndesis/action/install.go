package action

import (
	"context"
	"errors"
	"time"


	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/openshift/serviceaccount"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	corev1 "k8s.io/api/core/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/runtime/schema"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"

	"k8s.io/client-go/kubernetes"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/olm"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/operation"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

const (
	// SyndesisRouteName the base name for the Route to access Syndesis application
	SyndesisRouteName = "syndesis"
	// SyndesisPullSecret name of the Secret used to pull images from image registries that require authentication
	SyndesisPullSecret = "syndesis-pull-secret"
)

// Install syndesis into the namespace, taking resources from the bundled template.
type installAction struct {
	baseAction
}

func newInstallAction(mgr manager.Manager, clientTools *clienttools.ClientTools) SyndesisOperatorAction {
	return &installAction{
		newBaseAction(mgr, clientTools, "install"),
	}
}

func (a *installAction) installResource(ctx context.Context, rtClient client.Client, syndesis *v1beta1.Syndesis, res unstructured.Unstructured) (*unstructured.Unstructured, error) {
	operation.SetNamespaceAndOwnerReference(res, syndesis)
	o, modificationType, err := util.CreateOrUpdate(ctx, rtClient, &res)
	if err != nil {
		if util.IsNoKindMatchError(err) {
			gvk := res.GroupVersionKind()
			if _, found := kindsReportedNotAvailable[gvk]; !found {
				kindsReportedNotAvailable[gvk] = time.Now()
				a.log.Info("optional custom resource definition is not installed.", "group", gvk.Group, "version", gvk.Version, "kind", gvk.Kind)
			}
		} else {
			a.log.Info("failed to create or replace resource", "kind", res.GetKind(), "name", res.GetName(), "namespace", res.GetNamespace())
			return nil, err
		}
	} else {
		if modificationType != controllerutil.OperationResultNone {
			a.log.Info("resource "+string(modificationType), "kind", res.GetKind(), "name", res.GetName(), "namespace", res.GetNamespace())
		}
	}

	return o, nil
}

func (a *installAction) CanExecute(syndesis *v1beta1.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		v1beta1.SyndesisPhaseInstalling,
		v1beta1.SyndesisPhaseInstalled,
		v1beta1.SyndesisPhasePostUpgradeRun,
		v1beta1.SyndesisPhaseStarting,
		v1beta1.SyndesisPhaseStartupFailed,
	)
}

var kindsReportedNotAvailable = map[schema.GroupVersionKind]time.Time{}

func (a *installAction) Execute(ctx context.Context, syndesis *v1beta1.Syndesis) error {
	if syndesisPhaseIs(syndesis, v1beta1.SyndesisPhaseInstalling) {
		a.log.Info("installing Syndesis resource", "name", syndesis.Name)
	} else if syndesisPhaseIs(syndesis, v1beta1.SyndesisPhasePostUpgradeRun) {
		a.log.Info("installing Syndesis resource for the first time after upgrading", "name", syndesis.Name)
	}

	resourcesThatShouldExist := map[types.UID]bool{}

	rtClient, _ := a.clientTools.RuntimeClient()
	// Load configuration to to use as context for generate pkg
	config, err := configuration.GetProperties(ctx, configuration.TemplateConfig, a.clientTools, syndesis)
	if err != nil {
		a.log.Error(err, "Error occurred while initialising configuration")
		return err
	}

	//
	// Check for presence of route hostname as required for k8
	//
	if err := config.CheckRouteHostname(); err != nil {
		return err
	}

	//
	// Check for oauth secrets as required for k8
	//
	if err := config.CheckOAuthCredentialSecret(ctx, rtClient, syndesis); err != nil {
		return err
	}

	// Check if an image secret exists, to be used to connect to registries that require authentication
	secret := &corev1.Secret{}
	err = rtClient.Get(ctx, types.NamespacedName{Namespace: syndesis.Namespace, Name: SyndesisPullSecret}, secret)
	if err != nil {
		if k8serrors.IsNotFound(err) {
			secret = nil
		} else {
			return err
		}
	}

	if secret != nil {
		config.ImagePullSecrets = append(config.ImagePullSecrets, secret.Name)
	}

	serviceAccount, err := installServiceAccount(ctx, rtClient, syndesis, secret)
	if err != nil {
		return err
	}
	resourcesThatShouldExist[serviceAccount.GetUID()] = true

	token, err := serviceaccount.GetServiceAccountToken(ctx, rtClient, serviceAccount.Name, syndesis.Namespace)
	if err != nil {
		a.log.Info("Unable to get service account token", "error message", err.Error())
		return nil
	}
	config.OpenShiftOauthClientSecret = token

	// Render the route resource...
	all, err := generator.RenderDir("./route/", config)
	if err != nil {
		return err
	}

	routes, _ := util.SeperateStructuredAndUnstructured(a.scheme, all)
	syndesisRoute, err := installSyndesisRoute(ctx, rtClient, syndesis, routes)
	if err != nil {
		a.log.Info("Unable to set route syndesis", "error message", err.Error())
		return nil
	}

	if err := config.SetRoute(ctx, syndesisRoute.Host()); err != nil {
		return err
	}

	resourcesThatShouldExist[syndesisRoute.Meta().GetUID()] = true

	if err := config.SetConsoleLink(ctx, rtClient, syndesis, syndesisRoute.Host()); err != nil {
			return err
	}

	// Render the remaining syndesis resources...
	all, err = generator.RenderDir("./infrastructure/", config)
	if err != nil {
		return err
	}

	// Render the database resource if needed...
	if syndesis.Spec.Components.Database.ExternalDbURL == "" {
		dbResources, err := generator.RenderDir("./database/", config)
		if err != nil {
			return err
		}

		//
		// Log the possible combination of values chosen for the db persistent volume claim
		//
		if syndesisPhaseIs(syndesis, v1beta1.SyndesisPhaseInstalling) {
			a.log.Info("Will bind sydnesis-db to persistent volume with criteria ",
				"volume-access-mode", config.Syndesis.Components.Database.Resources.VolumeAccessMode,
				"volume-name", config.Syndesis.Components.Database.Resources.VolumeName,
				"storage-class", config.Syndesis.Components.Database.Resources.VolumeStorageClass)
		}

		all = append(all, dbResources...)
	}

	// Link the image secret to service accounts
	if secret != nil {
		err = linkImageSecretToServiceAccounts(ctx, rtClient, syndesis, secret)
		if err != nil {
			return err
		}
	}

	// Install the resources..
	for _, res := range all {
		o, err := a.installResource(ctx, rtClient, syndesis, res)
		if err != nil {
			return err // Fail-fast for core components
		}
		resourcesThatShouldExist[o.GetUID()] = true
	}

	addonsInfo := configuration.GetAddonsInfo(*config)
	for _, addonInfo := range addonsInfo {
		if !addonInfo.IsEnabled() {
			continue
		}

		a.log.Info("Installing addon", "Name", addonInfo.Name())

		if config.ApiServer.OlmSupport && addonInfo.GetOlmSpec() != nil && addonInfo.GetOlmSpec().Package != "" {
			a.log.Info("Subscribing to OLM operator:", "Package", addonInfo.GetOlmSpec().Package, "Channel", addonInfo.GetOlmSpec().Channel)
			//
			// Using the operator hub is not mutally exclusive to loading the addon
			// resources. Each addon should be tailored with conditionals to be
			// compatible with using the operator hub or not, ie. operator installation
			// should be delegated to the OLM & only if that's not possible should it
			// be installed from syndesis' own resources.
			//
			err := olm.SubscribeOperator(ctx, a.clientTools, config, addonInfo.GetOlmSpec())
			if err != nil {
				a.log.Error(err, "A subscription to an OLM operator failed", "Addon Name", addonInfo.Name(), "Package", addonInfo.GetOlmSpec().Package)
				continue
			}
		}

		addonDir := "./addons/" + addonInfo.Name() + "/"
		f, err := generator.GetAssetsFS().Open(addonDir)
		if err != nil {
			a.log.Info("unsupported addon configured", "addon", addonInfo.Name(), "error", err)
			continue
		}
		f.Close()

		resources, err := generator.RenderDir(addonDir, config)
		if err != nil {
			a.log.Info("Rendering of addon resources failed", "addon", addonInfo.Name(), "error message", err.Error())
			continue
		}

		//
		// Install the resources of this addon
		// If there is an error do NOT fail-fast but
		// try and continue to install the other addons
		//
		for _, res := range resources {
			o, err := a.installResource(ctx, rtClient, syndesis, res)
			if err != nil {
				a.log.Info("Install of addon failed", "addon", addonInfo.Name(), "error message", err.Error())
				break
			}
			resourcesThatShouldExist[o.GetUID()] = true
		}
	}

	// Find resources which need to be deleted.
	labelSelector, err := labels.Parse("owner=" + string(syndesis.GetUID()))
	if err != nil {
		panic(err)
	}
	options := client.ListOptions{
		Namespace:     syndesis.Namespace,
		LabelSelector: labelSelector,
	}

	api, _ := a.clientTools.ApiClient()
	err = ListAllTypesInChunks(ctx, api, rtClient, options, func(list []unstructured.Unstructured) error {
		for _, res := range list {
			if resourcesThatShouldExist[res.GetUID()] {
				continue
			}
			if res.GetOwnerReferences() == nil || len(res.GetOwnerReferences()) == 0 {
				continue
			}
			if res.GetOwnerReferences()[0].UID != syndesis.GetUID() {
				continue
			}

			// Found a resource that should not exist!
			err := rtClient.Delete(ctx, &res)
			if err != nil {
				if !k8serrors.IsNotFound(err) {
					a.log.Error(err, "could not deleted", "kind", res.GetKind(), "name", res.GetName(), "namespace", res.GetNamespace())
				}
			} else {
				a.log.Info("resource deleted", "kind", res.GetKind(), "name", res.GetName(), "namespace", res.GetNamespace())
			}
		}
		return nil
	})
	if err != nil {
		return err
	}

	addRouteAnnotation(syndesis, syndesisRoute)
	target := syndesis.DeepCopy()
	if syndesis.Status.Phase == v1beta1.SyndesisPhaseInstalling {
		// Installation completed, set the next state
		target.Status.Phase = v1beta1.SyndesisPhaseStarting
		target.Status.Reason = v1beta1.SyndesisStatusReasonMissing
		target.Status.Description = ""
		_, _, err := util.CreateOrUpdate(ctx, rtClient, target, "kind", "apiVersion")
		if err != nil {
			return err
		}

		a.log.Info("Syndesis resource installed", "name", target.Name)
	} else if syndesis.Status.Phase == v1beta1.SyndesisPhasePostUpgradeRun {
		// Installation completed, set the next state
		target.Status.Phase = v1beta1.SyndesisPhasePostUpgradeRunSucceed
		target.Status.Reason = v1beta1.SyndesisStatusReasonMissing
		target.Status.Description = ""
		_, _, err := util.CreateOrUpdate(ctx, rtClient, target, "kind", "apiVersion")
		if err != nil {
			return err
		}

		a.log.Info("Syndesis resource installed after upgrading", "name", target.Name)
	}

	return nil
}

// ListAllTypesInChunks pages through set of API types
func ListAllTypesInChunks(ctx context.Context, api kubernetes.Interface, c client.Client, options client.ListOptions, handler func([]unstructured.Unstructured) error) error {
	types, err := getTypes(api)
	if err != nil {
		return err
	}
nextType:
	for _, t := range types {
		options := client.ListOptions{
			Namespace:     options.Namespace,
			LabelSelector: options.LabelSelector,
			Raw: &metav1.ListOptions{
				TypeMeta: t,
				Limit:    200,
			},
		}
		list := unstructured.UnstructuredList{
			Object: map[string]interface{}{
				"apiVersion": t.APIVersion,
				"kind":       t.Kind,
			},
		}
		err = util.ListInChunks(ctx, c, &options, &list, handler)
		if err != nil {
			if k8serrors.IsNotFound(err) ||
				k8serrors.IsForbidden(err) ||
				k8serrors.IsMethodNotSupported(err) {
				continue nextType
			}
			return err
		}
	}
	return nil
}

func getTypes(api kubernetes.Interface) ([]metav1.TypeMeta, error) {
	resources, err := api.Discovery().ServerPreferredNamespacedResources()
	if err != nil {
		return nil, err
	}

	types := make([]metav1.TypeMeta, 0)
	for _, resource := range resources {
		for _, r := range resource.APIResources {
			types = append(types, metav1.TypeMeta{
				Kind:       r.Kind,
				APIVersion: resource.GroupVersion,
			})
		}
	}

	return types, nil
}

func installServiceAccount(ctx context.Context, cl client.Client, syndesis *v1beta1.Syndesis, secret *corev1.Secret) (*corev1.ServiceAccount, error) {
	sa := newSyndesisServiceAccount()
	if secret != nil {
		linkImagePullSecret(sa, secret)
	}

	operation.SetNamespaceAndOwnerReference(sa, syndesis)
	// We don't replace the service account if already present, to let Kubernetes generate its tokens
	o, _, err := util.CreateOrUpdate(ctx, cl, sa)
	if err != nil {
		return nil, err
	}
	sa.SetUID(o.GetUID())
	return sa, nil
}

func newSyndesisServiceAccount() *corev1.ServiceAccount {
	sa := corev1.ServiceAccount{
		TypeMeta: metav1.TypeMeta{
			APIVersion: "v1",
			Kind:       "ServiceAccount",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name: "syndesis-oauth-client",
			Labels: map[string]string{
				"app": "syndesis",
			},
			Annotations: map[string]string{
				"serviceaccounts.openshift.io/oauth-redirecturi.local":       "https://localhost:4200",
				"serviceaccounts.openshift.io/oauth-redirecturi.route":       "https://",
				"serviceaccounts.openshift.io/oauth-redirectreference.route": `{"kind": "OAuthRedirectReference", "apiVersion": "v1", "reference": {"kind": "Route","name": "syndesis"}}`,
			},
		},
	}

	return &sa
}

func addRouteAnnotation(syndesis *v1beta1.Syndesis, route Conduit) {
	annotations := syndesis.ObjectMeta.Annotations
	if annotations == nil {
		annotations = make(map[string]string)
		syndesis.ObjectMeta.Annotations = annotations
	}
	annotations["syndesis.io/applicationUrl"] = route.ExtractApplicationUrl()
}

func installSyndesisRoute(ctx context.Context, cl client.Client, syndesis *v1beta1.Syndesis, objects []runtime.Object) (Conduit, error) {
	conduit, err := findSyndesisRoute(objects)
	if err != nil {
		return nil, err
	}

	ct := conduit.Target()
	cm := conduit.Meta()

	operation.SetNamespaceAndOwnerReference(ct, syndesis)

	// We don't replace the route if already present, to let OpenShift generate its host
	o, _, err := util.CreateOrUpdate(ctx, cl, ct)
	if err != nil {
		return nil, err
	}
	cm.SetUID(o.GetUID())

	if len(conduit.Host()) > 0 {
		return conduit, nil
	}

	// Let's try to get the route from cluster to check the host field
	var key client.ObjectKey
	if key, err = client.ObjectKeyFromObject(ct); err != nil {
		return nil, err
	}

	err = cl.Get(ctx, key, ct)
	if err != nil {
		return nil, err
	}

	if len(conduit.Host()) == 0 {
		return nil, errors.New("hostname still not present on syndesis route")
	}

	return conduit, nil
}

func findSyndesisRoute(resources []runtime.Object) (Conduit, error) {
	for _, res := range resources {
		if route, ok := isSyndesisRoute(res); ok {
			return route, nil
		}
	}
	return nil, errors.New("syndesis route not found")
}

func isSyndesisRoute(resource runtime.Object) (Conduit, bool) {
	return ConduitWithName(resource, SyndesisRouteName)
}

func linkImageSecretToServiceAccounts(ctx context.Context, cl client.Client, syndesis *v1beta1.Syndesis, secret *corev1.Secret) error {
	// Link the builder service account to the image pull/push secret if it exists
	builder := &corev1.ServiceAccount{}
	err := cl.Get(ctx, types.NamespacedName{Namespace: syndesis.Namespace, Name: "builder"}, builder)
	if err != nil {
		return err
	}
	linked := linkImagePullSecret(builder, secret)
	linked = linkSecret(builder, secret.Name) || linked
	if linked {
		err = cl.Update(ctx, builder)
		if err != nil {
			return err
		}
	}
	return nil
}

func linkImagePullSecret(sa *corev1.ServiceAccount, secret *corev1.Secret) bool {
	exist := false
	for _, s := range sa.ImagePullSecrets {
		if s.Name == secret.Name {
			exist = true
			break
		}
	}

	if !exist {
		sa.ImagePullSecrets = append(sa.ImagePullSecrets, corev1.LocalObjectReference{
			Name: secret.Name,
		})
		return true
	}

	return false
}

func linkSecret(sa *corev1.ServiceAccount, secret string) bool {
	exist := false
	for _, s := range sa.Secrets {
		if s.Name == secret {
			exist = true
			break
		}
	}

	if !exist {
		sa.Secrets = append(sa.Secrets, corev1.ObjectReference{Namespace: sa.Namespace, Name: SyndesisPullSecret})
		return true
	}

	return false
}
