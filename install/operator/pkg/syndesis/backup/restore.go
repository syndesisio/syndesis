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
	"os"
	"path/filepath"

	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/client-go/tools/remotecommand"
)

func (b *Backup) Restore() error {
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
