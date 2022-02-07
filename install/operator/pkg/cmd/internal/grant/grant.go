/*
 * Copyright (C) 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grant

import (
	"fmt"

	"github.com/operator-framework/operator-sdk/pkg/log/zap"
	"github.com/pkg/errors"
	"github.com/spf13/cobra"

	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/capabilities"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
)

const RoleName = "syndesis-installer"

type Grant struct {
	*internal.Options
	Role      string
	Kind      string
	User      string
	cluster   bool
	ApiServer capabilities.ApiServerSpec
}

func New(parent *internal.Options) *cobra.Command {
	o := Grant{Options: parent}
	cmd := cobra.Command{
		Use:   "grant",
		Short: "grants permissions needed to run the operator(requires namespace admin privileges or cluster admin privileges)",
		Run: func(_ *cobra.Command, _ []string) {
			util.ExitOnError(o.grant())
		},
	}

	cmd.PersistentFlags().BoolVarP(&o.cluster, "cluster", "", false, "add the permission for all projects in the cluster(requires cluster admin privileges)")
	cmd.PersistentFlags().StringVarP(&o.User, "user", "u", "", "add permissions for the given User")
	cmd.PersistentFlags().AddFlagSet(zap.FlagSet())
	cmd.PersistentFlags().AddFlagSet(util.FlagSet)
	cobra.MarkFlagRequired(cmd.PersistentFlags(), "user")

	return &cmd
}

func (o *Grant) grant() error {

	apiServer, err := capabilities.ApiCapabilities(o.ClientTools())
	if err != nil {
		return err
	}

	o.ApiServer = *apiServer
	o.Role = RoleName

	o.Kind = "ClusterRole"
	if o.cluster == false {
		o.Kind = "Role"
	}

	resources, err := generator.Render("assets/install/role.yml.tmpl", o)
	if err != nil {
		return err
	}

	gr, err := generator.Render("assets/install/grant/grant_role.yml.tmpl", o)
	if err != nil {
		return err
	}
	resources = append(resources, gr...)

	// Allow syndesis-server user to lookup kafka customresources at cluster level
	kafka, err := generator.Render("assets/install/cluster_role_kafka.yml.tmpl", o)
	if err != nil {
		return err
	}
	resources = append(resources, kafka...)

	grkafka, err := generator.Render("assets/install/grant/grant_cluster_role_kafka.yml.tmpl", o)
	if err != nil {
		return err
	}
	resources = append(resources, grkafka...)

	//
	// Create & bind the cluster role for reading
	// operation-lifecycle-manager artifacts if they are available
	// If not available then resources will be empty
	//
	olm, err := generator.Render("assets/install/cluster_role_olm.yml.tmpl", o)
	if err != nil {
		return err
	}
	resources = append(resources, olm...)

	grolm, err := generator.Render("assets/install/grant/grant_cluster_role_olm.yml.tmpl", o)
	if err != nil {
		return err
	}
	resources = append(resources, grolm...)

	//
	// Will only render anything if there is NOT olm support
	//
	jaeger, err := generator.Render("assets/install/cluster_role_jaeger.yml.tmpl", o)
	if err != nil {
		return err
	}
	resources = append(resources, jaeger...)

	grjaeger, err := generator.Render("assets/install/grant/grant_cluster_role_jaeger.yml.tmpl", o)
	if err != nil {
		return err
	}
	resources = append(resources, grjaeger...)

	pubapi, err := generator.Render("assets/install/cluster_role_public_api.yml.tmpl", o)
	if err != nil {
		return err
	}
	resources = append(resources, pubapi...)

	grpubapi, err := generator.Render("assets/install/grant/grant_cluster_role_public_api.yml.tmpl", o)
	if err != nil {
		return err
	}
	resources = append(resources, grpubapi...)

	client, _ := o.ClientTools().RuntimeClient()
	for _, res := range resources {
		res.SetNamespace(o.Namespace)

		_, _, err := util.CreateOrUpdate(o.Context, client, &res)
		if err != nil {
			return errors.Wrap(err, util.Dump(res))
		}
	}

	fmt.Println("role", o.Role, "granted to", o.User)

	return nil
}
