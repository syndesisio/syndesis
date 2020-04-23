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
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"

	"github.com/go-logr/logr"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
)

// Interface to be used top perform upgrades
type Upgrader interface {
	// Upgrade run all the steps performing a syndesis upgrade
	Upgrade() (err error)

	// In case the Upgrade failed, it is possible to perform a rollback
	// Bringing syndesis to the state it was before the upgrade started
	Rollback() (err error)

	// Because we consider the first install run after upgrade part of the upgrade
	// We need a way to signal this pkg when that step went wrong
	InstallFailed() (count int)
}

type step struct {
	name        string
	log         logr.Logger
	executed    bool
	namespace   string
	context     context.Context
	clientTools *clienttools.ClientTools
}

type stepRunner interface {
	infoRun()
	infoRollback()
	canRun() (r bool)
	canRollback() (r bool)
	run() (err error)
	rollback() (err error)
}

type failure struct {
	T   time.Time
	S   interface{}
	Err error
}

type succeed struct {
	T time.Time
}

type result interface {
	failure() bool
	step() interface{}
}

type upgrade struct {
	log         logr.Logger
	steps       []stepRunner
	backup      sbackup.Runner
	attempts    []result
	ctx         context.Context
	syndesis    *v1beta1.Syndesis
	clientTools *clienttools.ClientTools
}

// Run the upgrade
func (u *upgrade) Upgrade() (err error) {
	for _, step := range u.steps {
		if step.canRun() {
			step.infoRun()
			if err = step.run(); err != nil {
				u.attempts = append(u.attempts, failure{S: step, T: time.Now(), Err: err})
				return
			}
		}
	}

	u.attempts = append(u.attempts, succeed{T: time.Now()})
	return
}

// Rollback a previous upgrade action. Rollback can only be executed
// if the upgrade failed
func (u *upgrade) Rollback() (err error) {
	switch v := u.attempts[len(u.attempts)-1].(type) {
	case failure:
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
func (u *upgrade) InstallFailed() (count int) {
	count = 0
	u.attempts = append(u.attempts, failure{T: time.Now(), S: install{}, Err: nil})
	for _, at := range u.attempts {
		switch at.step().(type) {
		case install:
			count = count + 1
		}
	}

	return
}

// build the upgrade struct
func Build(log logr.Logger, syndesis *v1beta1.Syndesis, clientTools *clienttools.ClientTools, ctx context.Context) (Upgrader, error) {
	base := step{
		log:         log,
		executed:    false,
		clientTools: clientTools,
		context:     ctx,
		namespace:   syndesis.Namespace,
	}

	bkp, err := sbackup.NewBackup(ctx, clientTools, syndesis,
		strings.Join([]string{"/tmp/", strconv.FormatInt(time.Now().Unix(), 10)}, ""))
	if err != nil {
		return nil, err
	}
	bkp.SetLocalOnly(true)

	u := &upgrade{
		log:         log,
		steps:       nil,
		backup:      bkp,
		attempts:    []result{},
		ctx:         ctx,
		syndesis:    syndesis,
		clientTools: clientTools,
	}

	bbkp, err := newBackup(base, u.syndesis)
	if err != nil {
		return nil, err
	}

	u.steps = []stepRunner{
		newScale(base).down(),
		bbkp,
		newMigration(base, u.syndesis, u.backup),
		newInstall(base, u.backup),
		newCleanup(base),
	}

	return u, nil
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

func (s succeed) failure() bool {
	return false
}

func (s succeed) step() interface{} {
	return nil
}

func (s failure) step() interface{} {
	return s.S
}

func (s failure) failure() bool {
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
