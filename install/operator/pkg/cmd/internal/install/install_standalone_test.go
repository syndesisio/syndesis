/*
 *
 *  * Copyright (C) 2019 Red Hat, Inc.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package install

import (
	"context"
	"testing"

	"k8s.io/apimachinery/pkg/runtime"
	"sigs.k8s.io/controller-runtime/pkg/client/fake"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"

	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
)

const (
	configLocation = "../../../../build/conf/config.yaml"
)

func TestEject(t *testing.T) {
	if testing.Short() {
		t.Skip("skipping operator install tests in short mode")
	}

	ctx := context.TODO()

	// Create a fake client to mock API calls and pass it to the cmd
	objs := []runtime.Object{}
	cl := fake.NewFakeClient(objs...)

	i := &Install{eject: "yaml", Options: &internal.Options{Namespace: ns, Context: ctx}}
	i.Client = &cl

	configuration.TemplateConfig = configLocation

	t.Logf("\tTest: When running `operator install standalone --eject yaml`, it should eject the syndesis resources in a yaml format")
	if err := i.installStandalone(); err != nil {
		t.Fatalf("\t%s\t Got an error when ejecting resources: [%v]", failed, err)
	} else {
		t.Logf("\t%s\t Resources ejected without errors", succeed)
		if i.ejectedResources == nil {
			t.Fatalf("\t%s\t Resources should be different than nil, but got nil", failed)
		}
		t.Logf("\t%s\t Resources aren't empty", succeed)
	}

	i.eject = "json"
	i.ejectedResources = nil
	t.Logf("\tTest: When running `operator install standalone --eject json`, it should eject the syndesis resources in a json format")
	if err := i.installStandalone(); err != nil {
		t.Fatalf("\t%s\t Got an error when ejecting resources: [%v]", failed, err)
	} else {
		t.Logf("\t%s\t Resources ejected without errors", succeed)
		if i.ejectedResources == nil {
			t.Fatalf("\t%s\t Resources should be different than nil, but got nil", failed)
		}
		t.Logf("\t%s\t Resources aren't empty", succeed)
	}
}
