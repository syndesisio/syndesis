package util

import (
	"context"
	"fmt"
	"strconv"
	"time"

	v1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime/schema"
	"k8s.io/client-go/dynamic"
	"k8s.io/client-go/kubernetes"
	k8sscheme "k8s.io/client-go/kubernetes/scheme"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/clientcmd"
	clientcmdapi "k8s.io/client-go/tools/clientcmd/api"
	"k8s.io/client-go/tools/remotecommand"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

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
		Group:    "apps",
		Version:  "v1",
		Resource: "deployments",
	}
	return WaitForResourceCondition(ctx, client, gvr, namespace, name, timeout, func(resource *unstructured.Unstructured) (bool, error) {
		availableReplicas := MustRenderGoTemplate("{{.status.availableReplicas}}", resource.Object)
		if availableReplicas != "" && availableReplicas != "<no value>" {
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

func GetPodWithLabelSelector(api kubernetes.Interface, namespace string, LabelSelector string) (*v1.Pod, error) {
	podList, err := api.CoreV1().Pods(namespace).List(metav1.ListOptions{
		LabelSelector: LabelSelector,
	})
	if err != nil {
		return nil, err
	}
	switch len(podList.Items) {
	case 1:
		return &podList.Items[0], nil // All good..
	case 0:
		return nil, fmt.Errorf("syndesis-db pod is not running")
	default:
		return nil, fmt.Errorf("too many pods look like they could be the syndesis-db pod")
	}
}

func ListInChunks(ctx context.Context, c client.Client, options *client.ListOptions, list *unstructured.UnstructuredList, handler func([]unstructured.Unstructured) error) (err error) {
	for {
		if err := c.List(ctx, list, options); err != nil {
			return err
		}
		err = handler(list.Items)
		if err != nil {
			return err
		}

		if len(list.GetContinue()) == 0 {
			return
		}
		// keep loading....
		options.Raw.Continue = list.GetContinue()
	}

}

type ExecOptions struct {
	Config    *rest.Config
	Api       kubernetes.Interface
	Namespace string
	Pod       string
	Container string
	Command   []string
	remotecommand.StreamOptions
}

func Exec(o ExecOptions) error {
	req := o.Api.CoreV1().RESTClient().Post().
		Resource("pods").
		Name(o.Pod).
		Namespace(o.Namespace).
		SubResource("exec").
		VersionedParams(&v1.PodExecOptions{
			Container: o.Container,
			Command:   o.Command,
			Stdout:    o.Stdout != nil,
			Stderr:    o.Stderr != nil,
			Stdin:     o.Stdin != nil,
		}, k8sscheme.ParameterCodec)

	exec, err := remotecommand.NewSPDYExecutor(o.Config, "POST", req.URL())
	if err != nil {
		return err
	}
	return exec.Stream(o.StreamOptions)
}
