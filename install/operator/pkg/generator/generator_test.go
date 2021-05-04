package generator_test

import (
	"context"
	"path/filepath"
	"reflect"
	"strings"
	"testing"

	"github.com/leanovate/gopter"
	"github.com/leanovate/gopter/arbitrary"
	"github.com/leanovate/gopter/gen"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta2"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/util/yaml"

	syntesting "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/testing"
)

func TestGenerator(t *testing.T) {
	syndesis := &v1beta2.Syndesis{
		Spec: v1beta2.SyndesisSpec{
			Addons: v1beta2.AddonsSpec{
				Jaeger: v1beta2.JaegerConfiguration{
					Enabled:      true,
					SamplerType:  "const",
					SamplerParam: "0",
				},
				Ops:  v1beta2.AddonSpec{Enabled: true},
				Todo: v1beta2.AddonSpec{Enabled: true},
				PublicApi: v1beta2.PublicApiConfiguration{
					Enabled:       true,
					RouteHostname: "mypublichost.com",
				},
			},
			Components: v1beta2.ComponentsSpec{
				Oauth: v1beta2.OauthConfiguration{},
				Server: v1beta2.ServerConfiguration{
					Resources: v1beta2.Resources{
						Limit: v1beta2.ResourceParams{
							Memory: "800Mi",
							CPU:    "750m",
						},
						Request: v1beta2.ResourceParams{
							Memory: "256Mi",
							CPU:    "450m",
						},
					},
					Features: v1beta2.ServerFeatures{
						Maven: v1beta2.MavenConfiguration{
							Append: false,
							Repositories: map[string]string{
								"central":           "https://repo.maven.apache.org/maven2/",
								"repo-02-redhat-ga": "https://maven.repository.redhat.com/ga/",
								"repo-03-jboss-ea":  "https://repository.jboss.org/nexus/content/groups/ea/",
							},
						},
					},
				},
				Meta: v1beta2.MetaConfiguration{
					Resources: v1beta2.ResourcesWithPersistentVolume{
						Limit: v1beta2.ResourceParams{
							Memory: "512Mi",
						},
						Request: v1beta2.ResourceParams{
							Memory: "280Mi",
						},
						VolumeCapacity: "1Gi",
					},
				},
				Database: v1beta2.DatabaseConfiguration{
					User: "syndesis",
					Name: "syndesis",
					URL:  "postgresql://syndesis-db:5432/syndesis?sslmode=disable",
					Resources: v1beta2.ResourcesWithPersistentVolume{
						Limit: v1beta2.ResourceParams{
							Memory: "255Mi",
						},
						Request: v1beta2.ResourceParams{
							Memory: "255Mi",
						},
						VolumeCapacity: "1Gi",
					},
				},
				Prometheus: v1beta2.PrometheusConfiguration{
					Resources: v1beta2.ResourcesWithPersistentVolume{
						Limit: v1beta2.ResourceParams{
							Memory: "512Mi",
						},
						Request: v1beta2.ResourceParams{
							Memory: "512Mi",
						},
						VolumeCapacity: "1Gi",
					},
				},
				Upgrade: v1beta2.UpgradeConfiguration{
					Resources: v1beta2.VolumeOnlyResources{VolumeCapacity: "1Gi"},
				},
			},
		},
	}

	clientTools := syntesting.FakeClientTools()
	configuration, err := configuration.GetProperties(context.TODO(), "../../build/conf/config.yaml", clientTools, syndesis)
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

	for _, addon := range []string{"todo", "jaeger", "ops", "publicApi"} {
		resources, err = generator.RenderFSDir(generator.GetAssetsFS(), "./addons/"+addon+"/", configuration)
		require.NoError(t, err)
		assert.True(t, len(resources) > 0)
	}

}

// Run test related with Ops addon
func TestOpsAddon(t *testing.T) {
	syndesis := &v1beta2.Syndesis{}
	baseDir := "./addons/ops/"

	clientTools := syntesting.FakeClientTools()
	conf, err := configuration.GetProperties(context.TODO(), "../../build/conf/config-test.yaml", clientTools, syndesis)
	require.NoError(t, err)

	for _, file := range []string{"addon-ops-db-alerting-rules.yml", "addon-ops-db-dashboard.yml"} {
		resources, err := generator.Render(filepath.Join(baseDir, file), conf)
		require.NoError(t, err)
		assert.True(t, len(resources) != 0, "Monitoring resources for database should be created when no external db url is defined")
	}

	syndesis.Spec.Components.Database.ExternalDbURL = "1234"
	conf, err = configuration.GetProperties(context.TODO(), "../../build/conf/config-test.yaml", clientTools, syndesis)
	if err != nil {

	}
	for _, file := range []string{"addon-ops-db-alerting-rules.yml", "addon-ops-db-dashboard.yml"} {
		resources, err := generator.Render(filepath.Join(baseDir, file), conf)
		require.NoError(t, err)
		assert.True(t, len(resources) == 0, "Monitoring resources for database should not be created when there is a external db url defined")
	}
}

