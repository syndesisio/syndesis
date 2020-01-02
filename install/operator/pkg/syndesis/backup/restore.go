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
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"

	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"

	"github.com/pkg/errors"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/client-go/tools/remotecommand"
)

// Restore backup from a zipped file or from a backup dir
// Restore database and openshift resources
func (b *Backup) Restore() (err error) {
	b.logger("restore").Info("restoring backup for syndesis", "backup", b.BackupDir)
	fi, err := os.Stat(b.BackupDir)
	if err != nil {
		return
	}

	if !fi.IsDir() {
		dir, err := ioutil.TempDir("/tmp", "restore-")
		if err != nil {
			return err
		}
		defer os.RemoveAll(dir)
		os.Chmod(dir, 0755)

		if err = b.unzip(b.BackupDir, dir); err != nil {
			return err
		}

		b.BackupDir = dir
	}

	if err = b.Validate(); err != nil {
		return
	}

	if err = b.RestoreDb(); err != nil {
		return
	}

	if err = b.RestoreResources(); err != nil {
		return
	}

	return
}

// Validates that a given backup has a correct format
// and is the right version
func (b *Backup) Validate() (err error) {
	if fr, err := os.Stat(filepath.Join(b.BackupDir, "resources")); err != nil || !fr.IsDir() {
		return fmt.Errorf("folder resources is missing or it is not accesible in backup dir %s", b.BackupDir)
	}

	if _, err = os.Stat(filepath.Join(b.BackupDir, "syndesis-db.dump")); err != nil {
		return fmt.Errorf("database backup file is missing or it is not accesible in backup dir %s", b.BackupDir)
	}

	return
}

// Restore openshift resources
func (b *Backup) RestoreResources() (err error) {
	rss, err := ioutil.ReadDir(filepath.Join(b.BackupDir, "resources"))
	if err != nil {
		return err
	}

	var obj interface{} = nil
	resources := []unstructured.Unstructured{}
	for _, rs := range rss {
		if strings.HasSuffix(rs.Name(), ".yml") || strings.HasSuffix(rs.Name(), ".yaml") {
			dat, err := ioutil.ReadFile(filepath.Join(b.BackupDir, "resources", rs.Name()))
			if err != nil {
				return err
			}

			err = util.UnmarshalYaml(dat, &obj)
			if err != nil {
				return errors.Errorf("%s:\n%s\n", err, string(dat))
			}

			switch v := obj.(type) {
			case []interface{}:
				for _, value := range v {
					if x, ok := value.(map[string]interface{}); ok {
						u := unstructured.Unstructured{x}
						//annotatedForDebugging(u, name, rawYaml)
						resources = append(resources, u)
					} else {
						return errors.New("list did not contain objects")
					}
				}
			case map[string]interface{}:
				u := unstructured.Unstructured{v}
				//annotatedForDebugging(u, name, rawYaml)
				resources = append(resources, u)
			case nil:
				// It's ok if a template chooses not to generate any resources..

			default:
				return fmt.Errorf("unexptected yaml unmarshal type: %v", obj)
			}
		}
	}

	err = b.install("upgrade", resources)
	if err != nil {
		return err
	}

	return
}

// Restore database
func (b *Backup) RestoreDb() (err error) {
	api, err := b.apiClient()
	if err != nil {
		return err
	}

	pod, err := util.GetPodWithLabelSelector(api, b.Namespace, "syndesis.io/component=syndesis-db")
	if err != nil {
		return err
	}

	backupFile, err := os.Open(filepath.Join(b.BackupDir, "syndesis-db.dump"))
	if err != nil {
		return err
	}
	defer backupFile.Close()

	return util.Exec(util.ExecOptions{
		Config:    b.clientConfig(),
		Api:       api,
		Namespace: b.Namespace,
		Pod:       pod.Name,
		Container: "postgresql",
		Command: []string{`bash`, `-c`, `
set -e;
base64 -d -i > /var/lib/pgsql/data/syndesis.dmp;
psql -c 'DROP database if exists syndesis_restore'
psql -c 'CREATE database syndesis_restore'
pg_restore -v -d syndesis_restore /var/lib/pgsql/data/syndesis.dmp;
psql -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'syndesis'"
psql -c 'DROP database if exists syndesis'
psql -c 'ALTER database syndesis_restore rename to syndesis'
rm /var/lib/pgsql/data/syndesis.dmp;
`},
		StreamOptions: remotecommand.StreamOptions{
			Stdin:  backupFile,
			Stdout: os.Stdout,
			Stderr: os.Stderr,
		},
	})
}

func (b *Backup) install(action string, resources []unstructured.Unstructured) error {
	client, err := b.client()
	if err != nil {
		return err
	}
	for _, res := range resources {
		_, _, err := util.CreateOrUpdate(b.Context, client, &res)
		if err != nil {
			return errors.Wrap(err, util.Dump(res))
		}
		b.log.Info("resource restored", "resources", res.GetName(), "kind", res.GetKind())
	}

	return nil
}
