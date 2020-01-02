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
	"testing"

	sbackup "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/backup"
)

type BackupTester struct {
}

func (bt BackupTester) Run() error {
	return nil
}

func (bt BackupTester) Restore() error {
	return nil
}

func (bt BackupTester) Validate() error {
	return nil
}

func (bt BackupTester) RestoreResources() error {
	return nil
}

func (bt BackupTester) RestoreDb() error {
	return nil
}

func (bt BackupTester) BuildBackupDir(path string) (b *sbackup.Backup, err error) {
	return &sbackup.Backup{}, nil
}

func Test_install_canRollback(t *testing.T) {
	type fields struct {
		step   step
		backup sbackup.Runner
	}
	tests := []struct {
		name   string
		fields fields
		result bool
	}{
		{"Install should rollback if previously executed", fields{step: step{executed: true}, backup: BackupTester{}}, true},
		{"Install should not rollback when not yet executed", fields{step: step{executed: false}, backup: BackupTester{}}, false},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			i := &install{
				step:   tt.fields.step,
				backup: tt.fields.backup,
			}
			if r := i.canRollback(); r != tt.result {
				t.Errorf("canRollback() error = %v, want result %v", r, tt.result)
			}
		})
	}
}

func Test_install_run(t *testing.T) {
	type fields struct {
		step   step
		backup sbackup.Runner
	}
	tests := []struct {
		name   string
		fields fields
		result bool
	}{
		{"Run should set executed to true if false", fields{step: step{executed: false}, backup: BackupTester{}}, true},
		{"Run should leave executed as true if true", fields{step: step{executed: true}, backup: BackupTester{}}, true},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			i := &install{
				step:   tt.fields.step,
				backup: tt.fields.backup,
			}
			if err := i.run(); i.executed != tt.result {
				t.Errorf("run() error = %v, wantErr %v", err, tt.result)
			}
		})
	}
}
