// Register all OpenShift types that we want to manage.
package openshift

import (
	apps "github.com/openshift/api/apps/v1"
	authorization "github.com/openshift/api/authorization/v1"
	build "github.com/openshift/api/build/v1"
	image "github.com/openshift/api/image/v1"
	oauth "github.com/openshift/api/oauth/v1"
	route "github.com/openshift/api/route/v1"
	template "github.com/openshift/api/template/v1"
	"k8s.io/apimachinery/pkg/runtime"
	utilruntime "k8s.io/apimachinery/pkg/util/runtime"
)

var (
	AddToScheme = addKnownTypes
)

func addKnownTypes(scheme *runtime.Scheme) {

	// Standardized groups
	utilruntime.Must(apps.AddToScheme(scheme))
	utilruntime.Must(template.AddToScheme(scheme))
	utilruntime.Must(image.AddToScheme(scheme))
	utilruntime.Must(route.AddToScheme(scheme))
	utilruntime.Must(build.AddToScheme(scheme))
	utilruntime.Must(authorization.AddToScheme(scheme))
	utilruntime.Must(oauth.AddToScheme(scheme))

	//// Legacy "oapi" resources
	//err = doAdd(apps.AddToSchemeInCoreGroup, scheme, err)
	//err = doAdd(template.AddToSchemeInCoreGroup, scheme, err)
	//err = doAdd(image.AddToSchemeInCoreGroup, scheme, err)
	//err = doAdd(route.AddToSchemeInCoreGroup, scheme, err)
	//err = doAdd(build.AddToSchemeInCoreGroup, scheme, err)
	//err = doAdd(authorization.AddToSchemeInCoreGroup, scheme, err)
	//
}
