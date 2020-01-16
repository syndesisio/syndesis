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
	"context"
	"strconv"
	"strings"
	"time"

	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	"sigs.k8s.io/controller-runtime/pkg/client/config"

	sbackup "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/backup"

	"github.com/go-logr/logr"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type step struct {
	name      string
	log       logr.Logger
	executed  bool
	namespace string
	context   context.Context
	client    client.Client
}

type stepRunner interface {
	infoRun()
	infoRollback()
	canRun() (r bool)
	canRollback() (r bool)
	run() (err error)
	rollback() (err error)
}

type Failure struct {
	T   time.Time
	S   interface{}
	Err error
}

type Succeed struct {
	T time.Time
}

type result interface {
	failure() bool
	step() interface{}
}

type Upgrade struct {
	log      logr.Logger
	steps    []stepRunner
	backup   sbackup.Runner
	attempts []result
	Ctx      context.Context
	Syndesis *v1alpha1.Syndesis
	Client   client.Client
}

// Run the upgrade
func (u *Upgrade) Upgrade() (err error) {
	for _, step := range u.steps {
		if step.canRun() {
			step.infoRun()
			if err = step.run(); err != nil {
				u.attempts = append(u.attempts, Failure{S: step, T: time.Now(), Err: err})
				return
			}
		}
	}

	u.attempts = append(u.attempts, Succeed{T: time.Now()})
	return
}

// Rollback a previous upgrade action. Rollback can only be executed
// if the upgrade failed
func (u *Upgrade) Rollback() (err error) {
	switch v := u.attempts[len(u.attempts)-1].(type) {
	case Failure:
		for _, step := range u.steps {
			if step.canRollback() {
				step.infoRollback()
				if err = step.rollback(); err != nil {
					u.log.Error(err, "an error has encountered while rolling back, some manual steps might be required")
				}
			}
		}

		u.attempts = []result{}
	default:
		u.log.Info("I should roll back from a Failure, but got something different", "last attempt", v)
	}

	return
}

// Add a failure for install step and return the total failures of this kind
func (u *Upgrade) InstallFailed() (count int) {
	count = 0
	u.attempts = append(u.attempts, Failure{T: time.Now(), S: install{}, Err: nil})
	for _, at := range u.attempts {
		switch at.step().(type) {
		case install:
			count = count + 1
		}
	}

	return
}

// build the upgrade struct
func Build(log logr.Logger, syndesis *v1alpha1.Syndesis, client client.Client, ctx context.Context) (r *Upgrade) {
	base := step{
		log:       log,
		executed:  false,
		client:    client,
		context:   ctx,
		namespace: syndesis.Namespace,
	}

	r = &Upgrade{
		log:   log,
		steps: nil,
		backup: &sbackup.Backup{
			Namespace: syndesis.Namespace,
			BackupDir: strings.Join([]string{"/tmp/", strconv.FormatInt(time.Now().Unix(), 10)}, ""),
			Delete:    false,
			LocalOnly: true,
			Context:   ctx,
			Client:    &client,
		},
		attempts: []result{},
		Ctx:      ctx,
		Syndesis: syndesis,
		Client:   client,
	}

	r.steps = []stepRunner{
		newScale(base).down(),
		newBackup(base),
		newMigration(base, r.Syndesis, r.backup),
		newInstall(base, r.backup),
		newScale(base).up(),
	}

	return
}

func (s step) canRun() (r bool) {
	return !s.executed
}

func (s step) canRollback() (r bool) {
	return s.executed
}

func (s step) infoRun() {
	s.log.Info("running step", "step", s.name)
}

func (s step) infoRollback() {
	s.log.Info("rolling back step", "step", s.name)
}

func (s Succeed) failure() bool {
	return false
}

func (s Succeed) step() interface{} {
	return nil
}

func (s Failure) step() interface{} {
	return s.S
}

func (s Failure) failure() bool {
	return true
}

func (s step) config() (c *rest.Config) {
	c, err := config.GetConfig()
	util.ExitOnError(err)

	return
}

func (s step) api() (*kubernetes.Clientset, error) {
	return kubernetes.NewForConfig(s.config())
}
