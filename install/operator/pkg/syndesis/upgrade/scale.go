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
	"time"

	appsv1 "k8s.io/api/apps/v1"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/apimachinery/pkg/util/wait"
)

const (
	up direction = iota
	down
)

type direction int

type scale struct {
	step
	dir      direction
	timeout  time.Duration
	interval time.Duration
}

func newScale(base step) (s *scale) {
	s = &scale{
		step:     base,
		dir:      up,
		timeout:  time.Second * 360,
		interval: time.Second * 10,
	}
	s.name = "Scale " + dirToS(s.dir)

	return
}

/*
 * The first step to perform is to stop all pods which can change the backend state of Syndesis so that there are no race conditions during the upgrade.
 *
 * Stopping should be performed by scaling down to 0 replicas for these pods and wait until they have been shut down in a controlled manner.
 * Ideally, the UI will show a maintenance screen when in upgrade mode. (But the UI should show a global error anyway when the backend Syndesis-server is not available).
 */
func (s *scale) run() (err error) {
	err = s.scale()
	return
}

/*
 * Rollback performs the same action as run, we need to ensure that replicas are set to 0 before starting a rollback, and afterwards
 * they should be scaled to 1
 */
func (s *scale) rollback() (err error) {
	err = s.scale()
	return
}

func (s *scale) down() (r *scale) {
	s.dir = down
	s.name = "Scale " + dirToS(down)
	return s
}

func (s *scale) up() (r *scale) {
	s.dir = up
	s.name = "Scale " + dirToS(up)
	return s
}

func dirToS(d direction) string {
	return [...]string{"up", "down"}[d]
}

// Scale Deployments up or down
func (s *scale) scale() (err error) {
	var replicas int32
	var deps = []string{"syndesis-meta", "syndesis-server"}

	if s.dir == up {
		replicas = 1
	}

	rtClient, err := s.clientTools.RuntimeClient()
	if err != nil {
		return err
	}

	// Scale up or down
	s.log.Info("scale Deployment", "direction", dirToS(s.dir), "deployments", deps)
	for _, dn := range deps {
		dep := &appsv1.Deployment{}
		if err = rtClient.Get(s.context, types.NamespacedName{Namespace: s.namespace, Name: dn}, dep); err != nil {
			return err
		}

		if *dep.Spec.Replicas != replicas {
			s.log.Info("scaling Deployments", "name", dn, "desired replicas", replicas, "replicas", dep.Spec.Replicas)
			dep.Spec.Replicas = &replicas
			if err = rtClient.Update(s.context, dep); err != nil {
				return err
			}
		}
	}

	// Wait for Deployments to correctly scale
	s.log.Info("waiting for Deployment to scale", "direction", dirToS(s.dir), "deployments", deps)
	err = wait.Poll(s.interval, s.timeout, func() (done bool, err error) {
		for i, dn := range deps {
			dep := &appsv1.Deployment{}
			if err = rtClient.Get(s.context, types.NamespacedName{Namespace: s.namespace, Name: dn}, dep); err != nil {
				return false, err
			}

			if dep.Status.AvailableReplicas == replicas {
				s.log.Info("deployment successfully scaled", "name", dn, "desired replicas", replicas, "available replicas", dep.Status.AvailableReplicas)
				if len(deps) == 1 {
					deps = deps[:len(deps)-1]
				} else {
					deps = append(deps[:i], deps[i+1:]...)
				}
			} else {
				s.log.Info("waiting for Deployment to reach desired number of replicas", "name", dn, "desired replicas", replicas, "available replicas", dep.Status.AvailableReplicas)
			}

			if len(deps) == 0 {
				return true, nil
			}
		}

		return false, nil
	})

	return
}

/*
 * Scale should always run and rollback, we want the Deployments replicas set to one
 * even if some steps failed rolling back
 */
func (s *scale) canRollback() bool {
	return true
}

func (s *scale) canRun() bool {
	return true
}
