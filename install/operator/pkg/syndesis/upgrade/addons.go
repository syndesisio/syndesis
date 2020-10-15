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
	synpkg "github.com/syndesisio/syndesis/install/operator/pkg"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta2"
	conf "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime/schema"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

const (
	JAEGER_OP = "jaeger-operator"
	JAEGER_CR = "syndesis-jaeger"
)

type tidyAddons struct {
	step
	syndesis *v1beta2.Syndesis
}

func newCleanupAddons(s step, syndesis *v1beta2.Syndesis) (ta *tidyAddons) {
	ta = &tidyAddons{s, syndesis}
	ta.name = "Cleanup Addons"
	return
}

func (ta *tidyAddons) canRun() (r bool) {
	r = ta.executed
	if r {
		ta.log.Info("Cleaning up addons previously done, skipping")
	}

	return !r
}

/*
 * run does nothing for install since install is a placeholder to rollback,
 * it is needed to restore the openshift resources
 */
func (ta *tidyAddons) run() (err error) {

	ta.log.Info("Cleaning up any old incompatible addons")

	config, err := conf.GetProperties(ta.context, conf.TemplateConfig, ta.clientTools, ta.syndesis)
	if err != nil {
		return err
	}

	addonsInfo := conf.GetAddonsInfo(*config)
	for _, addonInfo := range addonsInfo {
		if !addonInfo.IsEnabled() {
			continue
		}

		if addonInfo.IsVersionCompatible() {
			continue // Nothing required
		}

		ta.log.V(synpkg.DEBUG_LOGGING_LVL).Info("Addon not version compatible", "Name", addonInfo.Name())

		//
		// This addon is not version compatible so must be cleaned up
		// and allowed to be re-installed post-upgrade
		//
		if err := ta.cleanupAddon(addonInfo); err != nil {
			return err
		}
	}

	ta.executed = true
	return nil
}

func (ta *tidyAddons) cleanupAddon(addonInfo conf.AddonInfo) error {
	ta.log.V(synpkg.DEBUG_LOGGING_LVL).Info("Cleaning up addon", "Name", addonInfo.Name())

	//
	// Have to resort to an if statement here
	// Would be nice to move the addonInfo to
	// its own file and move this functionality
	// to its own function but a lot of work.
	// Maybe a TODO.
	//
	if addonInfo.Name() == "jaeger" {
		ta.log.V(synpkg.DEBUG_LOGGING_LVL).Info("Cleaning up jaeger addon")

		//
		// Removes the syndesis jaeger custom resource
		//
		jGvr := schema.GroupVersionResource{
			Group:    "jaegertracing.io",
			Version:  "v1",
			Resource: "jaegers",
		}

		jaegerCR, err := ta.dynClient().Resource(jGvr).Namespace(ta.namespace).Get(ta.context, JAEGER_CR, metav1.GetOptions{})
		if err == nil {
			err = ta.dynClient().Resource(jGvr).Namespace(ta.namespace).Delete(ta.context, jaegerCR.GetName(), metav1.DeleteOptions{})
			if err != nil {
				return err
			}
			ta.log.Info("Deleted the syndesis jaeger custom resource")
		} else if k8serrors.IsNotFound(err) {
			ta.log.V(synpkg.DEBUG_LOGGING_LVL).Info("No syndesis jaeger custom resource found ... skipping")
		} else {
			return err
		}

		//
		// Scales down then removes the
		// jaeger-operator deployment
		//
		dep := &appsv1.Deployment{}
		if err = ta.client().Get(ta.context, types.NamespacedName{Namespace: ta.namespace, Name: JAEGER_OP}, dep); err == nil {
			if !hasSyndesisOwner(dep.OwnerReferences) {
				ta.log.V(synpkg.DEBUG_LOGGING_LVL).Info("Found a jaeger operator deployment but not owned by Syndesis ... skipping")
			} else {

				// scale down the deployment
				if err := ta.scaleDownDeployment(dep.Name); err != nil {
					return err
				}

				err = ta.client().Delete(ta.context, dep)
				if err != nil {
					return err
				}
				ta.log.Info("Deleted the jaeger operater deployment")
			}
		} else if k8serrors.IsNotFound(err) {
			ta.log.V(synpkg.DEBUG_LOGGING_LVL).Info("No jaeger operator deployment found ... skipping")
		} else {
			return err
		}
	}

	//
	// Removes the jaeger-operator service account
	//
	sa := &corev1.ServiceAccount{}
	if err := ta.client().Get(ta.context, types.NamespacedName{Namespace: ta.namespace, Name: JAEGER_OP}, sa); err == nil {

		if !hasSyndesisOwner(sa.OwnerReferences) {
			ta.log.V(synpkg.DEBUG_LOGGING_LVL).Info("Found a jaeger operator service account but not owned by Syndesis ... skipping")
		} else {

			err = ta.client().Delete(ta.context, sa)
			if err != nil {
				return err
			}
			ta.log.Info("Deleted the jaeger operater service account")
		}

	} else if k8serrors.IsNotFound(err) {
		ta.log.V(synpkg.DEBUG_LOGGING_LVL).Info("No jaeger operator service account found ... skipping")
	} else {
		return err
	}

	return nil
}

// Patches the Deployment setting the number
// of replicas to 0
func (ta *tidyAddons) scaleDownDeployment(name string) error {
	ta.log.Info("Scaling down the deployment", "deployment", name)
	if err := ta.client().Patch(ta.context, &appsv1.Deployment{
		ObjectMeta: metav1.ObjectMeta{
			Name:      name,
			Namespace: ta.namespace,
		},
	}, client.RawPatch(types.MergePatchType, []byte(`{"spec":{"replicas":0}}`))); err != nil {
		return err
	}

	if err := ta.awaitScale(name, newDeploymentTracker()); err != nil {
		return err
	}

	return nil
}

func hasSyndesisOwner(owners []metav1.OwnerReference) bool {
	if len(owners) > 1 || len(owners) == 0 {
		// Should not delete this deployment as cannot be one of ours
		// since Syndesis only provides 1 ownerReference
		return false
	}

	// Don't own this deployment either so do not delete
	return owners[0].Kind == "Syndesis"
}

//
// There is no rollback for cleaning up addons
// as they will be reinstalled through syndesis CR
//
func (ta *tidyAddons) canRollback() (r bool) {
	return false
}

func (ta *tidyAddons) rollback() (err error) {
	return
}
