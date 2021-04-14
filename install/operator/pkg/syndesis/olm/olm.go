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
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	logf "sigs.k8s.io/controller-runtime/pkg/log"
)

var log = logf.Log.WithName("olm")

type BundleGenerator interface {
	Generate() (err error)
}

// Build the bundle generator
func Build(config *configuration.Config, path string, image string, tag string) (m BundleGenerator) {
	m = manifest{
		config:     config,
		path:       path,
		operator:   fmt.Sprintf("%s:%s", image, tag),
		csv:        &csv{config: config, image: image, tag: tag},
		crd:        &crd{},
		croles:     &croles{},
		annotation: &annotation{config: config},
		docker:     &docker{config: config},
		container:  &container{},
	}

	return
}

type manifest struct {
	config   *configuration.Config
	path     string
	operator string

	csv        *csv
	crd        *crd
	croles     *croles
	annotation *annotation
	docker     *docker
	container  *container
}

// Generate Cluster Service Version, CRD and Annotations
// and write the bundle out
//
// https://github.com/operator-framework/operator-registry#manifest-format
func (m manifest) Generate() (err error) {
	if err = m.csv.build(); err != nil {
		return err
	}

	if err = m.crd.build(); err != nil {
		return err
	}

	if err = m.croles.build(); err != nil {
		return err
	}

	if err = m.annotation.build(); err != nil {
		return err
	}

	if err = m.docker.build(); err != nil {
		return err
	}

	if err = m.container.build(); err != nil {
		return err
	}

	if err = m.ensureDir(); err != nil {
		return err
	}

	if err = ioutil.WriteFile(filepath.Join(m.path, m.config.Version, "manifests", "syndesis.crd.yaml"), m.crd.body, 0644); err != nil {
		return err
	}

	if err = ioutil.WriteFile(filepath.Join(m.path, m.config.Version, "manifests", "syndesis.clusterserviceversion.yaml"), m.csv.body, 0644); err != nil {
		return err
	}

	if err = ioutil.WriteFile(filepath.Join(m.path, m.config.Version, "manifests", "syndesis.clusterroles.yaml"), m.croles.body, 0644); err != nil {
		return err
	}

	if err = ioutil.WriteFile(filepath.Join(m.path, m.config.Version, "metadata", "annotations.yaml"), m.annotation.body, 0644); err != nil {
		return err
	}

	if err = ioutil.WriteFile(filepath.Join(m.path, m.config.Version, "Dockerfile"), m.docker.body, 0644); err != nil {
		return err
	}

	if err = ioutil.WriteFile(filepath.Join(m.path, "container.yaml"), m.container.body, 0644); err != nil {
		return err
	}

	return
}

// Generate directory tree for OLM bundle. Path must already
// exist
func (m manifest) ensureDir() (err error) {
	if _, err := os.Stat(m.path); os.IsNotExist(err) {
		log.Info("output path doesn't exist or is not accessible", "path", m.path)
		return err
	}

	err = os.Mkdir(filepath.Join(m.path), 0755)
	if err != nil && !os.IsExist(err) {
		return err
	}

	err = os.Mkdir(filepath.Join(m.path, m.config.Version), 0755)
	if err != nil && !os.IsExist(err) {
		return err
	}

	err = os.Mkdir(filepath.Join(m.path, m.config.Version, "manifests"), 0755)
	if err != nil && !os.IsExist(err) {
		return err
	}

	err = os.Mkdir(filepath.Join(m.path, m.config.Version, "metadata"), 0755)
	if err != nil && !os.IsExist(err) {
		return err
	}

	return nil
}
