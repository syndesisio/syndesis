package internal

import (
	"context"
	"github.com/spf13/cobra"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/client-go/dynamic"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/client/config"
)

type Options struct {
	KubeConfig string
	Namespace  string

	Context context.Context
	Command *cobra.Command
	Client  *client.Client
}

func (o *Options) GetClientConfig() *rest.Config {
	c, err := config.GetConfig()
	util.ExitOnError(err)
	return c
}

func (o *Options) GetClient() (c client.Client, err error) {
	if o.Client == nil {
		cl, err := client.New(o.GetClientConfig(), client.Options{})
		if err != nil {
			return nil, err
		}
		o.Client = &cl
	}
	return *o.Client, nil
}

func (o *Options) NewDynamicClient() (c dynamic.Interface, err error) {
	return dynamic.NewForConfig(o.GetClientConfig())
}

func (o *Options) NewApiClient() (*kubernetes.Clientset, error) {
	return kubernetes.NewForConfig(o.GetClientConfig())
}
