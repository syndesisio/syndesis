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

package upgrade

import (
	"strconv"
	"strings"
	"time"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta2"
	sbackup "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/backup"
)

type backup struct {
	step
	done bool
	sb   sbackup.Runner
}

func newBackup(base step, s *v1beta2.Syndesis) (*backup, error) {

	bkp, err := sbackup.NewBackup(base.context, base.clientTools, s,
		strings.Join([]string{"/tmp/", strconv.FormatInt(time.Now().Unix(), 10)}, ""))
	if err != nil {
		return nil, err
	}
	bkp.SetLocalOnly(true)

	b := &backup{
		step: base,
		done: false,
		sb:   bkp,
	}
	b.name = "Backup"

	return b, nil
}

/*
 * Before performing an upgrade of Syndesis, a full backup has to be done.
 *
 * It is needed to preserve the current status of Syndesis in case something goes wrong while upgrading. Before proceeding,
 * we will backup the postgres database, all openshift resources and the Syndesis custom resource
 */
func (b *backup) run() (err error) {
	if err = b.sb.Run(); err != nil {
		return err
	}

	b.executed = true
	return
}

// The backup is done properly, it should not repeat for the next upgrade iterations
func (b *backup) canRun() (r bool) {
	r = b.executed
	if r {
		b.log.Info("backup previously done, skipping")
	}

	return !r
}

// There is no rollback for the backup
func (b *backup) canRollback() (r bool) {
	return false
}

func (b *backup) rollback() (err error) {
	return
}
