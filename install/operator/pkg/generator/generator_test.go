package generator_test

import (
	"encoding/json"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/build"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/api/resource"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"path/filepath"
	"strconv"
	"strings"
	"testing"

	v12 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func TestGenerator(t *testing.T) {

	templateConfig, err := util.LoadJsonFromFile(filepath.Join(build.GO_MOD_DIRECTORY, "build", "conf", "config.yaml"))
	require.NoError(t, err)

	// Parse the config
	gen := &generator.Context{}
	err = json.Unmarshal(templateConfig, gen)
	require.NoError(t, err)

	aTrue := true
	syndesis := &v1alpha1.Syndesis{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: "TEST",
		},
		Spec: v1alpha1.SyndesisSpec{
			RouteHostname:        "myhost.com",
			ImageStreamNamespace: "IMAGETEST",
			TestSupport:          &aTrue,
			DeployIntegrations:   &aTrue,
			Components: v1alpha1.ComponentsSpec{
				Db: v1alpha1.DbConfiguration{},
				Server: v1alpha1.ServerConfiguration{
					Resources: v1alpha1.Resources{
						ResourceRequirements: v12.ResourceRequirements{
							Limits: v12.ResourceList{
								"memory": resource.MustParse("3Gi"),
							},
						},
					},
					Features: v1alpha1.ServerFeatures{
						ManagementUrlFor3scale: "http://www.3scale.org/3scale",
					},
				},
				Oauth: v1alpha1.OauthConfiguration{
					Tag: "2.1.0",
				},
				Komodo: v1alpha1.KomodoConfiguration{
					Resources: v1alpha1.Resources{
						ResourceRequirements: v12.ResourceRequirements{
							Limits: v12.ResourceList{
								"memory": resource.MustParse("3Gi"),
							},
						},
					},
				},
			},
			Addons: v1alpha1.AddonsSpec{
				"komodo": {
					"enabled": "true",
				},
			},
		},
	}
	gen.Syndesis = syndesis

	err = template.SetupRenderContext(gen, syndesis, template.ResourceParams{}, map[string]string{})
	require.NoError(t, err)

	configuration.SetConfigurationFromEnvVars(gen.Env, syndesis)

	resources, err := generator.RenderFSDir(generator.GetAssetsFS(), "./infrastructure/", gen)
	require.NoError(t, err)
	assert.True(t, len(resources) > 0)

	//
	// Performs comparison checks on the marshalled resources in relation
	// to the source values found in the Syndesis Struct.
	//
	// It cannot strictly enforce some resource properties' existence since
	// legitimate resources share the same name but contain different properties.
	//
	// Therefore, this only concerns checking that the substitution worked
	// correctly so that assuming a property exists it is equal to the expected value.
	//
	checks := 0
	for _, resource := range resources {
		checks += checkSynMeta(t, resource, gen.Syndesis)
		checks += checkSynServer(t, resource, gen.Syndesis)
		checks += checkSynGlobalConfig(t, resource, gen.Syndesis)
		checks += checkSynUIConfig(t, resource, gen.Syndesis)
		checks += checkSynOAuthProxy(t, resource, gen.Syndesis)
	}
	assert.True(t, checks >= 6)

	resources, err = generator.RenderFSDir(generator.GetAssetsFS(), "./upgrade/", gen)
	require.NoError(t, err)
	assert.True(t, len(resources) > 0)

	resources, err = generator.RenderFSDir(generator.GetAssetsFS(), "./addons/todo/", gen)
	require.NoError(t, err)
	assert.True(t, len(resources) > 0)

	resources, err = generator.RenderFSDir(generator.GetAssetsFS(), "./addons/komodo/", gen)
	require.NoError(t, err)
	assert.True(t, len(resources) > 0)

	checks = 0
	for _, resource := range resources {
		checks += checkSynAddonKomodo(t, resource, gen.Syndesis)
	}
	assert.True(t, checks >= 1)
}

//
// Checks syndesis-meta resources have had syndesis
// object values correctly applied
//
func checkSynMeta(t *testing.T, resource unstructured.Unstructured, syndesis *v1alpha1.Syndesis) int {
	if resource.GetName() != "syndesis-meta" {
		return 0
	}

	assertResourcePropertyStr(t, resource, syndesis.Spec.Components.Meta.Resources.VolumeCapacity, "spec", "resources", "requests", "storage")

	//
	// Check the namespace defined in the triggers
	//
	triggers := sliceProperty(resource, "spec", "triggers")
	if triggers != nil {
		assertPropStr(t, triggers, syndesis.Spec.ImageStreamNamespace, "imageChangeParams", "from", "namespace")
	}

	return 1
}

