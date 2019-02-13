// Register all Openshift types that we want to manage.
package openshift

import (
	apps "github.com/openshift/api/apps/v1"
	authorization "github.com/openshift/api/authorization/v1"
	build "github.com/openshift/api/build/v1"
	image "github.com/openshift/api/image/v1"
	oauthv1 "github.com/openshift/api/oauth/v1"
	route "github.com/openshift/api/route/v1"
	template "github.com/openshift/api/template/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/runtime/schema"
	cgoscheme "k8s.io/client-go/kubernetes/scheme"
	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
)

var log = logf.Log.WithName("openshift")


func init() {
	util.RegisterInScheme(registerInScheme)
}

func registerInScheme(scheme *runtime.Scheme) {

	// Add the standard kubernetes [GVK:Types] type registry
	// e.g (v1,Pods):&v1.Pod{}
	log.Info("Registering schemes")
	metav1.AddToGroupVersion(scheme, schema.GroupVersion{Version: "v1"})
	if err := cgoscheme.AddToScheme(scheme); err != nil {
		log.Error(err, "Cannot register kubernetes scheme")
	}
	if err := addKnownTypes(scheme); err != nil {
		log.Error(err, "Cannot add custom schemes")
	}
}

var (
	AddToScheme = addKnownTypes
)

type registerFunction func(*runtime.Scheme) error

func addKnownTypes(scheme *runtime.Scheme) error {

	var err error

	// Standardized groups
	err = doAdd(apps.AddToScheme, scheme, err)
	err = doAdd(template.AddToScheme, scheme, err)
	err = doAdd(image.AddToScheme, scheme, err)
	err = doAdd(route.AddToScheme, scheme, err)
	err = doAdd(build.AddToScheme, scheme, err)
	err = doAdd(authorization.AddToScheme, scheme, err)

	// Legacy "oapi" resources
	err = doAdd(apps.AddToSchemeInCoreGroup, scheme, err)
	err = doAdd(template.AddToSchemeInCoreGroup, scheme, err)
	err = doAdd(image.AddToSchemeInCoreGroup, scheme, err)
	err = doAdd(route.AddToSchemeInCoreGroup, scheme, err)
	err = doAdd(build.AddToSchemeInCoreGroup, scheme, err)
	err = doAdd(authorization.AddToSchemeInCoreGroup, scheme, err)
	err = doAdd(oauthv1.AddToScheme, scheme, err)

	return err
}

func doAdd(addToScheme registerFunction, scheme *runtime.Scheme, err error) error {
	callErr := addToScheme(scheme)
	if callErr != nil {
		log.Error(callErr, "Error while registering Openshift types")
	}

	if err == nil {
		return callErr
	}
	return err
}
