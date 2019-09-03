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

package uninstall

import (
	"fmt"
	"github.com/operator-framework/operator-sdk/pkg/restmapper"
	"github.com/spf13/cobra"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/api/errors"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/client/config"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

type Uninstall struct {
	*internal.Options
}

func New(parent *internal.Options) *cobra.Command {
	o := Uninstall{Options: parent}
	cmd := cobra.Command{
		Use:   "uninstall",
		Short: "uninstall syndesis app",
		Run: func(_ *cobra.Command, _ []string) {
			util.ExitOnError(o.uninstall())
		},
	}

	return &cmd
}

func (o *Uninstall) uninstall() error {
	sl := &v1alpha1.SyndesisList{}
	cfg, err := config.GetConfig()
	if err != nil {
		return err
	}

	mgr, err := manager.New(cfg, manager.Options{
		Namespace:      o.Namespace,
		MapperProvider: restmapper.NewDynamicRESTMapper,
	})
	if err != nil {
		return err
	}

	if err := apis.AddToScheme(mgr.GetScheme()); err != nil {
		return err
	}

	c, err := o.GetClient()
	if err != nil {
		return err
	}

	err = c.List(o.Context, &client.ListOptions{}, sl)
	for _, res := range sl.Items {
		err = c.Delete(o.Context, &res)
		if err != nil {
			if !errors.IsNotFound(err) {
				fmt.Println(err, "could not deleted", "custom resource", res.Name, "namespace", res.GetNamespace())
			}
		} else {
			fmt.Println("resource deleted", "custom resource", res.Name, "namespace", res.GetNamespace())
		}
	}

	return nil
}