//
// Checks syndesis-meta resources have had syndesis
// object values correctly applied
//
func checkSynMeta(t *testing.T, resource unstructured.Unstructured, syndesis *v1beta2.Syndesis) int {
	if resource.GetName() != "syndesis-meta" {
		return 0
	}

	assertResourcePropertyStr(t, resource, syndesis.Spec.Components.Meta.Resources.VolumeCapacity, "spec", "resources", "requests", "storage")

	return 1
}

//
// Checks syndesis-server resources have had syndesis
// object values correctly applied
//
func checkSynServer(t *testing.T, resource unstructured.Unstructured, syndesis *v1beta2.Syndesis) int {
	if resource.GetName() != "syndesis-server" {
		return 0
	}

	container := sliceProperty(resource, "spec", "template", "spec", "containers")
	if container != nil {
		//
		// Compare the server memory and cpu values
		//
		limits, lexists, _ := unstructured.NestedFieldNoCopy(container, "resources", "limits")
		if lexists {
			limitMap, ok := limits.(map[string]interface{})
			assert.True(t, ok)
			assert.Equal(t, syndesis.Spec.Components.Server.Resources.Limit.Memory, limitMap["memory"])
			assert.Equal(t, syndesis.Spec.Components.Server.Resources.Limit.CPU, limitMap["cpu"])
		}
	}

	return 1
}

func checkSynGlobalConfig(t *testing.T, resource unstructured.Unstructured, syndesis *v1beta2.Syndesis) int {
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

func checkSynUIConfig(t *testing.T, resource unstructured.Unstructured, syndesis *v1beta2.Syndesis) int {
	if resource.GetName() != "syndesis-ui-config" {
		return 0
	}

	return 1
}

func checkSynOAuthProxy(t *testing.T, resource unstructured.Unstructured, syndesis *v1beta2.Syndesis) int {
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

func assertPropStr(t *testing.T, resource map[string]interface{}, expected string, resourcePath ...string) {
	value, exists, _ := unstructured.NestedString(resource, resourcePath...)
	if exists {
		assert.Equal(t, expected, value, "rendering should be applied correctly")
	}
}

func renderResource(t *testing.T, syndesis *v1beta2.Syndesis, resourcePath string) []unstructured.Unstructured {
	clientTools := syntesting.FakeClientTools()
	configuration, err := configuration.GetProperties(context.TODO(), "../../build/conf/config-test.yaml", clientTools, syndesis)
	require.NoError(t, err)

	resources, err := generator.Render(resourcePath, configuration)
	require.NoError(t, err)
	assert.True(t, len(resources) > 0)
	return resources
}

func checkPesistentVolumeProps(t *testing.T, syndesis *v1beta2.Syndesis, infResource string, pvTest func(t *testing.T, resource unstructured.Unstructured)) {
	resources := renderResource(t, syndesis, infResource)

	for _, resource := range resources {
		if resource.GetKind() != "PersistentVolumeClaim" {
			continue
		}

		pvTest(t, resource)
	}
}

func TestGeneratorComponentPVAccessMode(t *testing.T) {

	resources := v1beta2.ResourcesWithPersistentVolume{
		Limit:            v1beta2.ResourceParams{Memory: "255Mi"},
		VolumeCapacity:   "1Gi",
		VolumeAccessMode: v1beta2.ReadOnlyMany,
	}

	testData := []struct {
		name               string
		path               string
		expectedAccessMode string
		syndesis           *v1beta2.Syndesis
	}{
		{
			name:               "syndesis-db-default",
			path:               "./database/",
			expectedAccessMode: string(v1beta2.ReadWriteOnce),
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Database: v1beta2.DatabaseConfiguration{
							Resources: v1beta2.ResourcesWithPersistentVolume{
								Limit: v1beta2.ResourceParams{
									Memory: "255Mi",
								},
								VolumeCapacity: "1Gi",
							},
						},
					},
				},
			},
		},
		{
			name:               "syndesis-db",
			path:               "./database/",
			expectedAccessMode: string(v1beta2.ReadOnlyMany),
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Database: v1beta2.DatabaseConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name:               "syndesis-meta",
			path:               "./infrastructure/04-syndesis-meta.yml.tmpl",
			expectedAccessMode: string(v1beta2.ReadOnlyMany),
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Meta: v1beta2.MetaConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name:               "syndesis-prometheus",
			path:               "./infrastructure/06-syndesis-prometheus.yml.tmpl",
			expectedAccessMode: string(v1beta2.ReadOnlyMany),
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Prometheus: v1beta2.PrometheusConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
	}

	for _, tt := range testData {
		t.Run(tt.name, func(t *testing.T) {
			checkPesistentVolumeProps(t, tt.syndesis, tt.path, func(t *testing.T, resource unstructured.Unstructured) {
				aModes, exists, _ := unstructured.NestedStringSlice(resource.UnstructuredContent(), "spec", "accessModes")
				assert.True(t, exists)
				assert.True(t, len(aModes) == 1)
				assert.Equal(t, aModes[0], tt.expectedAccessMode)
			})
		})
	}
}

