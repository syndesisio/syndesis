package util

import (
	"context"
	"fmt"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime/schema"
	"k8s.io/client-go/dynamic"
	"k8s.io/client-go/tools/clientcmd"
	clientcmdapi "k8s.io/client-go/tools/clientcmd/api"
	"os"
	"os/user"
	"path/filepath"
	"strconv"
	"time"
)

func KubeConfigPath() string {
	if len(os.Getenv("KUBECONFIG")) > 0 {
		return os.Getenv("KUBECONFIG")
	} else if usr, err := user.Current(); err == nil {
		return filepath.Join(usr.HomeDir, ".kube", "config")
	}
	return ""
}

func GetClientNamespace(configPath string) (string, error) {
	var clientConfig clientcmd.ClientConfig
	var apiConfig *clientcmdapi.Config
	var err error
	if configPath != "" {
		apiConfig, err = clientcmd.LoadFromFile(configPath)
		if err != nil {
			return "", fmt.Errorf("failed to load user provided kubeconfig: %v", err)
		}
	} else {
		apiConfig, err = clientcmd.NewDefaultClientConfigLoadingRules().Load()
		if err != nil {
			return "", fmt.Errorf("failed to get kubeconfig: %v", err)
		}
	}
	clientConfig = clientcmd.NewDefaultClientConfig(*apiConfig, &clientcmd.ConfigOverrides{})
	namespace, _, err := clientConfig.Namespace()
	if err != nil {
		return "", err
	}
	return namespace, nil
}

func WaitForDeploymentReady(ctx context.Context, client dynamic.Interface, namespace string, name string, timeout time.Duration) (bool, error) {
	gvr := schema.GroupVersionResource{
		Group:    "apps.openshift.io",
		Version:  "v1",
		Resource: "deploymentconfigs",
	}
	return WaitForResourceCondition(ctx, client, gvr, namespace, name, timeout, func(resource *unstructured.Unstructured) (bool, error) {
		availableReplicas := MustRenderGoTemplate("{{.status.availableReplicas}}", resource.Object)
		if availableReplicas != "" {
			i, err := strconv.Atoi(availableReplicas)
			if err != nil {
				return false, err
			}
			if i > 0 {
				return true, nil
			}
		}
		return false, nil
	})
}

func WaitForResourceCondition(ctx context.Context, client dynamic.Interface, gvr schema.GroupVersionResource, namespace string, name string, timeout time.Duration, condition func(resource *unstructured.Unstructured) (bool, error)) (bool, error) {
	options := metav1.ListOptions{FieldSelector: "metadata.name=" + name}
	r := client.Resource(gvr).Namespace(namespace)
	watcher, err := r.Watch(options)
	if err != nil {
		return false, err
	}

	defer watcher.Stop()
	events := watcher.ResultChan()

	if timeout >= 0 {
		go func() {
			time.Sleep(timeout)
			watcher.Stop()
		}()
	}

	for {
		select {
		case <-ctx.Done():
			return false, nil

		case e, ok := <-events:
			if !ok {
				return false, nil
			}
			if e.Object == nil {
				continue
			}
			if dc, ok := e.Object.(*unstructured.Unstructured); ok {
				satisfied, err := condition(dc)
				if err != nil {
					return false, err
				}
				if satisfied {
					return true, nil
				}
			}
		}
	}
}