//
// Checks syndesis-server resources have had syndesis
// object values correctly applied
//
func checkSynServer(t *testing.T, resource unstructured.Unstructured, syndesis *v1alpha1.Syndesis) int {
	if resource.GetName() != "syndesis-server" {
		return 0
	}

	container := sliceProperty(resource, "spec", "template", "spec", "containers")
	if container != nil {
		//
		// Compare the environment variables defined in the container
		//
		vars, exists, _ := unstructured.NestedSlice(container, "env")
		if exists {
			for _, s := range vars {
				if m, ok := s.(map[string]interface{}); ok {
					assertNameValueMap(t, m, "ENDPOINTS_TEST_SUPPORT_ENABLED", strconv.FormatBool(*syndesis.Spec.TestSupport))
					assertNameValueMap(t, m, "CONTROLLERS_INTEGRATION_ENABLED", strconv.FormatBool(*syndesis.Spec.DeployIntegrations))
					assertNameValueMap(t, m, "INTEGRATION_STATE_CHECK_INTERVAL", strconv.Itoa(*syndesis.Spec.Integration.StateCheckInterval))
					assertNameValueMap(t, m, "OPENSHIFT_MANAGEMENT_URL_FOR3SCALE", syndesis.Spec.Components.Server.Features.ManagementUrlFor3scale)
				}
			}
		}

		//
		// Compare the server memory limit which is set via the template function 'memoryLimit'
		//
		limits, lexists, _ := unstructured.NestedFieldNoCopy(container, "resources", "limits")
		if lexists {
			limitMap, ok := limits.(map[string]interface{})
			assert.True(t, ok)
			assert.Equal(t, syndesis.Spec.Components.Server.Resources.Limits.Memory().String(), limitMap["memory"])
		}
	}

	//
	// Check the namespace defined in the triggers
	//
	triggers := sliceProperty(resource, "spec", "triggers")
	if triggers != nil {
		assertPropStr(t, triggers, syndesis.Spec.ImageStreamNamespace, "imageChangeParams", "from", "namespace")
	}

	return 1
}

func checkSynGlobalConfig(t *testing.T, resource unstructured.Unstructured, syndesis *v1alpha1.Syndesis) int {
	if resource.GetName() != "syndesis-global-config" {
		return 0
	}

	params, exists, _ := unstructured.NestedFieldNoCopy(resource.UnstructuredContent(), "stringData", "params")
	if exists {
		paramsStr, ok := params.(string)
		assert.True(t, ok)
		assert.True(t, strings.Contains(paramsStr, "OPENSHIFT_OAUTH_CLIENT_SECRET="))
		assert.True(t, strings.Contains(paramsStr, "POSTGRESQL_PASSWORD="))
		assert.True(t, strings.Contains(paramsStr, "POSTGRESQL_SAMPLEDB_PASSWORD="))
		assert.True(t, strings.Contains(paramsStr, "OAUTH_COOKIE_SECRET="))
		assert.True(t, strings.Contains(paramsStr, "SYNDESIS_ENCRYPT_KEY="))
		assert.True(t, strings.Contains(paramsStr, "CLIENT_STATE_AUTHENTICATION_KEY="))
		assert.True(t, strings.Contains(paramsStr, "CLIENT_STATE_ENCRYPTION_KEY="))
	}

	return 1
}

func checkSynUIConfig(t *testing.T, resource unstructured.Unstructured, syndesis *v1alpha1.Syndesis) int {
	if resource.GetName() != "syndesis-ui-config" {
		return 0
	}

	config, exists, _ := unstructured.NestedString(resource.UnstructuredContent(), "data", "config.json")
	if exists {
		assert.True(t, strings.Contains(config, "\"enabled\": "+syndesis.Spec.Addons["komodo"]["enabled"]))
	}

	return 1
}

