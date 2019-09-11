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

package eject

import (
	"context"
	"encoding/json"
	"fmt"

	"github.com/operator-framework/operator-sdk/pkg/log/zap"

	"gopkg.in/yaml.v2"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"

	"github.com/spf13/cobra"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

type Eject struct {
	*internal.Options
	application string
	out         string
	// processing state
	ejectedResources []unstructured.Unstructured
}

func New(parent *internal.Options) *cobra.Command {
	e := Eject{Options: parent}
	cmd := cobra.Command{
		Use:               "eject",
		Long:              "parses the template files and eject the configuration that would be applied to the cluster in the specified format instead of installing the configuration. One of: json|yaml",
		Short:             "ejects syndesis app configuration",
		PersistentPreRunE: e.before,
		PersistentPostRun: e.after,
		Run: func(_ *cobra.Command, _ []string) {
			util.ExitOnError(e.eject())
		},
	}
	cmd.PersistentFlags().StringVarP(&e.application, "app", "a", "", "if provided, name of the custom resource to read the configuration from")
	cmd.PersistentFlags().StringVarP(&e.out, "out", "o", "yaml", "format for the configuration to be ejected. One of: json|yaml")
	cmd.PersistentFlags().StringVarP(&configuration.TemplateConfig, "operator-config", "", "/conf/config.yaml", "Path to the operator configuration file.")
	cmd.PersistentFlags().AddFlagSet(zap.FlagSet())

	return &cmd
}

func (e *Eject) before(_ *cobra.Command, args []string) (err error) {
	switch e.out {
	case "":
	case "yaml":
	case "json":
	default:
		return fmt.Errorf("invalid output format: %s", e.out)
	}

	if len(args) > 0 {
		return fmt.Errorf("unexpected argument: %s", args[0])
	}

	if e.out != "" {
		e.ejectedResources = []unstructured.Unstructured{}
	}

	return
}

func (e *Eject) after(cmd *cobra.Command, args []string) {
	if e.ejectedResources == nil {
		return
	}
	value := util.UnstructuredsToRuntimeObject(e.ejectedResources)
	if value == nil {
		return
	}

	switch e.out {
	case "yaml":
		data, err := yaml.Marshal(value)
		util.ExitOnError(err)
		fmt.Print(string(data))
	case "json":
		data, err := json.Marshal(value)
		util.ExitOnError(err)
		fmt.Print(string(data))
	default:
		panic(e.out)
	}
}

func (e *Eject) eject() error {
	templateConfig, err := util.LoadJsonFromFile(configuration.TemplateConfig)
	if err != nil {
		return err
	}

	// Parse the config
	gen := &generator.Context{}
	err = json.Unmarshal(templateConfig, gen)
	if err != nil {
		return err
	}

	syndesis := &v1alpha1.Syndesis{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: e.Namespace,
		},
	}
	if e.application != "" {
		cli, err := e.GetClient()
		if err != nil {
			return err
		}

		ctx := context.TODO()
		if err = cli.Get(ctx, util.NewObjectKey(e.application, e.Namespace), syndesis); err != nil {
			return err
		}
	}
	gen.Syndesis = syndesis

	if err = template.SetupRenderContext(gen, syndesis, template.ResourceParams{}, map[string]string{}); err != nil {
		return err
	}
	configuration.SetConfigurationFromEnvVars(gen.Env, syndesis)

	// Render the route resource...
	all, err := generator.RenderDir("./route/", gen)
	if err != nil {
		return err
	}

	// Render the remaining syndesis resources...
	inf, err := generator.RenderDir("./infrastructure/", gen)
	if err != nil {
		return err
	}

	all = append(all, inf...)

	for addon, properties := range syndesis.Spec.Addons {
		if properties["enabled"] != "true" {
			continue
		}

		addonDir := "./addons/" + addon + "/"
		f, err := generator.GetAssetsFS().Open(addonDir)
		if err != nil {
			fmt.Printf("unsuported addon configured: [%s]. [%v]", addon, err)
			continue
		}
		f.Close()

		resources, err := generator.RenderDir(addonDir, gen)
		if err != nil {
			return err
		}

		all = append(all, resources...)
	}

	e.ejectedResources = all
	return nil
}
