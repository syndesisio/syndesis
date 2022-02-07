package install

import (
	"fmt"
	"strconv"
	"time"

	"github.com/pkg/errors"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apiextensions-apiserver/pkg/apis/apiextensions"
	apiextensionsv1 "k8s.io/apiextensions-apiserver/pkg/apis/apiextensions/v1"
	apiextensionsv1beta1 "k8s.io/apiextensions-apiserver/pkg/apis/apiextensions/v1beta1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	v1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime/schema"
)

func (o *Install) installClusterResources() error {

	resources, err := o.render("assets/install/cluster")
	if err != nil {
		return err
	}

	//
	// Determine if the extensions v1 API is supported (openshift 3.11 does not)
	//
	isApiExtensionsV1, err := o.isExtensionsV1APISupported()
	if err != nil {
		return err
	}

	if !isApiExtensionsV1 {
		//
		// Not supported so downgrade the extension v1 API to v1beta1
		//
		resources, err = o.downgradeApiExtensions(resources)
		if err != nil {
			return err
		}
	}

	if o.ejectedResources != nil {
		o.ejectedResources = append(o.ejectedResources, resources...)
	} else {
		crds := toGroupVersionKindMap(resources)
		return util.RunAsMinishiftAdminIfPossible(o.ClientTools().RestConfig(), func() error {
			err := o.install("cluster resources were", resources)

			if err != nil {
				return err
			}

			// Wait for all CRDs to be installed before proceeding
			if err := o.waitForCustomResourceDefinitions(25*time.Second, crds); err != nil {
				return o.cleanUpCrdError(err)
			}
			return nil
		})
	}

	return nil
}

// waitForCustomResourceDefinitions waits until all crds are installed
func (o *Install) waitForCustomResourceDefinitions(timeout time.Duration, crds map[v1.GroupVersionKind]bool) error {
	waitingOn := map[v1.GroupVersionKind]bool{}
	for k, v := range crds {
		waitingOn[k] = v
	}

	deadline := time.Now().Add(timeout)
	for {
		count := 0
		for w, served := range waitingOn {
			installed, err := o.IsCRDInstalled(w)
			if err != nil {
				return err
			}

			//
			// Add to count if CRD is now installed
			// or CRD is not served
			//
			if installed || !served {
				count++
			}
		}

		if count == len(waitingOn) {
			return nil
		}

		//
		// Check after 2 seconds if not expired
		//

		if time.Now().After(deadline) {
			return errors.New("Timed out waiting on CRD installation after " + strconv.FormatInt(timeout.Nanoseconds()/1000000000, 10) + " seconds")
		}

		o.Println("waiting for syndesis crds to be ready...")
		time.Sleep(2 * time.Second)
	}
}

func toGroupVersionKindMap(resources []unstructured.Unstructured) map[v1.GroupVersionKind]bool {
	waintingOn := map[v1.GroupVersionKind]bool{}
	for _, res := range resources {
		if res.GetKind() == "CustomResourceDefinition" {

			versions, _, _ := unstructured.NestedSlice(res.UnstructuredContent(), "spec", "versions")
			for _, version := range versions {

				v, _ := version.(map[string]interface{})

				gvk := v1.GroupVersionKind{
					Group:   util.MustRenderGoTemplate("{{.spec.group}}", res.Object),
					Version: v["name"].(string),
					Kind:    util.MustRenderGoTemplate("{{.spec.names.kind}}", res.Object),
				}

				//
				// Served determines whether a CRD version is visible & can be discovered by the api.Discovery client
				//
				waintingOn[gvk] = v["served"].(bool)
			}
		}
	}
	return waintingOn
}

func (o *Install) isExtensionsV1APISupported() (bool, error) {
	api, err := o.ClientTools().ApiClient()
	if err != nil {
		return false, err
	}

	r, _ := api.Discovery().ServerResourcesForGroupVersion("apiextensions.k8s.io/v1")
	if r == nil || r.Size() == 0 {
		return false, nil
	}

	return true, nil
}

