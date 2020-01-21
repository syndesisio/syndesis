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

	logf "sigs.k8s.io/controller-runtime/pkg/log"

	sbackup "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/backup"
)

func Test_backup_canRun(t *testing.T) {
	type fields struct {
		step step
		done bool
		sb   sbackup.Runner
	}
	tests := []struct {
		name   string
		fields fields
		wantR  bool
	}{
		{"Backup should run if it didnt run before", fields{step: step{executed: false}, done: false, sb: BackupTester{}}, true},
		{"Backup should not run if it already ran", fields{step: step{executed: true, log: logf.Log.WithName("test")}, done: false, sb: BackupTester{}}, false},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			b := &backup{
				step: tt.fields.step,
				done: tt.fields.done,
				sb:   tt.fields.sb,
			}
			if gotR := b.canRun(); gotR != tt.wantR {
				t.Errorf("canRun() = %v, want %v", gotR, tt.wantR)
			}
		})
	}
}

func Test_backup_canRollback(t *testing.T) {
	type fields struct {
		step step
		done bool
		sb   sbackup.Runner
	}
	tests := []struct {
		name   string
		fields fields
		wantR  bool
	}{
		{"Backup doesnt rollback, independently of executed = true", fields{step: step{executed: true}, done: false, sb: BackupTester{}}, false},
		{"Backup doesnt rollback, independently of executed = false", fields{step: step{executed: false, log: logf.Log.WithName("test")}, done: false, sb: BackupTester{}}, false},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			b := &backup{
				step: tt.fields.step,
				done: tt.fields.done,
				sb:   tt.fields.sb,
			}
			if gotR := b.canRollback(); gotR != tt.wantR {
				t.Errorf("canRollback() = %v, want %v", gotR, tt.wantR)
			}
		})
	}
}

func Test_backup_run(t *testing.T) {
	type fields struct {
		step step
		done bool
		sb   sbackup.Runner
	}
	tests := []struct {
		name   string
		fields fields
		wantR  bool
	}{
		{"Run should set executed to true if false", fields{step: step{executed: true}, done: false, sb: BackupTester{}}, true},
		{"Run should leave executed as true if true", fields{step: step{executed: false, log: logf.Log.WithName("test")}, done: false, sb: BackupTester{}}, true},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			b := &backup{
				step: tt.fields.step,
				done: tt.fields.done,
				sb:   tt.fields.sb,
			}
			if err := b.run(); (err != nil) && b.executed != tt.wantR {
				t.Errorf("run() error = %v, wantErr %v", err, tt.wantR)
			}
		})
	}
}
