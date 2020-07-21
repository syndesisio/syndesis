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

package olm

import (
	"context"
	"io/ioutil"
	"os"
	"path/filepath"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	syntesting "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/testing"
)

func TestManifest_Generate(t *testing.T) {
	if testing.Short() {
		t.Skip("skipping tests in short mode")
	}

	clientTools := syntesting.FakeClientTools()
	conf, err := configuration.GetProperties(context.TODO(), "../../../build/conf/config-test.yaml", clientTools, &v1beta1.Syndesis{})
	assert.NoError(t, err)

	dir, err := ioutil.TempDir("/tmp", "olm-generate-test-")
	if err != nil {
		t.FailNow()
	}
	defer os.RemoveAll(dir)
	os.Chmod(dir, 0755)

	m := manifest{
		config:     conf,
		path:       dir,
		crd:        &crd{},
		annotation: &annotation{config: conf},
		csv:        &csv{config: conf, operator: "operator"},
	}
	err = m.Generate()
	assert.DirExists(t, filepath.Join(dir, m.config.Version, "manifests"))
	assert.FileExists(t, filepath.Join(dir, m.config.Version, "manifests", "syndesis.crd.yaml"))
	assert.FileExists(t, filepath.Join(dir, m.config.Version, "manifests", "syndesis.clusterserviceversion.yaml"))
	assert.DirExists(t, filepath.Join(dir, m.config.Version, "metadata"))
	assert.FileExists(t, filepath.Join(dir, m.config.Version, "metadata", "annotations.yaml"))

	assert.NoError(t, err)
}

func TestManifest_ensureDir(t *testing.T) {
	if testing.Short() {
		t.Skip("skipping tests in short mode")
	}

	clientTools := syntesting.FakeClientTools()
	conf, err := configuration.GetProperties(context.TODO(), "../../../build/conf/config-test.yaml", clientTools, &v1beta1.Syndesis{})
	assert.NoError(t, err)

	dir, err := ioutil.TempDir("/tmp", "olm-generate-test-")
	if err != nil {
		t.FailNow()
	}
	defer os.RemoveAll(dir)
	os.Chmod(dir, 0755)

	m := manifest{config: conf, path: dir}
	err = m.ensureDir()
	assert.NoError(t, err)
	assert.DirExists(t, filepath.Join(dir, m.config.Version, "manifests"))
	assert.DirExists(t, filepath.Join(dir, m.config.Version, "metadata"))

	m.path = "this dir does not exist"
	err = m.ensureDir()
	assert.Error(t, err)
}