func (o *Install) downgradeApiExtensions(resources []unstructured.Unstructured) ([]unstructured.Unstructured, error) {
	downgraded := []unstructured.Unstructured{}
	scheme := o.ClientTools().GetScheme()

	err := apiextensionsv1.AddToScheme(scheme)
	if err != nil {
		return downgraded, err
	}

	err = apiextensionsv1beta1.AddToScheme(scheme)
	if err != nil {
		return downgraded, err
	}

	for _, resource := range resources {

		object, err := util.RuntimeObjectFromUnstructured(scheme, &resource)
		if err != nil {
			return downgraded, err
		}

		v1Crd := object.(*apiextensionsv1.CustomResourceDefinition)

		//
		// v1 CRDs have the latest version at the END of the versions array
		// v1beta1 conversion takes the FIRST version from the versions array and
		// sets it as the deprecated spec.version attribute
		//
		// If CRD has multiple versions, reverse them so the correct version is chosen.
		//
		reverseVersions(v1Crd)

		v1beta1Crd := &apiextensionsv1beta1.CustomResourceDefinition{}
		crd := &apiextensions.CustomResourceDefinition{}

		err = apiextensionsv1.Convert_v1_CustomResourceDefinition_To_apiextensions_CustomResourceDefinition(v1Crd, crd, nil)
		if err != nil {
			return downgraded, err
		}

		err = apiextensionsv1beta1.Convert_apiextensions_CustomResourceDefinition_To_v1beta1_CustomResourceDefinition(crd, v1beta1Crd, nil)
		if err != nil {
			return downgraded, err
		}

		//
		// This metadata seems to be missed off of the final converted object
		//
		v1beta1Crd.SetGroupVersionKind(
			schema.GroupVersionKind{
				Group:   "apiextensions.k8s.io",
				Version: "v1beta1",
				Kind:    "CustomResourceDefinition",
			})

		dgdUns, err := util.ToUnstructured(v1beta1Crd)
		if err != nil {
			return downgraded, err
		}

		downgraded = append(downgraded, *dgdUns)
	}

	return downgraded, nil
}

func reverseVersions(crd *apiextensionsv1.CustomResourceDefinition) {

	if len(crd.Spec.Versions) < 2 {
		return // nothing to do
	}

	versions := make([]apiextensionsv1.CustomResourceDefinitionVersion, len(crd.Spec.Versions))
	copy(versions, crd.Spec.Versions)

	for i := len(versions)/2 - 1; i >= 0; i-- {
		opp := len(versions) - 1 - i
		versions[i], versions[opp] = versions[opp], versions[i]
	}

	crd.Spec.Versions = versions
}

// IsCRDInstalled check if the given CRD kind is installed
func (o *Install) IsCRDInstalled(crd v1.GroupVersionKind) (bool, error) {
	api, err := o.ClientTools().ApiClient()
	if err != nil {
		return false, err
	}

	lst, err := api.Discovery().ServerResourcesForGroupVersion(crd.Group + "/" + crd.Version)
	if err != nil && k8serrors.IsNotFound(err) {
		return false, nil
	} else if err != nil {
		return false, err
	}
	for _, res := range lst.APIResources {
		if res.Kind == crd.Kind {
			return true, nil
		}
	}
	return false, nil
}

func (o *Install) cleanUpCrdError(err error) error {
	if err != nil && k8serrors.IsForbidden(err) {
		fmt.Println("current user is not authorized to create cluster-wide objects like custom resource definitions or cluster roles: ", err)
		meg := fmt.Sprintf(`please login as cluster-admin and execute "%s install shared" to install cluster-wide resources (one-time operation)`, o.Command.Use)
		return errors.New(meg)
	} else if err != nil {
		return err
	}
	return nil
}
