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

package backup

import (
	"github.com/operator-framework/operator-sdk/pkg/log/zap"
	"github.com/spf13/cobra"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"io/ioutil"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/client-go/tools/remotecommand"
	"os"
	"path/filepath"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/yaml"
)

type Backup struct {
	*internal.Options
	backupDir string
}

func New(parent *internal.Options) *cobra.Command {
	o := Backup{Options: parent}
	cmd := cobra.Command{
		Use:   "backup",
		Short: "backup the data for the syndesis install",
		Run: func(_ *cobra.Command, _ []string) {
			util.ExitOnError(o.Run())
		},
	}
	cmd.Flags().StringVar(&o.backupDir, "backup", "backup", "The directory to store the back up in")
	cmd.PersistentFlags().AddFlagSet(zap.FlagSet())
	return &cmd
}

func (o *Backup) Run() error {
	os.MkdirAll(o.backupDir, 0755)
	err := o.backupResources()
	if err != nil {
		return err
	}
	err = o.backupDatabase()
	if err != nil {
		return err
	}
	return nil
}

func (o *Backup) backupDatabase() error {
	api, err := o.NewApiClient()
	if err != nil {
		return err
	}
	pod, err := util.GetPodWithLabelSelector(api, o.Namespace, "syndesis.io/component=syndesis-db")
	if err != nil {
		return err
	}
	backupfile, err := os.Create(filepath.Join(o.backupDir, "syndesis-db.dump"))
	if err != nil {
		return err
	}
	defer backupfile.Close()

	return util.Exec(util.ExecOptions{
		Config:    o.GetClientConfig(),
		Api:       api,
		Namespace: o.Namespace,
		Pod:       pod.Name,
		Container: "postgresql",
		Command:   []string{`bash`, `-c`, `pg_dump -Fc -b syndesis | base64`},
		StreamOptions: remotecommand.StreamOptions{
			Stdout: backupfile,
			Stderr: os.Stderr,
		},
	})
}

func (o *Backup) backupResources() error {
	backupTypes := []metav1.TypeMeta{
		metav1.TypeMeta{APIVersion: "v1", Kind: "ConfigMap"},
		metav1.TypeMeta{APIVersion: "v1", Kind: "PersistentVolumeClaim"},
		metav1.TypeMeta{APIVersion: "v1", Kind: "Secret"},
		metav1.TypeMeta{APIVersion: "v1", Kind: "Service"},
		metav1.TypeMeta{APIVersion: "v1", Kind: "ServiceAccount"},
		metav1.TypeMeta{APIVersion: "rbac.authorization.k8s.io/v1", Kind: "RoleBinding"},
		metav1.TypeMeta{APIVersion: "template.openshift.io/v1", Kind: "Template"},
		metav1.TypeMeta{APIVersion: "build.openshift.io/v1", Kind: "BuildConfig"},
		metav1.TypeMeta{APIVersion: "apps.openshift.io/v1", Kind: "DeploymentConfig"},
		metav1.TypeMeta{APIVersion: "route.openshift.io/v1", Kind: "Route"},
	}

	selector, err := labels.Parse("syndesis.io/app=syndesis,syndesis.io/type=infrastructure")
	if err != nil {
		return err
	}

	c, err := o.GetClient()
	if err != nil {
		return err
	}

	for _, typeMeta := range backupTypes {
		options := client.ListOptions{
			Namespace:     o.Namespace,
			LabelSelector: selector,
			Raw: &metav1.ListOptions{
				TypeMeta: typeMeta,
				Limit:    200,
			},
		}
		list := unstructured.UnstructuredList{
			Object: map[string]interface{}{
				"apiVersion": typeMeta.APIVersion,
				"kind":       typeMeta.Kind,
			},
		}
		err = util.ListInChunks(o.Context, c, &options, &list, func(resources []unstructured.Unstructured) error {

			os.MkdirAll(filepath.Join(o.backupDir, "resources"), 0755)
			for _, res := range resources {
				data, err := yaml.Marshal(res)
				if err != nil {
					return err
				}

				err = ioutil.WriteFile(filepath.Join(o.backupDir, "resources", typeMeta.Kind+"-"+res.GetName()+".yaml"), data, 0755)
				if err != nil {
					return err
				}
			}
			return nil
		})
	}
	return nil
}
