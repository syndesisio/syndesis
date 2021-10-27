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

package v1beta3

import (
	"testing"

	"github.com/stretchr/testify/assert"
	corev1 "k8s.io/api/core/v1"
)

func TestMarshallingSchedulingSpecNoIndent(t *testing.T) {
	spec := SchedulingSpec{
		Affinity: &corev1.Affinity{
			NodeAffinity: &corev1.NodeAffinity{
				RequiredDuringSchedulingIgnoredDuringExecution: &corev1.NodeSelector{
					NodeSelectorTerms: []corev1.NodeSelectorTerm{
						{
							MatchExpressions: []corev1.NodeSelectorRequirement{{
								Key:      "hello",
								Operator: "eq",
								Values:   []string{"world"},
							}},
							MatchFields: []corev1.NodeSelectorRequirement{},
						},
					},
				},
			},
		},
	}

	marshalled := spec.Marshall(0)

	assert.Equal(t, `affinity:
  nodeAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
      nodeSelectorTerms:
      - matchExpressions:
        - key: hello
          operator: eq
          values:
          - world`, marshalled)
}

func TestMarshallingSchedulingSpecSomeIndent(t *testing.T) {
	spec := SchedulingSpec{
		Affinity: &corev1.Affinity{
			NodeAffinity: &corev1.NodeAffinity{
				RequiredDuringSchedulingIgnoredDuringExecution: &corev1.NodeSelector{
					NodeSelectorTerms: []corev1.NodeSelectorTerm{
						{
							MatchExpressions: []corev1.NodeSelectorRequirement{{
								Key:      "hello",
								Operator: "eq",
								Values:   []string{"world"},
							}},
							MatchFields: []corev1.NodeSelectorRequirement{},
						},
					},
				},
			},
		},
	}

	marshalled := spec.Marshall(6)

	assert.Equal(t, `      affinity:
        nodeAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
            nodeSelectorTerms:
            - matchExpressions:
              - key: hello
                operator: eq
                values:
                - world`, marshalled)
}

func TestMarshallingEmptySchedulingSpecEmptyResult(t *testing.T) {
	spec := SchedulingSpec{}

	marshalled := spec.Marshall(0)

	assert.Equal(t, "", marshalled)
}