func TestGeneratorComponentPVNoExtraProps(t *testing.T) {
	resources := v1beta2.ResourcesWithPersistentVolume{
		Limit: v1beta2.ResourceParams{
			Memory: "255Mi",
		},
		VolumeCapacity: "1Gi",
	}

	testData := []struct {
		name     string
		path     string
		syndesis *v1beta2.Syndesis
	}{
		{
			name: "syndesis-db",
			path: "./database/",
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Database: v1beta2.DatabaseConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-meta",
			path: "./infrastructure/04-syndesis-meta.yml.tmpl",
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Meta: v1beta2.MetaConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-prometheus",
			path: "./infrastructure/06-syndesis-prometheus.yml.tmpl",
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Prometheus: v1beta2.PrometheusConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
	}

	for _, tt := range testData {
		t.Run(tt.name, func(t *testing.T) {
			checkPesistentVolumeProps(t, tt.syndesis, tt.path, func(t *testing.T, resource unstructured.Unstructured) {
				_, exists, _ := unstructured.NestedString(resource.UnstructuredContent(), "spec", "volumeName")
				assert.False(t, exists)

				_, exists, _ = unstructured.NestedString(resource.UnstructuredContent(), "spec", "storageClassName")
				assert.False(t, exists)

				_, exists, _ = unstructured.NestedMap(resource.UnstructuredContent(), "spec", "matchLabels")
				assert.False(t, exists)
			})
		})
	}
}

func TestGeneratorComponentPVVolumeName(t *testing.T) {
	volumeName := "pv0001"

	resources := v1beta2.ResourcesWithPersistentVolume{
		Limit: v1beta2.ResourceParams{
			Memory: "255Mi",
		},
		VolumeCapacity: "1Gi",
		VolumeName:     volumeName,
	}

	testData := []struct {
		name     string
		path     string
		syndesis *v1beta2.Syndesis
	}{
		{
			name: "syndesis-db",
			path: "./database/",
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Database: v1beta2.DatabaseConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-meta",
			path: "./infrastructure/04-syndesis-meta.yml.tmpl",
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Meta: v1beta2.MetaConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-prometheus",
			path: "./infrastructure/06-syndesis-prometheus.yml.tmpl",
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Prometheus: v1beta2.PrometheusConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
	}

	for _, tt := range testData {
		t.Run(tt.name, func(t *testing.T) {
			checkPesistentVolumeProps(t, tt.syndesis, tt.path, func(t *testing.T, resource unstructured.Unstructured) {
				assertResourcePropertyStr(t, resource, volumeName, "spec", "volumeName")
			})
		})
	}
}

func TestGeneratorDBVolumeStorageClass(t *testing.T) {
	volumeStorageClass := "gluster-fs"

	resources := v1beta2.ResourcesWithPersistentVolume{
		Limit: v1beta2.ResourceParams{
			Memory: "255Mi",
		},
		VolumeCapacity:     "1Gi",
		VolumeStorageClass: volumeStorageClass,
	}

	testData := []struct {
		name     string
		path     string
		syndesis *v1beta2.Syndesis
	}{
		{
			name: "syndesis-db",
			path: "./database/",
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Database: v1beta2.DatabaseConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-meta",
			path: "./infrastructure/04-syndesis-meta.yml.tmpl",
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Meta: v1beta2.MetaConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-prometheus",
			path: "./infrastructure/06-syndesis-prometheus.yml.tmpl",
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Prometheus: v1beta2.PrometheusConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
	}

	for _, tt := range testData {
		t.Run(tt.name, func(t *testing.T) {
			checkPesistentVolumeProps(t, tt.syndesis, tt.path, func(t *testing.T, resource unstructured.Unstructured) {
				assertResourcePropertyStr(t, resource, volumeStorageClass, "spec", "storageClassName")
			})
		})
	}
}

func TestGeneratorDBVolumeLabels(t *testing.T) {
	volumeLabels := map[string]string{
		"storage-tier":          "gold",
		"aws-availability-zone": "us-east-1",
	}

	resources := v1beta2.ResourcesWithPersistentVolume{
		Limit: v1beta2.ResourceParams{
			Memory: "255Mi",
		},
		VolumeCapacity: "1Gi",
		VolumeLabels:   volumeLabels,
	}

	testData := []struct {
		name     string
		path     string
		syndesis *v1beta2.Syndesis
	}{
		{
			name: "syndesis-db",
			path: "./database/",
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Database: v1beta2.DatabaseConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-meta",
			path: "./infrastructure/04-syndesis-meta.yml.tmpl",
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Meta: v1beta2.MetaConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-prometheus",
			path: "./infrastructure/06-syndesis-prometheus.yml.tmpl",
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Prometheus: v1beta2.PrometheusConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
	}

	for _, tt := range testData {
		t.Run(tt.name, func(t *testing.T) {
			checkPesistentVolumeProps(t, tt.syndesis, tt.path, func(t *testing.T, resource unstructured.Unstructured) {
				labelsMap, exists, _ := unstructured.NestedMap(resource.UnstructuredContent(), "spec", "selector", "matchLabels")
				assert.True(t, exists)
				assert.Equal(t, len(labelsMap), 2)

				for key, val := range labelsMap {
					assert.Equal(t, val, volumeLabels[key])
				}
			})
		})
	}
}

func TestAuditingFeatureToggle(t *testing.T) {
	for _, givenEnabled := range []bool{true, false} {
		syndesis := v1beta2.Syndesis{
			Spec: v1beta2.SyndesisSpec{
				Components: v1beta2.ComponentsSpec{
					Server: v1beta2.ServerConfiguration{
						Features: v1beta2.ServerFeatures{
							Auditing: givenEnabled,
						},
					},
				},
			},
		}

		resource := renderResource(t, &syndesis, "./infrastructure/03-syndesis-server-config.yml.tmpl")

		assert.True(t, len(resource) == 1, "Expected server config map to be rendered")

		data := resource[0].Object["data"].(map[string]interface{})
		assert.NotNil(t, data, "Expected server config map to contain data")

		applicationYAML := data["application.yml"]
		assert.NotNil(t, applicationYAML, "Expected server config map to contain application.yaml in data")

		appConfiguration := map[string]interface{}{}
		err := yaml.Unmarshal([]byte(applicationYAML.(string)), &appConfiguration)
		require.NoError(t, err, "Unable to unmarshal application.yml")

		features := appConfiguration["features"]
		assert.NotNil(t, features, "Expected application.yml to contain the key `features`")

		auditing := features.(map[string]interface{})["auditing"].(map[string]interface{})
		assert.NotNil(t, auditing, "Expected application.yml to contain the key `features.auditing`")

		enabled := auditing["enabled"].(bool)
		assert.NotNil(t, enabled)
		assert.Equalf(t, givenEnabled, enabled, "Expected features.auditing.enabled to be %v", givenEnabled)
	}
}

// TestGeneratorProperty makes sure that no matter what configuration
// is provided resulting YAML files can be parsed. The
// generator.Render* methods use Go templates and parse the results
// as YAML.
func TestGeneratorProperty(t *testing.T) {
	// if the test fails, comment above and use this instead
	// pass in the seed to troubleshoot
	//parameters := gopter.DefaultTestParametersWithSeed(...)
	parameters := gopter.DefaultTestParameters()
	parameters.MaxSize = 10

	arbitraries := arbitrary.DefaultArbitraries()
	arbitraries.RegisterGen(gen.AlphaString())
	arbitraries.RegisterGen(gen.Struct(reflect.TypeOf(v1beta2.ResourcesWithPersistentVolume{}), map[string]gopter.Gen{
		"VolumeLabels": gen.MapOf(gen.Identifier(), gen.Identifier()), // we can't have volume labels with keys that are empty strings
	}))
	arbitraries.RegisterGen(gen.Struct(reflect.TypeOf(v1beta2.DatabaseConfiguration{}), map[string]gopter.Gen{
		"ExternalDbURL": gen.Identifier(),
	}))
	arbitraries.RegisterGen(gen.Struct(reflect.TypeOf(v1beta2.MavenConfiguration{}), map[string]gopter.Gen{
		"Repositories": gen.MapOf(gen.Identifier(), gen.Identifier()), // we can't have Maven repositories with illegal characters
	}))

	properties := gopter.NewProperties(parameters)

	clientTools := syntesting.FakeClientTools()

	properties.Property("all combinations render correctly", arbitraries.ForAll(
		func(spec *v1beta2.SyndesisSpec) bool {
			configuration, err := configuration.GetProperties(context.TODO(), "../../build/conf/config.yaml", clientTools, &v1beta2.Syndesis{Spec: *spec})
			if err != nil {
				// it's best to panic that gives gopter a chance to
				// handle the error and fill in the details, we need
				// the seed to be able to reproduce the issue
				panic(err)
			}

			rendered, err := generator.RenderFSDir(generator.GetAssetsFS(), "./infrastructure/", configuration)

			if err != nil {
				// see above
				panic(err)
			}

			return len(rendered) > 0
		},
	))

	properties.TestingRun(t)
}
