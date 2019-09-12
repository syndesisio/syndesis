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

package eject

import (
	"context"
	"testing"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"

	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
)

const (
	ns             = "namespace"
	configLocation = "../../../../build/conf/config.yaml"
	succeed        = "\u2713"
	failed         = "\u2717"
)

func TestEject(t *testing.T) {
	ctx := context.TODO()
	e := &Eject{out: "yaml", Options: &internal.Options{Namespace: ns, Context: ctx}}
	configuration.TemplateConfig = configLocation

	t.Logf("\tTest: When running `operator eject --out yaml`, it should eject the syndesis resources in a yaml format")
	if err := e.eject(); err != nil {
		t.Fatalf("\t%s\t Got an error when ejecting resources: [%v]", failed, err)
	} else {
		t.Logf("\t%s\t Resources ejected without errors", succeed)
		if e.ejectedResources == nil {
			t.Fatalf("\t%s\t Resources should be different than nil, but got nil", failed)
		}
		t.Logf("\t%s\t Resources aren't empty", succeed)
	}

	e.out = "json"
	e.ejectedResources = nil
	t.Logf("\tTest: When running `operator eject --out json`, it should eject the syndesis resources in a json format")
	if err := e.eject(); err != nil {
		t.Fatalf("\t%s\t Got an error when ejecting resources: [%v]", failed, err)
	} else {
		t.Logf("\t%s\t Resources ejected without errors", succeed)
		if e.ejectedResources == nil {
			t.Fatalf("\t%s\t Resources should be different than nil, but got nil", failed)
		}
		t.Logf("\t%s\t Resources aren't empty", succeed)
	}
}