func checkSynAddonKomodo(t *testing.T, resource unstructured.Unstructured, syndesis *v1alpha1.Syndesis) int {
	if resource.GetName() != "syndesis-dv" {
		return 0
	}

	container := sliceProperty(resource, "spec", "template", "spec", "containers")
	if container != nil {
		//
		// Compare the komodo memory limit which is set via the template function 'memoryLimit'
		//
		limits, lexists, _ := unstructured.NestedFieldNoCopy(container, "resources", "limits")
		assert.True(t, lexists)
		limitMap, ok := limits.(map[string]interface{})
		assert.True(t, ok)
		assert.Equal(t, syndesis.Spec.Components.Komodo.Resources.Limits.Memory().String(), limitMap["memory"])
	}

	return 1
}

func checkSynOAuthProxy(t *testing.T, resource unstructured.Unstructured, syndesis *v1alpha1.Syndesis) int {
	if resource.GetName() != "oauth-proxy" {
		return 0
	}

	//
	// Check the tag name
	//
	tags := sliceProperty(resource, "spec", "tags")
	if tags != nil {
		assertPropStr(t, tags, syndesis.Spec.Components.Oauth.Tag, "name")
	}

	return 1
}
func assertNameValueMap(t *testing.T, m map[string]interface{}, name string, expected interface{}) int {
	field, ok := m["name"]
	if !ok || field != name {
		// Not found the correct map yet
		return 0
	}

	field, ok = m["value"]
	assert.True(t, ok, "Should be a value field mapped")
	assert.Equal(t, expected, field, "rendering should be applied correctly")
	return 1
}

func sliceProperty(resource unstructured.Unstructured, resourcePath ...string) map[string]interface{} {
	v, exists, _ := unstructured.NestedSlice(resource.UnstructuredContent(), resourcePath...)
	if !exists {
		return nil
	}

	if len(v) == 0 {
		return nil
	}

	if m, ok := v[0].(map[string]interface{}); ok {
		return m
	}

	return nil
}

func assertResourcePropertyStr(t *testing.T, resource unstructured.Unstructured, expected string, resourcePath ...string) {
	assertPropStr(t, resource.UnstructuredContent(), expected, resourcePath...)
}

func assertPropertyBool(t *testing.T, resource map[string]interface{}, expected bool, resourcePath ...string) {
	value, exists, _ := unstructured.NestedBool(resource, resourcePath...)
	if exists {
		assert.Equal(t, expected, value, "rendering should be applied correctly")
	}
}

func assertPropStr(t *testing.T, resource map[string]interface{}, expected string, resourcePath ...string) {
	value, exists, _ := unstructured.NestedString(resource, resourcePath...)
	if exists {
		assert.Equal(t, expected, value, "rendering should be applied correctly")
	}
}

func TestConfigYAML(t *testing.T) {
	templateConfig, err := util.LoadJsonFromFile(filepath.Join(build.GO_MOD_DIRECTORY, "build", "conf", "config.yaml"))
	require.NoError(t, err)

	// Parse the config
	gen := &generator.Context{}
	err = json.Unmarshal(templateConfig, gen)
	require.NoError(t, err)

	// Tags are mandatory as fallback in case CR and Secret dont have them defined
	assert.NotNil(t, gen.Tags.Syndesis, "Tags.Syndesis is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.Tags.Syndesis, "Tags.Syndesis is a mandatory field in config.yaml file")

	assert.NotNil(t, gen.Tags.Komodo, "Tags.Komodo is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.Tags.Komodo, "Tags.Komodo is a mandatory field in config.yaml file")

	assert.NotNil(t, gen.Tags.OAuthProxy, "Tags.OAuthProxy is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.Tags.OAuthProxy, "Tags.OAuthProxy is a mandatory field in config.yaml file")

	assert.NotNil(t, gen.Tags.PostgresExporter, "Tags.PostgresExporter is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.Tags.PostgresExporter, "Tags.PostgresExporter is a mandatory field in config.yaml file")

	assert.NotNil(t, gen.Tags.Postgresql, "Tags.Postgresql is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.Tags.Postgresql, "Tags.Postgresql is a mandatory field in config.yaml file")

	assert.NotNil(t, gen.Tags.Prometheus, "Tags.Prometheus is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.Tags.Prometheus, "Tags.Prometheus is a mandatory field in config.yaml file")

	assert.NotNil(t, gen.TagMajor, "TagMajor is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.TagMajor, "TagMajor is a mandatory field in config.yaml file")

	assert.NotNil(t, gen.TagMinor, "TagMinor is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.TagMinor, "TagMinor is a mandatory field in config.yaml file")
}
