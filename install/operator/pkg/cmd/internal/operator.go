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

	osappsv1 "github.com/openshift/api/apps/v1"
	"k8s.io/client-go/kubernetes/scheme"
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

		//
		// Add schemes that the client should be capable of retrieving
		// scheme.Scheme provides most of the fundamental types
		// whilst runtime.Scheme is the empty equivalent.
		//
		s := scheme.Scheme

		// Openshift types such as DeploymentConfig
		osappsv1.AddToScheme(s)

		// Register
		options := client.Options{
			Scheme: s,
		}

		cl, err := client.New(o.GetClientConfig(), options)
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

func (o *Options) NewAPIClient() (*kubernetes.Clientset, error) {
	return kubernetes.NewForConfig(o.GetClientConfig())
}
