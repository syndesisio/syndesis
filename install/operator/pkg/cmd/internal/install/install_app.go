package install

import (
	"context"
	"encoding/json"
	"time"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime/schema"
	"k8s.io/client-go/dynamic"
)

func (o *Install) installApplication() error {

	resources := make([]unstructured.Unstructured, 0)
	if o.customResource == "" {
		appResources, err := o.render("./install/app.yml.tmpl")
		if err != nil {
			return err
		}

		resources = append(resources, appResources...)

	} else {
		customResData, err := util.LoadJsonFromFile(o.customResource)
		if err != nil {
			return err
		}

		//
		// check the customResource can parse to syndesis type
		//
		syndesis := &v1beta1.Syndesis{}
		err = json.Unmarshal(customResData, syndesis)
		if err != nil {
			return err
		}

		resource, err := util.LoadUnstructuredObject(customResData)
		if err != nil {
			return err
		}

		resources = append(resources, *resource)
	}

	if o.ejectedResources != nil {
		o.ejectedResources = append(o.ejectedResources, resources...)
	} else {
		for _, res := range resources {
			res.SetNamespace(o.Namespace)
		}
		err := o.install("syndesis application was", resources)
		if err != nil {
			return err
		}

		client, err := o.ClientTools().DynamicClient()
		if err != nil {
			return err
		}

		if o.wait {
			for {
				o.Println("waiting for syndesis application deployment to be ready...")
				ready, err := WaitForSyndesisReady(o.Context, client, o.Namespace, "app", 5*time.Second)

				if err != nil {
					return err
				}
				if ready {
					o.Println("syndesis application deployment is ready")
					return nil
				}
			}
		}

	}
	return nil
}

func WaitForSyndesisReady(ctx context.Context, client dynamic.Interface, namespace string, name string, timeout time.Duration) (bool, error) {
	gvr := schema.GroupVersionResource{
		Group:    "syndesis.io",
		Version:  "v1beta1",
		Resource: "syndesises",
	}
	return util.WaitForResourceCondition(ctx, client, gvr, namespace, name, timeout, func(resource *unstructured.Unstructured) (bool, error) {
		phase := util.MustRenderGoTemplate("{{.status.phase}}", resource.Object)
		return phase == "Installed", nil
	})
}
