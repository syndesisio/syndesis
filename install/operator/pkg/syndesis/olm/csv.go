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
	"path/filepath"
	"time"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/capabilities"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"

	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"

	"gopkg.in/yaml.v2"
)

type csv struct {
	config *configuration.Config
	image  string
	tag    string
	body   []byte

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
	ApiVersion string `yaml:"apiVersion"`
	Kind       string
	Metadata   Metadata
	Spec       Spec
}

type Spec struct {
	DisplayName               string `yaml:"displayName"`
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
	InstallModes              []InstallMode `yaml:"installModes"`
	Install                   Install
	Customresourcedefinitions CustomResourceDefinitions
	RelatedImages             []Image `yaml:"relatedImages"`
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
	CreatedAt      string `yaml:"createdAt"`
	ContainerImage string `yaml:"containerImage"`
	Support        string
	Description    string
	Repository     string
	AlmExamples    string `yaml:"alm-examples"`
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
	Name  string
	Image string
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
	ClusterPermissions []InstallSpecPermission
	Permissions        []InstallSpecPermission
	Deployments        []InstallSpecDeployment
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

	// Both of these are required to ensure permissions are correctly added to manifest
	c.config.ApiServer.ImageStreams = true
	c.config.ApiServer.Routes = true
	c.config.ApiServer.EmbeddedProvider = true
	c.config.ApiServer.OlmSupport = true
	c.config.ApiServer.ConsoleLink = true

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

	alm, err := Read("assets/alm-examples")
	if err != nil {
		return err
	}
	descriptionLong, err := Read(filepath.Join("assets/", target, "description"))
	if err != nil {
		return err
	}
	icon, err := Read("assets/icon")
	if err != nil {
		return err
	}

	ruleSpecs := []InstallSpecPermission{}
	clusterRuleSpecs := []InstallSpecPermission{}

	//
	// syndesis-operator service account
	//
	if synOpPerm, err := c.installPerm("syndesis-operator", "assets/install/role.yml.tmpl"); err == nil {
		ruleSpecs = append(ruleSpecs, synOpPerm)
	} else {
		return err
	}

	//
	// Gives syndesis-operator necessary cluster-level privileges,
	// inc. creating the clusterrolebindings for the operand service accounts
	//
	if synOpPerm, err := c.installPerm(
		"syndesis-operator",
		"assets/install/cluster_role_olm.yml.tmpl",
		"assets/install/cluster_role_kafka.yml.tmpl",
		"assets/install/cluster_role_oauthproxy.yml.tmpl",
		"assets/install/cluster_role_public_api.yml.tmpl"); err == nil {
		clusterRuleSpecs = append(clusterRuleSpecs, synOpPerm)
	} else {
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
				ContainerImage: fmt.Sprintf("%s:%s", c.image, c.tag),
				Support:        c.support,
				Description:    c.description,
				Repository:     "https://github.com/syndesisio/syndesis/",
				AlmExamples:    string(alm),
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
					ClusterPermissions: clusterRuleSpecs,
					Permissions:        ruleSpecs,
					Deployments: []InstallSpecDeployment{{
						Name: c.name,
						Spec: deployment,
					}},
				},
			},
			Customresourcedefinitions: CustomResourceDefinitions{
				Owned: []CustomResourceDefinition{{
					Name:        "syndesises.syndesis.io",
					Version:     "v1beta3",
					Kind:        "Syndesis",
					DisplayName: "Syndesis CRD",
					Description: "Syndesis CRD",
				}},
			},
			RelatedImages: []Image{}, // Has to be specified but populated downstream
		},
	}

	c.body, err = yaml.Marshal(co)
	return
}

func (c *csv) installPerm(sa string, templates ...string) (perm InstallSpecPermission, err error) {

	perm = InstallSpecPermission{
		ServiceAccountName: sa,
	}

	perm.Rules, err = c.loadRolesFromTemplate(templates...)
	return
}

func (c *csv) loadRolesFromTemplate(templates ...string) (m []map[string]interface{}, err error) {
	context := struct {
		Kind      string
		Role      string
		ApiServer capabilities.ApiServerSpec
	}{
		Kind:      "",
		Role:      "",
		ApiServer: c.config.ApiServer,
	}

	if len(templates) == 0 {
		return m, nil
	}

	m = make([]map[string]interface{}, 0, 0)
	for _, template := range templates {

		resources, err := generator.Render(template, context)
		if err != nil {
			return nil, err
		}

		if len(resources) == 0 {
			return nil, fmt.Errorf("Rendering of rule template %s is empty.", template)
		}

		for _, resource := range resources {
			rules, exists, _ := unstructured.NestedFieldNoCopy(resource.UnstructuredContent(), "rules")
			if !exists {
				return nil, fmt.Errorf("Cannot validate 'rules' in %s", resource.GetName())
			}

			ruleMaps, ok := rules.([]interface{})
			if !ok || len(ruleMaps) == 0 {
				return nil, fmt.Errorf("Cannot validate rule maps in %s", resource.GetName())
			}

			for _, ruleMap := range ruleMaps {
				ruleMap, ok := ruleMap.(map[string]interface{})
				if !ok {
					return nil, fmt.Errorf("Cannot validate 'rule map' in %s", resource.GetName())
				}
				m = append(m, ruleMap)
			}
		}
	}

	return
}

// Load syndesis-operator deployment from template file
func (c *csv) loadDeploymentFromTemplate() (r interface{}, err error) {
	context := struct {
		DatabaseImage   string
		Image           string
		Tag             string
		AmqImage        string
		TodoImage       string
		OauthImage      string
		UiImage         string
		S2iImage        string
		PrometheusImage string
		UpgradeImage    string
		MetaImage       string
		ServerImage     string
		ExporterImage   string
		DevSupport      bool
		LogLevel        int
	}{
		Image:           c.image,
		Tag:             c.tag,
		DatabaseImage:   c.config.Syndesis.Components.Database.Image,
		TodoImage:       c.config.Syndesis.Addons.Todo.Image,
		AmqImage:        c.config.Syndesis.Components.AMQ.Image,
		OauthImage:      c.config.Syndesis.Components.Oauth.Image,
		UiImage:         c.config.Syndesis.Components.UI.Image,
		MetaImage:       c.config.Syndesis.Components.Meta.Image,
		ServerImage:     c.config.Syndesis.Components.Server.Image,
		S2iImage:        c.config.Syndesis.Components.S2I.Image,
		PrometheusImage: c.config.Syndesis.Components.Prometheus.Image,
		UpgradeImage:    c.config.Syndesis.Components.Upgrade.Image,
		ExporterImage:   c.config.Syndesis.Components.Database.Exporter.Image,
		DevSupport:      false, // Never be true in CSV generation - here for template compatibility
		LogLevel:        0,     // Never to be more in CSV generation - here for template compatibility
	}

	g, err := generator.Render("assets/install/operator_deployment.yml.tmpl", context)
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
