package generator_test

import (
	"context"
	"path/filepath"
	"strings"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"

	syntesting "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/testing"
)

func TestGenerator(t *testing.T) {
	syndesis := &v1beta1.Syndesis{
		Spec: v1beta1.SyndesisSpec{
			Addons: v1beta1.AddonsSpec{
				Jaeger: v1beta1.JaegerConfiguration{
					Enabled:      true,
					SamplerType:  "const",
					SamplerParam: "0",
				},
				Ops:  v1beta1.AddonSpec{Enabled: true},
				Todo: v1beta1.AddonSpec{Enabled: true},
				PublicApi: v1beta1.PublicApiConfiguration{
					Enabled:       true,
					RouteHostname: "mypublichost.com",
				},
			},
			Components: v1beta1.ComponentsSpec{
				Oauth: v1beta1.OauthConfiguration{},
				Server: v1beta1.ServerConfiguration{
					Resources: v1beta1.Resources{Memory: "800Mi"},
					Features: v1beta1.ServerFeatures{
						Maven: v1beta1.MavenConfiguration{
							Append: false,
							Repositories: map[string]string{
								"central":           "https://repo.maven.apache.org/maven2/",
								"repo-02-redhat-ga": "https://maven.repository.redhat.com/ga/",
								"repo-03-jboss-ea":  "https://repository.jboss.org/nexus/content/groups/ea/",
							},
						},
					},
				},
				Meta: v1beta1.MetaConfiguration{
					Resources: v1beta1.ResourcesWithPersistentVolume{
						Memory:         "512Mi",
						VolumeCapacity: "1Gi",
					},
				},
				Database: v1beta1.DatabaseConfiguration{
					User: "syndesis",
					Name: "syndesis",
					URL:  "postgresql://syndesis-db:5432/syndesis?sslmode=disable",
					Resources: v1beta1.ResourcesWithPersistentVolume{
						Memory:         "255Mi",
						VolumeCapacity: "1Gi",
					},
				},
				Prometheus: v1beta1.PrometheusConfiguration{
					Resources: v1beta1.ResourcesWithPersistentVolume{
						Memory:         "512Mi",
						VolumeCapacity: "1Gi",
					},
				},
				Upgrade: v1beta1.UpgradeConfiguration{
					Resources: v1beta1.VolumeOnlyResources{VolumeCapacity: "1Gi"},
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
	syndesis := &v1beta1.Syndesis{}
	baseDir := "./addons/ops/"

	clientTools := syntesting.FakeClientTools()
	conf, err := configuration.GetProperties(context.TODO(), "../../build/conf/config-test.yaml", clientTools, syndesis)
	if err != nil {

	}
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
func checkSynMeta(t *testing.T, resource unstructured.Unstructured, syndesis *v1beta1.Syndesis) int {
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
func checkSynServer(t *testing.T, resource unstructured.Unstructured, syndesis *v1beta1.Syndesis) int {
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

	return 1
}

func checkSynGlobalConfig(t *testing.T, resource unstructured.Unstructured, syndesis *v1beta1.Syndesis) int {
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

func checkSynUIConfig(t *testing.T, resource unstructured.Unstructured, syndesis *v1beta1.Syndesis) int {
	if resource.GetName() != "syndesis-ui-config" {
		return 0
	}

	return 1
}

func checkSynOAuthProxy(t *testing.T, resource unstructured.Unstructured, syndesis *v1beta1.Syndesis) int {
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

func renderResource(t *testing.T, syndesis *v1beta1.Syndesis, resourcePath string) []unstructured.Unstructured {
	clientTools := syntesting.FakeClientTools()
	configuration, err := configuration.GetProperties(context.TODO(), "../../build/conf/config-test.yaml", clientTools, syndesis)
	require.NoError(t, err)

	resources, err := generator.Render(resourcePath, configuration)
	require.NoError(t, err)
	assert.True(t, len(resources) > 0)
	return resources
}

func checkPesistentVolumeProps(t *testing.T, syndesis *v1beta1.Syndesis, infResource string, pvTest func(t *testing.T, resource unstructured.Unstructured)) {
	resources := renderResource(t, syndesis, infResource)

	for _, resource := range resources {
		if resource.GetKind() != "PersistentVolumeClaim" {
			continue
		}

		pvTest(t, resource)
	}
}

func TestGeneratorComponentPVAccessMode(t *testing.T) {

	resources := v1beta1.ResourcesWithPersistentVolume{
		Memory:           "255Mi",
		VolumeCapacity:   "1Gi",
		VolumeAccessMode: v1beta1.ReadOnlyMany,
	}

	testData := []struct {
		name               string
		path               string
		expectedAccessMode string
		syndesis           *v1beta1.Syndesis
	}{
		{
			name:               "syndesis-db-default",
			path:               "./database/",
			expectedAccessMode: string(v1beta1.ReadWriteOnce),
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Database: v1beta1.DatabaseConfiguration{
							Resources: v1beta1.ResourcesWithPersistentVolume{
								Memory:         "255Mi",
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
			expectedAccessMode: string(v1beta1.ReadOnlyMany),
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Database: v1beta1.DatabaseConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name:               "syndesis-meta",
			path:               "./infrastructure/04-syndesis-meta.yml.tmpl",
			expectedAccessMode: string(v1beta1.ReadOnlyMany),
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Meta: v1beta1.MetaConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name:               "syndesis-prometheus",
			path:               "./infrastructure/06-syndesis-prometheus.yml.tmpl",
			expectedAccessMode: string(v1beta1.ReadOnlyMany),
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Prometheus: v1beta1.PrometheusConfiguration{
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
	resources := v1beta1.ResourcesWithPersistentVolume{
		Memory:         "255Mi",
		VolumeCapacity: "1Gi",
	}

	testData := []struct {
		name     string
		path     string
		syndesis *v1beta1.Syndesis
	}{
		{
			name: "syndesis-db",
			path: "./database/",
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Database: v1beta1.DatabaseConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-meta",
			path: "./infrastructure/04-syndesis-meta.yml.tmpl",
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Meta: v1beta1.MetaConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-prometheus",
			path: "./infrastructure/06-syndesis-prometheus.yml.tmpl",
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Prometheus: v1beta1.PrometheusConfiguration{
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

	resources := v1beta1.ResourcesWithPersistentVolume{
		Memory:         "255Mi",
		VolumeCapacity: "1Gi",
		VolumeName:     volumeName,
	}

	testData := []struct {
		name     string
		path     string
		syndesis *v1beta1.Syndesis
	}{
		{
			name: "syndesis-db",
			path: "./database/",
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Database: v1beta1.DatabaseConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-meta",
			path: "./infrastructure/04-syndesis-meta.yml.tmpl",
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Meta: v1beta1.MetaConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-prometheus",
			path: "./infrastructure/06-syndesis-prometheus.yml.tmpl",
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Prometheus: v1beta1.PrometheusConfiguration{
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

	resources := v1beta1.ResourcesWithPersistentVolume{
		Memory:             "255Mi",
		VolumeCapacity:     "1Gi",
		VolumeStorageClass: volumeStorageClass,
	}

	testData := []struct {
		name     string
		path     string
		syndesis *v1beta1.Syndesis
	}{
		{
			name: "syndesis-db",
			path: "./database/",
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Database: v1beta1.DatabaseConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-meta",
			path: "./infrastructure/04-syndesis-meta.yml.tmpl",
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Meta: v1beta1.MetaConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-prometheus",
			path: "./infrastructure/06-syndesis-prometheus.yml.tmpl",
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Prometheus: v1beta1.PrometheusConfiguration{
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

	resources := v1beta1.ResourcesWithPersistentVolume{
		Memory:         "255Mi",
		VolumeCapacity: "1Gi",
		VolumeLabels:   volumeLabels,
	}

	testData := []struct {
		name     string
		path     string
		syndesis *v1beta1.Syndesis
	}{
		{
			name: "syndesis-db",
			path: "./database/",
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Database: v1beta1.DatabaseConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-meta",
			path: "./infrastructure/04-syndesis-meta.yml.tmpl",
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Meta: v1beta1.MetaConfiguration{
							Resources: resources,
						},
					},
				},
			},
		},
		{
			name: "syndesis-prometheus",
			path: "./infrastructure/06-syndesis-prometheus.yml.tmpl",
			syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Components: v1beta1.ComponentsSpec{
						Prometheus: v1beta1.PrometheusConfiguration{
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
