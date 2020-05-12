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
	"io/ioutil"
	"path/filepath"
	"time"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"

	"github.com/syndesisio/syndesis/install/operator/pkg/generator"

	"gopkg.in/yaml.v2"
)

type csv struct {
	config   *configuration.Config
	operator string
	body     []byte

	// Variable needed to build the CSV
	version  string
	maturity string

	// Dependant on whether it is community or productized
	name           string
	displayName    string
	support        string
	description    string
	maintainerName string
	maintainerMail string
	provider       string
}

type CSVOut struct {
	ApiVersion string
	Kind       string
	Metadata   Metadata
	Spec       Spec
}

type Spec struct {
	DisplayName               string
	Description               string
	Keywords                  []string
	Version                   string
	Maturity                  string
	Maintainers               []Maintainer
	Provider                  Provider
	Labels                    Labels
	Selector                  Selector
	Icon                      []Icon
	Links                     []Link
	RelatedImages             []Image
	InstallModes              []InstallMode
	Install                   Install
	Customresourcedefinitions CustomResourceDefinitions
}

type Metadata struct {
	Name        string
	Namespace   string
	Annotations MetadataAnnotations
}

type MetadataAnnotations struct {
	Capabilities   string
	Categories     string
	Certified      string
	CreatedAt      string
	ContainerImage string
	Support        string
	Description    string
	Repository     string
	Alm            string
}

type Maintainer struct {
	Name  string
	Email string
}

type Provider struct {
	Name string
}

type Labels struct {
	Name string
}

type Selector struct {
	MatchLabels Label
}

type Link struct {
	Name string
	Url  string
}

type Image struct {
	Name   string
	Images string
}

type InstallMode struct {
	Type      string
	Supported bool
}
type Label struct {
	Name string
}

type Icon struct {
	Base64data string
	Mediatype  string
}

type Install struct {
	Strategy string
	Spec     InstallSpec
}

type InstallSpec struct {
	Permissions []InstallSpecPermission
	Deployments []InstallSpecDeployment
}

type InstallSpecPermission struct {
	ServiceAccountName string
	Rules              interface{}
}

type InstallSpecDeployment struct {
	Name string
	Spec interface{}
}

type CustomResourceDefinitions struct {
	Owned []CustomResourceDefinition
}

type CustomResourceDefinition struct {
	Name        string
	Version     string
	Kind        string
	DisplayName string
	Description string
}

// In order to build the body for both upstream and downstream,
// set variables accordingly
func (c *csv) setVariables() {
	c.version = c.config.Version
	c.maturity = "alpha"

	// Dependant on whether it is community or productized
	c.name = "fuse-online-operator"
	c.displayName = "Red Hat Integration - Fuse Online"
	c.support = "Fuse Online"
	c.description = "Manages the installation of Fuse Online, a flexible and customizable open source platform that provides core integration capabilities as a service."
	c.maintainerName = "Jon Anstey"
	c.maintainerMail = "janstey@redhat.com"
	c.provider = "Red Hat"

	if !c.config.Productized {
		c.name = "syndesis-operator"
		c.displayName = "Syndesis"
		c.support = "Syndesis"
		c.description = "Manages the installation of Syndesis, a flexible and customizable open source platform that provides core integration capabilities as a service."
		c.maintainerName = "Syndesis team"
		c.maintainerMail = "syndesis@googlegroups.com"
		c.provider = "Syndesis team"
	}
}

