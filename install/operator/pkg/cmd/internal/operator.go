package internal

import (
	"context"

	"github.com/spf13/cobra"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
)

type Options struct {
	KubeConfig string
	Namespace  string

	Context     context.Context
	Command     *cobra.Command
	clientTools *clienttools.ClientTools
}

func (o *Options) ClientTools() *clienttools.ClientTools {
	if o.clientTools == nil {
		ct := clienttools.ClientTools{}
		o.clientTools = &ct
	}
	return o.clientTools
}
