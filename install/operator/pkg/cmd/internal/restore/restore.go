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

package restore

import (
	"github.com/chirino/hawtgo/sh"
	"github.com/operator-framework/operator-sdk/pkg/log/zap"
	"github.com/spf13/cobra"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal/backup"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"os"
	"path/filepath"
)

type Backup struct {
	*internal.Options
	backupDir string
}

func New(parent *internal.Options) *cobra.Command {
	o := Backup{Options: parent}
	cmd := cobra.Command{
		Use:   "restore",
		Short: "restore the data that was previously backed up",
		Run: func(_ *cobra.Command, _ []string) {
			util.ExitOnError(o.Run())
		},
	}
	cmd.Flags().StringVar(&o.backupDir, "backup", "/tmp/backup", "The directory where the backup is stored")
	cmd.PersistentFlags().AddFlagSet(zap.FlagSet())
	cmd.PersistentFlags().AddFlagSet(util.FlagSet)
	return &cmd
}

func (o *Backup) Run() error {
	api, err := o.NewApiClient()
	if err != nil {
		return err
	}

	podName, err := backup.GetPostgresPodName(api, o.Namespace)
	if err != nil {
		return err
	}

	backupfile, err := os.Open(filepath.Join(o.backupDir, "syndesis-db.dump"))
	if err != nil {
		return err
	}
	defer backupfile.Close()
	sh.New().LineArgs(`oc`, `rsh`, `--container=postgresql`, podName, `bash`, `-c`, `
set -e;
base64 -d -i > /var/lib/pgsql/data/syndesis.dmp;
psql -c 'DROP database if exists syndesis_restore'
psql -c 'CREATE database syndesis_restore'
pg_restore -v -d syndesis_restore /var/lib/pgsql/data/syndesis.dmp;
psql -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'syndesis'"
psql -c 'DROP database if exists syndesis'
psql -c 'ALTER database syndesis_restore rename to syndesis'
rm /var/lib/pgsql/data/syndesis.dmp;
`).
		Stdin(backupfile).
		MustZeroExit()

	return nil
}
