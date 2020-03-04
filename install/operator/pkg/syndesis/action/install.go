package action

import (
	"context"
	"errors"
	"fmt"
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
	"k8s.io/apimachinery/pkg/util/wait"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"

	"k8s.io/client-go/kubernetes"

	appsv1 "github.com/openshift/api/apps/v1"
	v1 "github.com/openshift/api/route/v1"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
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

func newInstallAction(mgr manager.Manager, api kubernetes.Interface) SyndesisOperatorAction {
	return &installAction{
		newBaseAction(mgr, api, "install"),
	}
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

	// Load configuration to to use as context for generate pkg
	config, err := configuration.GetProperties(configuration.TemplateConfig, ctx, a.client, syndesis)
	if err != nil {
		return err
	}

	// Check if an image secret exists, to be used to connect to registries that require authentication
	secret := &corev1.Secret{}
	err = a.client.Get(ctx, types.NamespacedName{Namespace: syndesis.Namespace, Name: SyndesisPullSecret}, secret)
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

	serviceAccount, err := installServiceAccount(ctx, a.client, syndesis, secret)
	if err != nil {
		return err
	}
	resourcesThatShouldExist[serviceAccount.GetUID()] = true

	token, err := serviceaccount.GetServiceAccountToken(ctx, a.client, serviceAccount.Name, syndesis.Namespace)
	if err != nil {
		return err
	}
	config.OpenShiftOauthClientSecret = token

	if err := config.ExternalDatabase(ctx, a.client, syndesis); err != nil {
		return err
	}

	// Render the route resource...
	all, err := generator.RenderDir("./route/", config)
	if err != nil {
		return err
	}

	routes, _ := util.SeperateStructuredAndUnstructured(a.scheme, all)
	syndesisRoute, err := installSyndesisRoute(ctx, a.client, syndesis, routes)
	if err != nil {
		return err
	}
	if err := config.SetRoute(ctx, a.client, syndesis); err != nil {
		return err
	}

	resourcesThatShouldExist[syndesisRoute.GetUID()] = true

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

	addons := configuration.GetAddons(*config)
	for _, addon := range addons {
		if !addon.Enabled {
			continue
		}

		addonDir := "./addons/" + addon.Name + "/"
		f, err := generator.GetAssetsFS().Open(addonDir)
		if err != nil {
			a.log.Info("unsupported addon configured", "addon", addon.Name, "error", err)
			continue
		}
		f.Close()

		resources, err := generator.RenderDir(addonDir, config)
		if err != nil {
			return err
		}

		all = append(all, resources...)
	}

	// Link the image secret to service accounts
	if secret != nil {
		err = linkImageSecretToServiceAccounts(ctx, a.client, syndesis, secret)
		if err != nil {
			return err
		}
	}

	// Install the resources..
	for _, res := range all {
		operation.SetNamespaceAndOwnerReference(res, syndesis)
		o, modificationType, err := util.CreateOrUpdate(ctx, a.client, &res)
		if err != nil {
			if util.IsNoKindMatchError(err) {
				gvk := res.GroupVersionKind()
				if _, found := kindsReportedNotAvailable[gvk]; !found {
					kindsReportedNotAvailable[gvk] = time.Now()
					a.log.Info("optional custom resource definition is not installed.", "group", gvk.Group, "version", gvk.Version, "kind", gvk.Kind)
				}
			} else {
				a.log.Info("failed to create or replace resource", "kind", res.GetKind(), "name", res.GetName(), "namespace", res.GetNamespace())
				return err
			}
		} else {
			resourcesThatShouldExist[o.GetUID()] = true
			if modificationType != controllerutil.OperationResultNone {
				a.log.Info("resource "+string(modificationType), "kind", res.GetKind(), "name", res.GetName(), "namespace", res.GetNamespace())
			}
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
	err = ListAllTypesInChunks(ctx, a.api, a.client, options, func(list []unstructured.Unstructured) error {
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
			err := a.client.Delete(ctx, &res)
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
		_, _, err := util.CreateOrUpdate(ctx, a.client, target, "kind", "apiVersion")
		if err != nil {
			return err
		}

		a.log.Info("Syndesis resource installed", "name", target.Name)
	} else if syndesis.Status.Phase == v1beta1.SyndesisPhasePostUpgradeRun {
		a.removePostgresUpgradeTrigger(ctx, syndesis)

		// Installation completed, set the next state
		target.Status.Phase = v1beta1.SyndesisPhasePostUpgradeRunSucceed
		target.Status.Reason = v1beta1.SyndesisStatusReasonMissing
		target.Status.Description = ""
		_, _, err := util.CreateOrUpdate(ctx, a.client, target, "kind", "apiVersion")
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

func addRouteAnnotation(syndesis *v1beta1.Syndesis, route *v1.Route) {
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

func installSyndesisRoute(ctx context.Context, cl client.Client, syndesis *v1beta1.Syndesis, objects []runtime.Object) (*v1.Route, error) {
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
	if key, err = client.ObjectKeyFromObject(route); err != nil {
		return nil, err
	}
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

//
// After the syndesis-db pod starts and has finished upgrading to the version consistent with the image
// of the container then the POSTGRESQL_UPGRADE=copy environment variable needs to be removed.
//
func (a *installAction) removePostgresUpgradeTrigger(context context.Context, syndesis *v1beta1.Syndesis) {
	pollTimeout := 600 * time.Second
	pollInterval := 5 * time.Second

	wait.Poll(pollInterval, pollTimeout, func() (done bool, err error) {
		dc := &appsv1.DeploymentConfig{}
		if err := a.client.Get(context, types.NamespacedName{Namespace: syndesis.Namespace, Name: "syndesis-db"}, dc); err != nil {
			a.log.Error(err, "getting `syndesis-db` DeploymentConfig in "+syndesis.Namespace)
			return false, err
		}

		if dc.Spec.Replicas != dc.Status.ReadyReplicas {
			return false, nil
		}

		for containerIdx, c := range dc.Spec.Template.Spec.Containers {
			if c.Name != "postgresql" {
				continue
			}

			for envIdx, e := range c.Env {
				if e.Name != "POSTGRESQL_UPGRADE" {
					continue
				}

				removeUpgradeEnvVar := client.ConstantPatch(types.JSONPatchType, []byte(fmt.Sprintf(`[{
					"op": "remove",
					"path": "/spec/template/spec/containers/%d/env/%d"
				}]`, containerIdx, envIdx)))
				if err := a.client.Patch(context, dc, removeUpgradeEnvVar); err != nil {
					a.log.Error(err, "patching `syndesis-db` DeploymentConfig to remove `POSTGRESQL_UPGRADE` environment variable")
					return true, err
				}
				break
			}
			break
		}

		return true, nil
	})
}
