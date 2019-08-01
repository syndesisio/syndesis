package install

import (
	"fmt"
	"github.com/pkg/errors"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"strconv"
	"time"
)

func (o *Install) installClusterResources() error {

	resources, err := o.render("./install/cluster.yml")
	if err != nil {
		return err
	}

	if o.ejectedResources != nil {
		o.ejectedResources = append(o.ejectedResources, resources...)
	} else {
		// Installing CRDs needs admin rights, so skip this step if possible...
		crds := toGroupVersionKindMap(resources)
		if err := o.removeInstalledCRDs(crds); err != nil {
			return o.cleanUpCrdError(err)
		}
		if len(crds) == 0 {
			o.Println("shared resources were previously installed")
		} else {
			return util.RunAsMinishiftAdminIfPossible(o.GetClientConfig(), func() error {
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
	}

	return nil
}

// waitForCustomResourceDefinitions waits until all crds are installed
func (o *Install) waitForCustomResourceDefinitions(timeout time.Duration, crds map[v1.GroupVersionKind]bool) error {
	waintingOn := map[v1.GroupVersionKind]bool{}
	for k, v := range crds {
		waintingOn[k] = v
	}

	deadline := time.Now().Add(timeout)
	for {
		err := o.removeInstalledCRDs(waintingOn)
		if err != nil {
			return err
		}
		if len(waintingOn) == 0 {
			return nil
		}

		// Check after 2 seconds if not expired
		if time.Now().After(deadline) {
			return errors.New("cannot check CRD installation after " + strconv.FormatInt(timeout.Nanoseconds()/1000000000, 10) + " seconds")
		}

		o.Println("waiting for syndesis crds to be ready...")
		time.Sleep(2 * time.Second)
	}
}

func toGroupVersionKindMap(resources []unstructured.Unstructured) map[v1.GroupVersionKind]bool {
	waintingOn := map[v1.GroupVersionKind]bool{}
	for _, res := range resources {
		if res.GetKind() == "CustomResourceDefinition" {
			gvk := v1.GroupVersionKind{
				Group:   util.MustRenderGoTemplate("{{.spec.group}}", res.Object),
				Version: util.MustRenderGoTemplate("{{.spec.version}}", res.Object),
				Kind:    util.MustRenderGoTemplate("{{.spec.names.kind}}", res.Object),
			}
			waintingOn[gvk] = true
		}
	}
	return waintingOn
}

func (o *Install) removeInstalledCRDs(crds map[v1.GroupVersionKind]bool) error {
	for crd, _ := range crds {
		if found, err := o.IsCRDInstalled(crd); err != nil {
			return err
		} else if found {
			delete(crds, crd)
		}
	}
	return nil
}

// IsCRDInstalled check if the given CRD kind is installed
func (o *Install) IsCRDInstalled(crd v1.GroupVersionKind) (bool, error) {
	api, err := o.NewApiClient()
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
