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

package upgrade

import (
	"github.com/syndesisio/syndesis/install/operator/pkg"
	sbackup "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/backup"
)

type install struct {
	step
	backup sbackup.Runner
}

func newInstall(s step, b sbackup.Runner) (i *install) {
	i = &install{s, b}
	i.name = "Install"
	return
}

/*
 * run does nothing for install since install is a placeholder to rollback,
 * it is needed to restore the openshift resources
 */
func (i *install) run() (err error) {
	i.executed = true
	return
}

// Restore openshift resources
func (i *install) rollback() (err error) {
	i.executed = false

	if err = i.backup.Validate(); err != nil {
		if i.backup, err = i.backup.BuildBackupDir(pkg.DefaultOperatorTag); err != nil {
			return
		}
	}

	err = i.backup.RestoreResources()
	return
}
