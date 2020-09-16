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

package v1beta2

import (
	"context"
	"errors"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

const DefaultNamespace string = "syndesis"

func NewSyndesis(namespace string) (*Syndesis, error) {
	if len(namespace) == 0 {
		return nil, errors.New("Creating a new Syndesis requires a namespace")
	}

	syndesis := &Syndesis{
		TypeMeta: metav1.TypeMeta{
			Kind:       "Syndesis",
			APIVersion: "syndesis.io/v1beta2",
		},
		ObjectMeta: metav1.ObjectMeta{
			Namespace: namespace,
		},
		Spec: SyndesisSpec{
			Components: ComponentsSpec{},
			Addons:     AddonsSpec{},
		},
		Status: SyndesisStatus{
			TargetVersion: "latest",
		},
	}

	return syndesis, nil
}

/*
 * Look at the existing namespace and return the installed syndesis CR
 */
func InstalledSyndesis(ctx context.Context, c client.Client, namespace string) (*Syndesis, error) {
	synList := &SyndesisList{}
	// List restricted to the namespace as user not necessarily has
	// permission to access all syndesises at the cluster level
	err := c.List(ctx, synList, client.InNamespace(namespace))
	if err != nil {
		return nil, err
	}

	syndesis, _ := NewSyndesis(namespace)
	for _, synRes := range synList.Items {
		//
		// TODO
		// Should usually be 1
		// In the event there is >1 then perhaps implement a choice switch to specify
		// the name of the required resource
		//

		//
		// Copy the installed syndesis as it will include
		// a valid UID but also properties such as ExternalDatabase
		//
		synRes.DeepCopyInto(syndesis)
		return syndesis, nil
	}

	// None are installed
	return nil, nil
}
