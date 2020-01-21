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

package action

import (
	"context"
	"fmt"
	"strings"

	"k8s.io/client-go/kubernetes"
	"sigs.k8s.io/controller-runtime/pkg/manager"

	cron "github.com/robfig/cron/v3"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/backup"
)

var c = cron.New()

// Manages syndesis backups
type backupAction struct {
	baseAction
}

func newBackupAction(mgr manager.Manager, api kubernetes.Interface) SyndesisOperatorAction {
	return &backupAction{
		newBaseAction(mgr, api, "backup"),
	}
}

func (a *backupAction) CanExecute(syndesis *v1beta1.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		v1beta1.SyndesisPhaseInstalled,
	)
}

// Schedule a cronjob for systematic backups
func (a *backupAction) Execute(ctx context.Context, syndesis *v1beta1.Syndesis) error {
	entries := c.Entries()

	if s := syndesis.Spec.Backup.Schedule; s != "" {
		if len(entries) == 0 {
			a.log.Info("scheduling backup job", "frequency", string(s))
			c.AddFunc(strings.Join([]string{"@", string(s)}, ""), func() {
				b := &backup.Backup{
					Namespace: syndesis.Namespace,
					Context:   ctx,
					Client:    &a.client,
					Delete:    true,
					LocalOnly: false,
					BackupDir: "/tmp/foo",
				}

				b.Run()
			})

			c.Start()
		} else if len(entries) == 1 {
			syndesis.Status.Backup.Next = entries[0].Next.String()
			syndesis.Status.Backup.Previous = entries[0].Prev.String()

			return a.client.Update(ctx, syndesis)
		} else {
			return fmt.Errorf("unsopported number of entries for cron instance, cron %v", c)
		}
	} else {
		if len(entries) == 1 {
			e := entries[0]

			a.log.Info("removing backup job from scheduler", "job", e.ID)
			c.Remove(e.ID)
			c.Stop()
		} else if len(entries) > 1 {
			return fmt.Errorf("unsopported number of entries for cron instance, cron %v", c)
		}
	}

	return nil
}
