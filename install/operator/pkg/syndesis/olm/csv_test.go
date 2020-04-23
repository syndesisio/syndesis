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
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	syntesting "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/testing"
)

func Test_csv_build(t *testing.T) {
	clientTools := syntesting.FakeClientTools()
	conf, err := configuration.GetProperties(context.TODO(), "../../../build/conf/config-test.yaml", clientTools, &v1beta1.Syndesis{})
	assert.NoError(t, err)

	c := &csv{config: conf, operator: "operator-image"}
	err = c.build()
	assert.NoError(t, err)
	// fmt.Println(string(c.body))
}

func Test_csv_setCommunityVariables(t *testing.T) {
	type fields struct {
		config   *configuration.Config
		operator string
		want     *csv
	}
	tests := []struct {
		name   string
		fields fields
	}{
		{
			name: "For upstream",
			fields: fields{
				config:   &configuration.Config{Productized: false, Version: "7.7.0"},
				operator: "",
				want: &csv{
					version:        "7.7.0",
					maturity:       "alpha",
					name:           "syndesis-operator",
					displayName:    "Syndesis",
					support:        "Syndesis",
					description:    "Manages the installation of Syndesis, a flexible and customizable open source platform that provides core integration capabilities as a service.",
					maintainerName: "Syndesis team",
					maintainerMail: "syndesis@googlegroups.com",
					provider:       "Syndesis team",
				},
			},
		},
		{
			name: "For Downstream",
			fields: fields{
				config:   &configuration.Config{Productized: true, Version: "7.7.0"},
				operator: "",
				want: &csv{
					version:        "7.7.0",
					maturity:       "alpha",
					name:           "fuse-online-operator",
					displayName:    "Red Hat Integration - Fuse Online",
					support:        "Fuse Online",
					description:    "Manages the installation of Fuse Online, a flexible and customizable open source platform that provides core integration capabilities as a service.",
					maintainerName: "Jon Anstey",
					maintainerMail: "janstey@redhat.com",
					provider:       "Red Hat",
				},
			},
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			c := &csv{
				config:   tt.fields.config,
				operator: tt.fields.operator,
			}
			c.setVariables()
			c.config = nil
			assert.Equal(t, tt.fields.want, c)
		})
	}
}

func Test_csv_loadDeploymentFromTemplate(t *testing.T) {
	clientTools := syntesting.FakeClientTools()
	conf, err := configuration.GetProperties(context.TODO(), "../../../build/conf/config-test.yaml", clientTools, &v1beta1.Syndesis{})
	assert.NoError(t, err)

	c := &csv{config: conf, operator: ""}
	d, err := c.loadDeploymentFromTemplate()
	assert.NoError(t, err)
	assert.NotNil(t, d)
}

func Test_csv_loadRoleFromTemplate(t *testing.T) {
	clientTools := syntesting.FakeClientTools()
	conf, err := configuration.GetProperties(context.TODO(), "../../../build/conf/config-test.yaml", clientTools, &v1beta1.Syndesis{})
	assert.NoError(t, err)

	c := &csv{config: conf, operator: ""}
	r, err := c.loadRoleFromTemplate()
	assert.NoError(t, err)
	assert.NotNil(t, r)
}
