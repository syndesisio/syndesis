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
	"context"
	"io/ioutil"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"github.com/go-logr/logr"

	"github.com/syndesisio/syndesis/install/operator/pkg"
	"k8s.io/client-go/dynamic"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	"sigs.k8s.io/controller-runtime/pkg/client/config"

	"k8s.io/client-go/tools/remotecommand"

	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"gopkg.in/yaml.v2"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/labels"
	"sigs.k8s.io/controller-runtime/pkg/client"
	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
)

var backupLog = logf.Log.WithName("backup")

type Backup struct {
	log        logr.Logger
	backupPath string
	Namespace  string
	BackupDir  string
	Delete     bool
	LocalOnly  bool
	Context    context.Context
	Client     *client.Client
}

// Uploader interface has methods to upload backup files
// to a remote datastore
type Uploader interface {
	// Upload backup files to a remote location
	Upload(dir string) (err error)

	// Update syndesis status to reflect an upload
	Status() (err error)

	// Can this uploader be used with current settings
	Enabled() (result bool)
}

// downloader interface has methods to download backup files
// from a remote location
type Downloader interface {
	Download(dir string) (err error)
	Enabled() (result bool)
}

// Create a backup, zip it and upload it to different
// datastores
func (b *Backup) Run() (err error) {
	b.logger("backup").Info("starting backup for syndesis")

	if err = b.backup(); err != nil {
		b.log.Error(err, "error performing backup")
		return
	}

	zipped, err := b.zip()
	if err != nil {
		b.log.Error(err, "error creating zip file for backup")
		return
	}

	if b.Delete {
		defer os.RemoveAll(b.backupPath)
		defer os.RemoveAll(zipped)
	}

	if !b.LocalOnly {
		uploader := []Uploader{&S3{Backup: b, file: zipped}}

		for _, u := range uploader {
			if u.Enabled() {
				if err = u.Upload(b.BackupDir); err != nil {
					b.log.Error(err, "error uploading backup file to source", "source", u)
					return
				}
				break
			}
		}
	}

	b.log.Info("backup for syndesis done")
	return
}

// Perform a backup of all relevant openshift resources
// and the database
func (b *Backup) backup() (err error) {
	if err = b.ensureDir(); err != nil {
		return
	}

	if err = b.backupResources(); err != nil {
		return
	}

	if err = b.backupDatabase(); err != nil {
		return
	}

	return
}

func (b *Backup) ensureDir() (err error) {
	if len(b.BackupDir) == 0 {
		abs, err := filepath.Abs(".")
		if err != nil {
			return err
		}

		b.BackupDir = abs
	}

	b.backupPath = filepath.Join(pkg.DefaultOperatorTag, strconv.FormatInt(time.Now().Unix(), 10))
	err = os.MkdirAll(filepath.Join(b.BackupDir, b.backupPath), 0755)

	if err == nil || os.IsExist(err) {
		return nil
	}

	return
}

// Create a database backup
func (b *Backup) backupDatabase() error {
	api, err := b.apiClient()
	if err != nil {
		return err
	}

	pod, err := util.GetPodWithLabelSelector(api, b.Namespace, "syndesis.io/component=syndesis-db")
	if err != nil {
		return err
	}
	backupfile, err := os.Create(filepath.Join(b.BackupDir, b.backupPath, "syndesis-db.dump"))
	if err != nil {
		return err
	}
	defer backupfile.Close()

	return util.Exec(util.ExecOptions{
		Config:    b.clientConfig(),
		Api:       api,
		Namespace: b.Namespace,
		Pod:       pod.Name,
		Container: "postgresql",
		Command:   []string{`bash`, `-c`, `pg_dump -Fc -b syndesis | base64`},
		StreamOptions: remotecommand.StreamOptions{
			Stdout: backupfile,
			Stderr: os.Stderr,
		},
	})
}

// Create a openshift resource backup
func (b *Backup) backupResources() error {
	backupTypes := []metav1.TypeMeta{
		{APIVersion: "v1", Kind: "ConfigMap"},
		{APIVersion: "v1", Kind: "PersistentVolumeClaim"},
		{APIVersion: "v1", Kind: "Secret"},
		{APIVersion: "v1", Kind: "Service"},
		{APIVersion: "v1", Kind: "ServiceAccount"},
		{APIVersion: "rbac.authorization.k8s.io/v1", Kind: "RoleBinding"},
		{APIVersion: "template.openshift.io/v1", Kind: "Template"},
		{APIVersion: "build.openshift.io/v1", Kind: "BuildConfig"},
		{APIVersion: "apps.openshift.io/v1", Kind: "DeploymentConfig"},
		{APIVersion: "route.openshift.io/v1", Kind: "Route"},
	}

	selector, err := labels.Parse("syndesis.io/app=syndesis,syndesis.io/type=infrastructure")
	if err != nil {
		return err
	}

	c, err := b.client()
	if err != nil {
		return err
	}

	for _, typeMeta := range backupTypes {
		options := client.ListOptions{
			Namespace:     b.Namespace,
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
		err = util.ListInChunks(b.Context, c, &options, &list, func(resources []unstructured.Unstructured) error {
			os.MkdirAll(filepath.Join(b.BackupDir, b.backupPath, "resources"), 0755)
			for _, res := range resources {
				data, err := yaml.Marshal(res.Object)
				if err != nil {
					return err
				}

				err = ioutil.WriteFile(filepath.Join(b.BackupDir, b.backupPath, "resources", strings.ToLower(typeMeta.Kind+"-"+res.GetName()+".yaml")), data, 0755)
				if err != nil {
					return err
				}
			}
			return nil
		})
	}
	return nil
}

func (b *Backup) clientConfig() *rest.Config {
	c, err := config.GetConfig()
	util.ExitOnError(err)
	return c
}

func (b *Backup) client() (c client.Client, err error) {
	if b.Client == nil {
		cl, err := client.New(b.clientConfig(), client.Options{})
		if err != nil {
			return nil, err
		}
		b.Client = &cl
	}
	return *b.Client, nil
}

func (b *Backup) dynamicClient() (c dynamic.Interface, err error) {
	return dynamic.NewForConfig(b.clientConfig())
}

func (b *Backup) apiClient() (*kubernetes.Clientset, error) {
	return kubernetes.NewForConfig(b.clientConfig())
}

func (b *Backup) logger(t string) logr.Logger {
	if b.log == nil {
		b.log = backupLog.WithValues("action", t)
	}

	return b.log
}
