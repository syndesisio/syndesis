package generator_test

import (
	"context"
	"strings"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
)

func TestGenerator(t *testing.T) {
	syndesis := &v1alpha1.Syndesis{
		Spec: v1alpha1.SyndesisSpec{
			Addons: v1alpha1.AddonsSpec{
				Jaeger: v1alpha1.JaegerConfiguration{
					Enabled:      true,
					SamplerType:  "const",
					SamplerParam: "0",
				},
				Ops:  v1alpha1.AddonSpec{Enabled: true},
				Todo: v1alpha1.AddonSpec{Enabled: true},
				DV: v1alpha1.DvConfiguration{
					Enabled:   false,
					Resources: v1alpha1.Resources{Memory: "1024Mi"},
				},
				CamelK: v1alpha1.CamelKConfiguration{
					Enabled:       true,
					CamelVersion:  "2.21.0.fuse-760006",
					CamelKRuntime: "0.3.4.fuse-740008",
					Image:         "fabric8/s2i-java:3.0-java8",
				},
			},
			Components: v1alpha1.ComponentsSpec{
				Oauth: v1alpha1.OauthConfiguration{},
				Server: v1alpha1.ServerConfiguration{
					Resources: v1alpha1.Resources{Memory: "800Mi"},
					Features: v1alpha1.ServerFeatures{
						MavenRepositories: map[string]string{
							"central":           "https://repo.maven.apache.org/maven2/",
							"repo-02-redhat-ga": "https://maven.repository.redhat.com/ga/",
							"repo-03-jboss-ea":  "https://repository.jboss.org/nexus/content/groups/ea/",
						},
					},
				},
				Meta: v1alpha1.MetaConfiguration{
					Resources: v1alpha1.ResourcesWithVolume{
						Memory:         "512Mi",
						VolumeCapacity: "1Gi",
					},
				},
				Database: v1alpha1.DatabaseConfiguration{
					User:     "syndesis",
					Database: "syndesis",
					URL:      "postgresql://syndesis-db:5432/syndesis?sslmode=disable",
					Resources: v1alpha1.ResourcesWithVolume{
						Memory:         "255Mi",
						VolumeCapacity: "1Gi",
					},
				},
				Prometheus: v1alpha1.PrometheusConfiguration{
					Resources: v1alpha1.ResourcesWithVolume{
						Memory:         "512Mi",
						VolumeCapacity: "1Gi",
					},
				},
				Upgrade: v1alpha1.UpgradeConfiguration{
					Resources: v1alpha1.VolumeOnlyResources{VolumeCapacity: "1Gi"},
				},
			},
		},
	}

	configuration, err := configuration.GetProperties("../../build/conf/config.yaml", context.TODO(), nil, syndesis)
	require.NoError(t, err)

	resources, err := generator.RenderFSDir(generator.GetAssetsFS(), "./infrastructure/", configuration)
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
		checks += checkSynMeta(t, resource, syndesis)
		checks += checkSynServer(t, resource, syndesis)
		checks += checkSynGlobalConfig(t, resource, syndesis)
		checks += checkSynUIConfig(t, resource, syndesis)
		checks += checkSynOAuthProxy(t, resource, syndesis)
	}
	assert.True(t, checks >= 6)

	for _, addon := range []string{"todo", "camelk", "jaeger", "dv", "ops"} {
		resources, err = generator.RenderFSDir(generator.GetAssetsFS(), "./addons/"+addon+"/", configuration)
		require.NoError(t, err)
		assert.True(t, len(resources) > 0)
	}

	resources, err = generator.RenderFSDir(generator.GetAssetsFS(), "./addons/dv/", configuration)
	checks = 0
	for _, resource := range resources {
		checks += checkSynAddonDv(t, resource, syndesis)
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
		// Compare the server memory limit which is set via the template function 'memoryLimit'
		//
		limits, lexists, _ := unstructured.NestedFieldNoCopy(container, "resources", "limits")
		if lexists {
			limitMap, ok := limits.(map[string]interface{})
			assert.True(t, ok)
			assert.Equal(t, syndesis.Spec.Components.Server.Resources.Memory, limitMap["memory"])
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
		var expected string
		if syndesis.Spec.Addons.DV.Enabled {
			expected = "1"
		} else {
			expected = "0"
		}
		assert.True(t, strings.Contains(config, "\"enabled\": "+expected))
	}

	return 1
}

func checkSynAddonDv(t *testing.T, resource unstructured.Unstructured, syndesis *v1alpha1.Syndesis) int {
	if resource.GetName() != "syndesis-dv" {
		return 0
	}

	container := sliceProperty(resource, "spec", "template", "spec", "containers")
	if container != nil {
		//
		// Compare the dv memory limit which is set via the template function 'memoryLimit'
		//
		limits, lexists, _ := unstructured.NestedFieldNoCopy(container, "resources", "limits")
		assert.True(t, lexists)
		limitMap, ok := limits.(map[string]interface{})
		assert.True(t, ok)
		assert.Equal(t, syndesis.Spec.Addons.DV.Resources.Memory, limitMap["memory"])
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
		assertPropStr(t, tags, "quay.io/openshift/origin-oauth-proxy:v4.0.0", "name")
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