// Build the content of the csv file
func (c *csv) build() (err error) {
	target := "productized"
	if !c.config.Productized {
		target = "community"
	}
	c.setVariables()

	alm, err := ioutil.ReadFile(filepath.Join("pkg", "syndesis", "olm", "assets", "alm-examples"))
	descriptionLong, _ := ioutil.ReadFile(filepath.Join("pkg", "syndesis", "olm", "assets", target, "description"))
	icon, _ := ioutil.ReadFile(filepath.Join("pkg", "syndesis", "olm", "assets", "icon"))
	rules, err := c.loadRoleFromTemplate()
	if err != nil {
		return err
	}

	deployment, err := c.loadDeploymentFromTemplate()
	if err != nil {
		return err
	}

	co := CSVOut{
		ApiVersion: "operators.coreos.com/v1alpha1",
		Kind:       "ClusterServiceVersion",
		Metadata: Metadata{
			Name:      c.name + ".v" + c.version,
			Namespace: "placeholder",
			Annotations: MetadataAnnotations{
				Capabilities:   "Seamless Upgrades",
				Categories:     "Integration & Delivery",
				Certified:      "false",
				CreatedAt:      time.Now().String(),
				ContainerImage: c.operator,
				Support:        c.support,
				Description:    c.description,
				Repository:     "https://github.com/syndesisio/syndesis/",
				Alm:            string(alm),
			},
		},
		Spec: Spec{
			DisplayName: c.displayName,
			Description: string(descriptionLong),
			Keywords:    []string{"camel", "integration", "syndesis", "fuse", "online"},
			Version:     c.version,
			Maturity:    c.maturity,
			Maintainers: []Maintainer{
				{
					Name:  c.maintainerName,
					Email: c.maintainerMail,
				},
			},
			Provider: Provider{Name: c.provider},
			Labels:   Labels{Name: c.name},
			Selector: Selector{MatchLabels: Label{Name: c.name}},
			Icon: []Icon{
				{
					Base64data: string(icon),
					Mediatype:  "image/svg+xml",
				},
			},
			Links: []Link{
				{
					Name: "Red Hat Fuse Online Documentation",
					Url:  "https://access.redhat.com/documentation/en-us/red-hat-fuse",
				}, {
					Name: "Upstream project Syndesis",
					Url:  "https://github.com/syndesisio/syndesis",
				}, {
					Name: "Upstream Syndesis Operator",
					Url:  "https://github.com/syndesisio/syndesis/tree/master/install/operator",
				},
			},
			RelatedImages: []Image{
				{
					Name:   "DV",
					Images: c.config.Syndesis.Addons.DV.Image,
				},
				{
					Name:   "Oauth",
					Images: c.config.Syndesis.Components.Oauth.Image,
				},
				{
					Name:   "UI",
					Images: c.config.Syndesis.Components.UI.Image,
				},
				{
					Name:   "S2I",
					Images: c.config.Syndesis.Components.S2I.Image,
				},
				{
					Name:   "Prometheus",
					Images: c.config.Syndesis.Components.Prometheus.Image,
				},
				{
					Name:   "Upgrade",
					Images: c.config.Syndesis.Components.Upgrade.Image,
				},
				{
					Name:   "Postgres",
					Images: c.config.Syndesis.Components.Database.Image,
				},
				{
					Name:   "Exporter",
					Images: c.config.Syndesis.Components.Database.Exporter.Image,
				},
				{
					Name:   "Server",
					Images: c.config.Syndesis.Components.Server.Image,
				},
				{
					Name:   "CamelK",
					Images: c.config.Syndesis.Addons.CamelK.Image,
				},
				{
					Name:   "Jaeger Agent",
					Images: c.config.Syndesis.Addons.Jaeger.ImageAgent,
				},
				{
					Name:   "Jaeger Operator",
					Images: c.config.Syndesis.Addons.Jaeger.ImageOperator,
				},
				{
					Name:   "Jaeger",
					Images: c.config.Syndesis.Addons.Jaeger.ImageAllInOne,
				},
				{
					Name:   "Todo",
					Images: c.config.Syndesis.Addons.Todo.Image,
				},
				{
					Name:   "AMQ",
					Images: c.config.Syndesis.Components.AMQ.Image,
				},
			},
			InstallModes: []InstallMode{
				{
					Type:      "OwnNamespace",
					Supported: true,
				}, {
					Type:      "SingleNamespace",
					Supported: true,
				}, {
					Type:      "MultiNamespace",
					Supported: false,
				}, {
					Type:      "AllNamespaces",
					Supported: false,
				},
			},
			Install: Install{
				Strategy: "deployment",
				Spec: InstallSpec{
					Permissions: []InstallSpecPermission{{
						ServiceAccountName: "syndesis-operator",
						Rules:              rules,
					}},
					Deployments: []InstallSpecDeployment{{
						Name: c.name,
						Spec: deployment,
					}},
				},
			},
			Customresourcedefinitions: CustomResourceDefinitions{
				Owned: []CustomResourceDefinition{{
					Name:        "syndesises.syndesis.io",
					Version:     "v1beta1",
					Kind:        "Syndesis",
					DisplayName: "Syndesis CRD",
					Description: "Syndesis CRD",
				}},
			},
		},
	}

	c.body, err = yaml.Marshal(co)
	return
}

// Load role to apply to syndesis operator, from template file. This role
// is later applied to the syndesis-operator service account, by OLM
func (c *csv) loadRoleFromTemplate() (r interface{}, err error) {
	context := struct {
		Kind string
		Role string
	}{
		Kind: "",
		Role: "",
	}

	g, err := generator.Render("./install/role.yml.tmpl", context)
	if err != nil {
		return nil, err
	}

	mjson, err := g[0].MarshalJSON()
	if err != nil {
		return nil, err
	}

	m := make(map[string]interface{})
	if err := yaml.Unmarshal(mjson, &m); err != nil {
		return nil, err
	}

	r = m["rules"]
	return
}

// Load syndesis-operator deployment from template file
func (c *csv) loadDeploymentFromTemplate() (r interface{}, err error) {
	context := struct {
		DatabaseImage string
		OperatorImage string
	}{
		DatabaseImage: c.config.Syndesis.Components.Database.Image,
		OperatorImage: "",
	}

	g, err := generator.Render("./install/deployment.yml.tmpl", context)
	if err != nil {
		return nil, err
	}

	mjson, err := g[0].MarshalJSON()
	if err != nil {
		return nil, err
	}

	m := make(map[string]interface{})
	if err := yaml.Unmarshal(mjson, &m); err != nil {
		return nil, err
	}

	r = m["spec"]
	return
}
