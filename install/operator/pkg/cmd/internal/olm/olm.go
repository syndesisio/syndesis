/*
 * Copyright (C) 2020 Red Hat, Inc.
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

package olm

import (
	"context"

	"github.com/spf13/cobra"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/olm"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
)

type Olm struct {
	*internal.Options
	Path     string
	Operator string
}

func New(parent *internal.Options) *cobra.Command {
	o := Olm{Options: parent}
	cmd := cobra.Command{
		Use:   "olm",
		Short: "generates bundle files for OLM installation",
		Run: func(_ *cobra.Command, _ []string) {
			util.ExitOnError(o.generate())
		},
	}

	cmd.PersistentFlags().StringVarP(&o.Path, "path", "p", ".", "Path where bundle files should be saved")
	cmd.PersistentFlags().StringVarP(&o.Path, "operator-image", "o", "syndesis/syndesis-operator:latest", "Syndesis operator docker image")
	cmd.PersistentFlags().StringVarP(&configuration.TemplateConfig, "operator-config", "", "/conf/config.yaml", "Path to the operator configuration file.")
	return &cmd
}

func (o *Olm) generate() (err error) {
	conf, err := configuration.GetProperties(context.TODO(), configuration.TemplateConfig, o.ClientTools(), &v1beta1.Syndesis{})
	if err != nil {
		return err
	}

	mg := olm.Build(conf, o.Path, o.Operator)
	err = mg.Generate()

	return
}
