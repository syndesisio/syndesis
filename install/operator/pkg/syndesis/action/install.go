package action

import (
	"context"
	"errors"
	"time"

	"github.com/syndesisio/syndesis/install/operator/pkg"
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

	v1 "github.com/openshift/api/route/v1"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta2"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/olm"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/operation"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

const (
	SyndesisRouteName  = "syndesis"
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

func (a *installAction) installResource(ctx context.Context, rtClient client.Client, syndesis *v1beta2.Syndesis, res unstructured.Unstructured) (*unstructured.Unstructured, error) {
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

//
// Log the possible combination of values chosen for the db persistent volume claim
//
func (a *installAction) logResourcePersistentVolume(syndesis *v1beta2.Syndesis, componentName string, resourceVolume configuration.ResourcesWithPersistentVolume) {
	if syndesisPhaseIs(syndesis, v1beta2.SyndesisPhaseInstalling) {
		a.log.V(pkg.DEBUG_LOGGING_LVL).Info("Binding to persistent volume with criteria ",
			"component", componentName,
			"volume-access-mode", resourceVolume.VolumeAccessMode,
			"volume-name", resourceVolume.VolumeName,
			"storage-class", resourceVolume.VolumeStorageClass)
	}
}

func (a *installAction) CanExecute(syndesis *v1beta2.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		v1beta2.SyndesisPhaseInstalling,
		v1beta2.SyndesisPhaseInstalled,
		v1beta2.SyndesisPhasePostUpgradeRun,
		v1beta2.SyndesisPhaseStarting,
		v1beta2.SyndesisPhaseStartupFailed,
	)
}

var kindsReportedNotAvailable = map[schema.GroupVersionKind]time.Time{}

func (a *installAction) Execute(ctx context.Context, syndesis *v1beta2.Syndesis) error {
	if syndesisPhaseIs(syndesis, v1beta2.SyndesisPhaseInstalling) {
		a.log.Info("installing Syndesis resource", "name", syndesis.Name)
	} else if syndesisPhaseIs(syndesis, v1beta2.SyndesisPhasePostUpgradeRun) {
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

	if err := config.SetRoute(ctx, rtClient, syndesis); err != nil {
		return err
	}

	resourcesThatShouldExist[syndesisRoute.GetUID()] = true

	// Render the remaining syndesis resources...
	all, err = generator.RenderDir("./infrastructure/", config)
	if err != nil {
		return err
	}

	a.logResourcePersistentVolume(syndesis, "syndesis-meta", config.Syndesis.Components.Meta.Resources)
	a.logResourcePersistentVolume(syndesis, "syndesis-prometheus", config.Syndesis.Components.Prometheus.Resources)

	// Render the database resource if needed...
	if syndesis.Spec.Components.Database.ExternalDbURL == "" {
		dbResources, err := generator.RenderDir("./database/", config)
		if err != nil {
			return err
		}

		a.logResourcePersistentVolume(syndesis, "syndesis-db", config.Syndesis.Components.Database.Resources)

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
		err = PreProcessForAffinityTolerations(ctx, rtClient, syndesis, &res)
		if err != nil {
			return err // Fail-fast for core components
		}

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

		a.log.V(pkg.DEBUG_LOGGING_LVL).Info("Installing addon", "Name", addonInfo.Name())

		if config.ApiServer.OlmSupport && addonInfo.GetOlmSpec() != nil && addonInfo.GetOlmSpec().Package != "" {
			a.log.V(pkg.DEBUG_LOGGING_LVL).Info("Subscribing to OLM operator:", "Package", addonInfo.GetOlmSpec().Package, "Channel", addonInfo.GetOlmSpec().Channel)
			//
			// Using the operator hub is not mutually exclusive to loading the addon
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
			a.log.Error(err, "Unsupported addon configured", "addon", addonInfo.Name())
			continue
		}
		f.Close()

		resources, err := generator.RenderDir(addonDir, config)
		if err != nil {
			a.log.Error(err, "Rendering of addon resources failed", "addon", addonInfo.Name())
			continue
		}

		//
		// Install the resources of this addon
		// If there is an error do NOT fail-fast but
		// try and continue to install the other addons
		//
		for _, res := range resources {
			err = PreProcessForAffinityTolerations(ctx, rtClient, syndesis, &res)
			if err != nil {
				a.log.Error(err, "Install of addon failed", "addon", addonInfo.Name())
				break
			}

			o, err := a.installResource(ctx, rtClient, syndesis, res)
			if err != nil {
				a.log.Error(err, "Install of addon failed", "addon", addonInfo.Name())
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
	if syndesis.Status.Phase == v1beta2.SyndesisPhaseInstalling {
		// Installation completed, set the next state
		target.Status.Phase = v1beta2.SyndesisPhaseStarting
		target.Status.Reason = v1beta2.SyndesisStatusReasonMissing
		target.Status.Description = ""
		_, _, err := util.CreateOrUpdate(ctx, rtClient, target, "kind", "apiVersion")
		if err != nil {
			return err
		}

		a.log.Info("Syndesis resource installed", "name", target.Name)
	} else if syndesis.Status.Phase == v1beta2.SyndesisPhasePostUpgradeRun {
		// Installation completed, set the next state
		target.Status.Phase = v1beta2.SyndesisPhasePostUpgradeRunSucceed
		target.Status.Reason = v1beta2.SyndesisStatusReasonMissing
		target.Status.Description = ""
		_, _, err := util.CreateOrUpdate(ctx, rtClient, target, "kind", "apiVersion")
		if err != nil {
			return err
		}

		a.log.Info("Syndesis resource installed after upgrading", "name", target.Name)
	}

	return nil
}

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

func installServiceAccount(ctx context.Context, cl client.Client, syndesis *v1beta2.Syndesis, secret *corev1.Secret) (*corev1.ServiceAccount, error) {
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

func addRouteAnnotation(syndesis *v1beta2.Syndesis, route *v1.Route) {
	annotations := syndesis.ObjectMeta.Annotations
	if annotations == nil {
		annotations = make(map[string]string)
		syndesis.ObjectMeta.Annotations = annotations
	}
	annotations["syndesis.io/applicationUrl"] = extractApplicationUrl(route)
}

func extractApplicationUrl(route *v1.Route) string {
	scheme := "http"
	if route.Spec.TLS != nil {
		scheme = "https"
	}
	return scheme + "://" + route.Spec.Host
}

func installSyndesisRoute(ctx context.Context, cl client.Client, syndesis *v1beta2.Syndesis, objects []runtime.Object) (*v1.Route, error) {
	route, err := findSyndesisRoute(objects)
	if err != nil {
		return nil, err
	}

	operation.SetNamespaceAndOwnerReference(route, syndesis)

	// We don't replace the route if already present, to let OpenShift generate its host
	o, _, err := util.CreateOrUpdate(ctx, cl, route)
	if err != nil {
		return nil, err
	}
	route.SetUID(o.GetUID())

	if route.Spec.Host != "" {
		return route, nil
	}

	// Let's try to get the route from OpenShift to check the host field
	var key client.ObjectKey
	key = client.ObjectKeyFromObject(route)
	err = cl.Get(ctx, key, route)
	if err != nil {
		return nil, err
	}

	if route.Spec.Host == "" {
		return nil, errors.New("hostname still not present on syndesis route")
	}
	return route, nil
}

func findSyndesisRoute(resources []runtime.Object) (*v1.Route, error) {
	for _, res := range resources {
		if route, ok := isSyndesisRoute(res); ok {
			return route, nil
		}
	}
	return nil, errors.New("syndesis route not found")
}

func isSyndesisRoute(resource runtime.Object) (*v1.Route, bool) {
	if route, ok := resource.(*v1.Route); ok {
		if route.Name == SyndesisRouteName {
			return route, true
		}
	}
	return nil, false
}

func linkImageSecretToServiceAccounts(ctx context.Context, cl client.Client, syndesis *v1beta2.Syndesis, secret *corev1.Secret) error {
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

func PreProcessForAffinityTolerations(ctx context.Context, cl client.Client, syndesis *v1beta2.Syndesis, resource *unstructured.Unstructured) error {
	kind := resource.GetKind()
	if kind != "DeploymentConfig" && kind != "Jaeger" {
		return nil // only deployment-configs and jaeger CR to be processed
	}

	if syndesis.Spec.InfraScheduling.Affinity == nil && len(syndesis.Spec.InfraScheduling.Tolerations) == 0 {
		return nil // No infra-scheduling defined so ignore
	}

	if syndesis.Spec.InfraScheduling.Affinity != nil {
		affinityData, err := runtime.DefaultUnstructuredConverter.ToUnstructured(syndesis.Spec.InfraScheduling.Affinity)
		if err != nil {
			return err
		}

		path := make([]string, 0)
		if kind == "DeploymentConfig" {
			path = append(path, "spec", "template")
		}

		// Jaeger does not need the same prefix
		path = append(path, "spec", "affinity")

		err = unstructured.SetNestedField(resource.UnstructuredContent(), affinityData, path...)
		if err != nil {
			return err
		}
	}

	if len(syndesis.Spec.InfraScheduling.Tolerations) > 0 {
		tolerations := make([]interface{}, len(syndesis.Spec.InfraScheduling.Tolerations))

		for i, toleration := range syndesis.Spec.InfraScheduling.Tolerations {
			tolerationData, err := runtime.DefaultUnstructuredConverter.ToUnstructured(&toleration)
			if err != nil {
				return err
			}
			tolerations[i] = tolerationData
		}

		path := make([]string, 0)
		if kind == "DeploymentConfig" {
			path = append(path, "spec", "template")
		}

		// Jaeger does not need the same prefix
		path = append(path, "spec", "tolerations")

		err := unstructured.SetNestedSlice(resource.UnstructuredContent(), tolerations, path...)
		if err != nil {
			return err
		}
	}

	return nil
}
