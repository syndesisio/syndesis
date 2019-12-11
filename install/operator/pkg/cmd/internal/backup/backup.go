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
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/backup"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
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
	cmd.PersistentFlags().AddFlagSet(util.FlagSet)
	return &cmd
}

func (o *Backup) Run() error {
	b := backup.Backup{
		Namespace: o.Namespace,
		BackupDir: o.backupDir,
		Context:   o.Context,
		Client:    o.Client,
	}

	return b.Backup()
}
