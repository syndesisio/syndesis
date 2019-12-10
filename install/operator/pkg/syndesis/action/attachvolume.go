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

	"github.com/syndesisio/syndesis/install/operator/pkg"

	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/labels"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"k8s.io/client-go/kubernetes"
	"sigs.k8s.io/controller-runtime/pkg/manager"
)

// Reinstall operator resources to make sure the volume is mounted
type attachVolumeAction struct {
	baseAction
}

func newAttachVolumeAction(mgr manager.Manager, api kubernetes.Interface) SyndesisOperatorAction {
	return &attachVolumeAction{
		newBaseAction(mgr, api, "attachVolume"),
	}
}

func (a *attachVolumeAction) CanExecute(syndesis *v1alpha1.Syndesis) bool {
	return syndesisPhaseIs(syndesis,
		v1alpha1.SyndesisPhaseAttachingVolume,
	)
}

// Check whether a pvc is attached to the operator pod, if not, recreate the resource
func (a *attachVolumeAction) Execute(ctx context.Context, syndesis *v1alpha1.Syndesis) error {
	target := syndesis.DeepCopy()

	selector, err := labels.Parse("syndesis.io/app=syndesis,syndesis.io/component=syndesis-operator")
	if err != nil {
		return err
	}

	typeMeta := metav1.TypeMeta{Kind: "Pod", APIVersion: "v1"}
	options := client.ListOptions{
		Namespace:     syndesis.Namespace,
		LabelSelector: selector,
		Raw: &metav1.ListOptions{
			TypeMeta: typeMeta,
			Limit:    1,
		},
	}
	list := unstructured.UnstructuredList{
		Object: map[string]interface{}{
			"apiVersion": typeMeta.APIVersion,
			"kind":       typeMeta.Kind,
		},
	}
	err = util.ListInChunks(ctx, a.client, &options, &list, func(resources []unstructured.Unstructured) error {
		for _, res := range resources {
			if res.GetKind() == "Pod" {
				pod := &corev1.Pod{}
				if err := a.client.Get(ctx, client.ObjectKey{Namespace: syndesis.Namespace, Name: res.GetName()}, pod); err != nil {
					return err
				}

				var pvc *corev1.PersistentVolumeClaimVolumeSource
				for _, vol := range pod.Spec.Volumes {
					if vol.PersistentVolumeClaim != nil {
						pvc = vol.PersistentVolumeClaim
						break
					}
				}

				if pvc != nil {
					// PVC is attached we move on to the next phase depending on where
					// we come from
					target.Status.Version = pkg.DefaultOperatorTag
					switch target.Status.PreviousPhase {
					case v1alpha1.SyndesisPhaseMissing, v1alpha1.SyndesisPhaseNotInstalled:
						target.Status.Phase = v1alpha1.SyndesisPhaseInstalling
						target.Status.Reason = v1alpha1.SyndesisStatusReasonVolumeDetected
						target.Status.Description = fmt.Sprintf("PVC %s is attached to pod %s, proceed to install", pvc.ClaimName, pod.Name)

						a.log.Info("Operator volume attached", "syndesis", syndesis.Name, "version", pkg.DefaultOperatorTag)
					case v1alpha1.SyndesisPhaseInstalled:
						target.Status.Phase = v1alpha1.SyndesisPhaseUpgrading
						target.Status.Reason = v1alpha1.SyndesisStatusReasonMissing
						target.Status.Description = "Upgrading from " + syndesis.Status.Version + " to " + pkg.DefaultOperatorTag
						target.Status.LastUpgradeFailure = nil
						target.Status.UpgradeAttempts = 0
						target.Status.ForceUpgrade = false

						a.log.Info("Starting upgrade of Syndesis resource", "name", syndesis.Name, "currentVersion", syndesis.Status.Version, "targetVersion", pkg.DefaultOperatorTag)
					default:
						return fmt.Errorf("unsupported phase: %s", target.Status.PreviousPhase)
					}
					target.Status.PreviousPhase = v1alpha1.SyndesisPhaseMissing

				} else {
					configuration, err := configuration.GetProperties(configuration.TemplateConfig, ctx, a.client, syndesis)
					if err != nil {
						return err
					}
					context := struct {
						Image          string
						DevSupport     bool
						VolumeCapacity string
					}{
						Image:          pkg.DefaultOperatorImage + ":" + pkg.DefaultOperatorTag,
						DevSupport:     configuration.DevSupport,
						VolumeCapacity: configuration.Syndesis.Components.Operator.Resources.VolumeCapacity,
					}

					operator, err := generator.Render("./install/operator.yml.tmpl", context)
					if err != nil {
						return err
					}

					a.log.Info("Recreating resources and shutting down", "syndesis", syndesis.Name, "version", pkg.DefaultOperatorTag)
					for _, res := range operator {
						res.SetNamespace(syndesis.Namespace)
						_, modificationType, err := util.CreateOrUpdate(ctx, a.client, &res)
						if err != nil {
							a.log.Info("Failed to create or replace resource", "kind", res.GetKind(), "name", res.GetName(), "namespace", res.GetNamespace())
							return err
						} else {
							if modificationType != controllerutil.OperationResultNone {
								a.log.Info("resource "+string(modificationType), "kind", res.GetKind(), "name", res.GetName(), "namespace", res.GetNamespace())
							}
						}
					}
				}
			}
		}

		return nil
	})
	if err != nil {
		// We land here if we have issues fetching the pod, or figuring out if there is a volume attached
		target.Status.Phase = v1alpha1.SyndesisPhaseNotInstalled
		target.Status.Reason = v1alpha1.SyndesisStatusReasonCanNotDetectVolume
		target.Status.Description = "Cannot fetch syndesis operator pod"
		a.log.Error(nil, "Cannot validate whether a volume is attached to the operator pod", "err", err.Error())
	}

	return a.client.Update(ctx, target)
}
