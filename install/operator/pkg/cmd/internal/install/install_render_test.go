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

package install

import (
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"strings"
	"testing"
)

func TestInstallResourcesRender(t *testing.T) {

	f, err := generator.GetAssetsFS().Open("./install")
	require.NoError(t, err)
	defer f.Close()

	files, err := f.Readdir(-1)
	require.NoError(t, err)

	for _, f := range files {

		if strings.HasPrefix(f.Name(), "grant_") {
			continue // skip these.. Not testing the grant resource rendering..
		}

		o := Install{
			Options: &internal.Options{
				Namespace: "syndesis",
			},
			image:      "syndesis-operator",
			tag:        "latest",
			devSupport: true,
		}
		resources, err := o.render("./install/" + f.Name())
		require.NoError(t, err)
		assert.NotEqual(t, 0, len(resources))
	}
}
