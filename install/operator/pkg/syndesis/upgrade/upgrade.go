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
	"fmt"
	"strconv"
	"strings"
	"time"

	sbackup "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/backup"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/client-go/kubernetes"
	cgocorev1 "k8s.io/client-go/kubernetes/typed/core/v1"
	"k8s.io/client-go/rest"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/client/config"

	"github.com/go-logr/logr"
	oappsv1 "github.com/openshift/api/apps/v1"
	synapi "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta3"
	appsv1 "k8s.io/api/apps/v1"
	k8serr "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/apimachinery/pkg/util/wait"
	"k8s.io/client-go/dynamic"
)

const upgradeDeploymentName = "syndesis-db-upgrade"

var upgradeLabels = map[string]string{
	"syndesis.io/app":       "syndesis",
	"syndesis.io/component": upgradeDeploymentName,
}

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
	syndesis    *synapi.Syndesis
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
func Build(ctx context.Context, log logr.Logger, syndesis *synapi.Syndesis, clientTools *clienttools.ClientTools) (Upgrader, error) {
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
		newCleanupAddons(base, u.syndesis),
		newMigration(base, u.syndesis, u.backup),
		newDatabaseUpgrade(base, syndesis),
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

func (s *step) client() client.Client {
	client, err := s.clientTools.RuntimeClient()
	if err != nil {
		panic(err)
	}

	return client
}

func (s *step) dynClient() dynamic.Interface {
	dynClient, err := s.clientTools.DynamicClient()
	if err != nil {
		panic(err)
	}

	return dynClient
}

func (s *step) coreV1Client() cgocorev1.CoreV1Interface {
	coreV1Client, err := s.clientTools.CoreV1Client()
	if err != nil {
		panic(err)
	}

	return coreV1Client
}

func (s step) apiClient() kubernetes.Interface {
	apiClient, err := s.clientTools.ApiClient()
	if err != nil {
		panic(err)
	}

	return apiClient
}

type scaleTracker interface {
	obj() client.Object
	hasScaled() bool
}

type deploymentTracker struct {
	deployment appsv1.Deployment
}

func newDeploymentTracker() scaleTracker {
	return &deploymentTracker{
		deployment: appsv1.Deployment{},
	}
}

func (d *deploymentTracker) obj() client.Object {
	return &d.deployment
}

func (d *deploymentTracker) hasScaled() bool {
	//
	// Wait for scaling up of the db upgrade container
	// Waits for the ReadyReplicas to equal the required Replicas
	//
	return *d.deployment.Spec.Replicas == d.deployment.Status.ReadyReplicas
}

type deploymentConfigTracker struct {
	deployment oappsv1.DeploymentConfig
}

func newDeploymentConfigTracker() scaleTracker {
	return &deploymentConfigTracker{
		deployment: oappsv1.DeploymentConfig{},
	}
}

func (d *deploymentConfigTracker) obj() client.Object {
	return &d.deployment
}

func (d *deploymentConfigTracker) hasScaled() bool {
	return d.deployment.Status.Replicas == d.deployment.Status.ReadyReplicas
}

// Waits at most 15min for the Deployment or DeploymentConfig to reach the desired scale
func (s *step) awaitScale(name string, tracker scaleTracker) error {
	if err := wait.PollImmediate(time.Second*3, time.Minute*15, func() (done bool, err error) {
		if err = s.client().Get(s.context, types.NamespacedName{Namespace: s.namespace, Name: name}, tracker.obj()); err != nil {
			if k8serr.IsNotFound(err) {
				// May not have been created yet so wait until the timeout
				return false, nil
			}

			return false, err
		}

		if !tracker.hasScaled() {
			s.log.Info("Waiting for the deployment to scale", "deployment", name)
			return false, nil
		}

		return true, nil
	}); err != nil {
		s.log.Error(err, "Failed in waiting for the deployment to scale", "deployment", name)
		return err
	}

	return nil
}

//
// Used both in migration step if rolling back
// & in cleanup on completion
//
func (s *step) deleteMigrationJob() {

	s.log.Info("cleaning up migration job")
	if err := s.apiClient().BatchV1().
		Jobs(s.namespace).
		Delete(s.context, "upgrade-db-migration", metav1.DeleteOptions{}); err != nil {
		if !k8serr.IsNotFound(err) {
			s.log.Error(err, "there was an error deleting the job generated by the migration step, some manual steps might be required")
		}
	}

	s.log.Info("cleaning up migration pods")
	if err := s.coreV1Client().
		Pods(s.namespace).
		DeleteCollection(s.context, metav1.DeleteOptions{}, metav1.ListOptions{LabelSelector: "job-name=upgrade-db-migration"}); err != nil {
		if !k8serr.IsNotFound(err) {
			s.log.Error(err, "there was an error deleting the pods generated by the migration step, some manual steps might be required")
		}
	}
}

//
// Used both in db upgrade step if rolling back
// & in cleanup on completion
//
func (s *step) deleteDbUpgrade() error {
	var combined error
	if err := s.client().DeleteAllOf(s.context, &appsv1.Deployment{}, client.InNamespace(s.namespace), client.MatchingLabels(upgradeLabels)); err != nil {
		combined = err
	}
	if err := s.client().DeleteAllOf(s.context, &corev1.PersistentVolumeClaim{}, client.InNamespace(s.namespace), client.MatchingLabels(upgradeLabels)); err != nil {
		combined = fmt.Errorf("unable to perform cleanup due to: %v, %v", combined, err)
	}
	if err := s.client().DeleteAllOf(s.context, &batchv1.Job{}, client.InNamespace(s.namespace), client.MatchingLabels(upgradeLabels)); err != nil {
		combined = fmt.Errorf("unable to perform cleanup due to: %v, %v", combined, err)
	}
	if err := s.client().DeleteAllOf(s.context, &corev1.Pod{}, client.InNamespace(s.namespace), client.MatchingLabels(upgradeLabels)); err != nil {
		combined = fmt.Errorf("unable to perform cleanup due to: %v, %v", combined, err)
	}

	return combined
}
