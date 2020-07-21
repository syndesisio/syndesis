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
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
)

type Restore struct {
	*Backup
	customOptions string
}

func NewRestore(parent *internal.Options) *cobra.Command {
	o := Restore{
		Backup: &Backup{
			Options: parent,
		},
	}
	cmd := cobra.Command{
		Use:   "restore",
		Short: "restore the data that was previously backed up",
		Run: func(_ *cobra.Command, _ []string) {
			util.ExitOnError(o.Run())
		},
	}

	cmd.PersistentFlags().StringVarP(&configuration.TemplateConfig, "operator-config", "", "/conf/config.yaml", "Path to the operator configuration file.")
	cmd.Flags().StringVar(&o.backupDir, "backup", "/tmp/backup", "The directory where the backup is stored")
	cmd.Flags().StringVar(&o.customOptions, "custom-options", "", "Set of custom options to provide to pg_restore (default: --no-password --clean --if-exists --create --verbose)")
	cmd.PersistentFlags().AddFlagSet(zap.FlagSet())
	cmd.PersistentFlags().AddFlagSet(util.FlagSet)
	return &cmd
}

func (o *Restore) Run() error {
	syndesis, err := o.prepare()
	if err != nil {
		return err
	}

	b, err := backup.NewBackup(o.Context, o.ClientTools(), syndesis, o.backupDir)
	if err != nil {
		return err
	}

	b.SetCustomOptions(o.customOptions)
	return b.Restore()
}
