/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package cmd

import (
	"context"
	"flag"

	"github.com/spf13/cobra"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal/backup"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal/grant"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal/install"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal/restore"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal/run"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal/uninstall"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
)

// Creates a new operator command.
func NewOperator(ctx context.Context) (*cobra.Command, error) {
	options := internal.Options{
		Context: ctx,
	}
	var cmd = cobra.Command{
		// BashCompletionFunction: bashCompletionFunction,
		Use:   `operator`,
		Short: `the Syndesis operator`,
		Long:  `the Syndesis operator takes care of installing and running Syndesis on a cluster.`,
	}
	options.Command = &cmd

	// Lets rexport the flags installed by the controller runtime, and make them a little less kube specific
	f := *flag.CommandLine.Lookup("kubeconfig")
	f.Name = "config"
	f.Usage = "path to the config file to connect to the cluster"
	cmd.PersistentFlags().AddGoFlag(&f)

	f = *flag.CommandLine.Lookup("master")
	f.Usage = "the address of the cluster API server."
	cmd.PersistentFlags().AddGoFlag(&f)

	// cmd.PersistentFlags().StringVar(&options.KubeConfig, "config", , "path to the config file to connect to the cluster")
	namespace, _ := util.GetClientNamespace(options.KubeConfig)
	cmd.PersistentFlags().StringVarP(&options.Namespace, "namespace", "n", namespace, "namespace to run against")

	cmd.AddCommand(install.New(&options))
	cmd.AddCommand(grant.New(&options))
	cmd.AddCommand(run.New(&options))
	cmd.AddCommand(uninstall.New(&options))
	cmd.AddCommand(backup.New(&options))
	cmd.AddCommand(restore.New(&options))

	return &cmd, nil
}
