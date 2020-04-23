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
	v1 "github.com/openshift/api/build/v1"

	v12 "github.com/openshift/api/apps/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type cleanup struct {
	step
}

func newCleanup(base step) (c *cleanup) {
	c = &cleanup{base}
	c.name = "Cleanup"
	return c
}

/*
 * TODO: Delete for 1.10
 * Because: Updating the DeploymentConfigs doesnt delete the existing triggers, so
 * the DC continues triggering redeploys on imageStreamTag change but it is not what we want
 * since we are using docker images
 */
func (c *cleanup) run() (err error) {
	if err = c.deleteDeploymentConfigs(); err != nil {
		return err
	}

	if err = c.deleteBuildConfigs(); err != nil {
		return err
	}

	return nil
}

func (c *cleanup) deleteDeploymentConfigs() (err error) {
	rtClient, err := c.clientTools.RuntimeClient()
	if err != nil {
		return err
	}

	for _, dcName := range []string{"syndesis-meta", "syndesis-server", "syndesis-ui", "syndesis-prometheus", "todo"} {
		dc := &v12.DeploymentConfig{}
		if err := rtClient.Get(c.context, client.ObjectKey{Name: dcName, Namespace: c.namespace}, dc); err != nil {
			if !k8serrors.IsNotFound(err) {
				c.log.Info(err.Error())
			}
		} else {
			if err := rtClient.Delete(c.context, dc); err != nil {
				c.log.Info(err.Error())
			} else {
				c.log.Info("force deleted DeploymentConfig", "name", dcName)
			}
		}
	}

	return nil
}

func (c *cleanup) deleteBuildConfigs() (err error) {
	rtClient, err := c.clientTools.RuntimeClient()
	if err != nil {
		return err
	}

	for _, bcName := range []string{"todo"} {
		bc := &v1.BuildConfig{}
		if err := rtClient.Get(c.context, client.ObjectKey{Name: bcName, Namespace: c.namespace}, bc); err != nil {
			if !k8serrors.IsNotFound(err) {
				c.log.Info(err.Error())
			}
		} else {
			if err := rtClient.Delete(c.context, bc); err != nil {
				c.log.Info(err.Error())
			} else {
				c.log.Info("force deleted BuildConfig", "name", bcName)
			}
		}
	}

	return nil
}

// No action rolling this back, since restoring the backup will bring the deleted resources up
func (c *cleanup) rollback() (err error) {
	return nil
}
