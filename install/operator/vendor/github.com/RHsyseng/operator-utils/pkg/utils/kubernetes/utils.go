package kubernetes

import (
	"errors"
	"k8s.io/apimachinery/pkg/runtime/schema"
	"k8s.io/client-go/discovery"
	"sigs.k8s.io/controller-runtime/pkg/client/config"
)

func CustomResourceDefinitionExists(gvk schema.GroupVersionKind) error {
	cfg, err := config.GetConfig()
	if err != nil {
		return err
	}
	client, err := discovery.NewDiscoveryClientForConfig(cfg)
	if err != nil {
		return err
	}
	return customResourceDefinitionExists(gvk, client)
}

func customResourceDefinitionExists(gvk schema.GroupVersionKind, client discovery.ServerResourcesInterface) error {
	api, err := client.ServerResourcesForGroupVersion(gvk.GroupVersion().String())
	if err != nil {
		return err
	}
	for _, a := range api.APIResources {
		if a.Kind == gvk.Kind {
			return nil
		}
	}
	return errors.New(gvk.String() + " Kind not found ")
}
