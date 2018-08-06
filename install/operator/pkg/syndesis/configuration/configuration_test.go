package configuration

import (
	"github.com/stretchr/testify/assert"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	v12 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/resource"
	"k8s.io/apimachinery/pkg/apis/meta/v1"
	"testing"
)

func TestStandardConfig(t *testing.T) {
	syndesis := v1alpha1.Syndesis{
		ObjectMeta: v1.ObjectMeta{
			Namespace: "ns",
		},
	}

	config := GetEnvVars(&syndesis)
	assert.Contains(t, config, string(EnvOpenshiftProject))
	assert.Len(t, config, 1)
}


func TestSpecificConfig(t *testing.T) {
	demodata := true
	deploy := false
	limit := 2
	stateCheckInterval := 120
	syndesis := v1alpha1.Syndesis{
		ObjectMeta: v1.ObjectMeta{
			Namespace: "ns",
		},
		Spec: v1alpha1.SyndesisSpec{
			ImageStreamNamespace: "is",
			DemoData: &demodata,
			Integration: v1alpha1.IntegrationSpec{
				Limit: &limit,
				StateCheckInterval: &stateCheckInterval,
			},
			RouteHostName: "myhost",
			Registry: "registry",
			DeployIntegrations: &deploy,
			Components: v1alpha1.ComponentsSpec{
				Db: v1alpha1.DbConfiguration{
					ImageStreamNamespace: "dbis",
					Database: "db",
					User: "user",
					Resources: v1alpha1.ResourcesWithVolume{
						ResourceRequirements: v12.ResourceRequirements{
							Limits: v12.ResourceList{
								"memory": resource.MustParse("1Gi"),
							},
						},
						VolumeCapacity: "2Gi",
					},
				},
				Server: v1alpha1.ServerConfiguration{
					Resources: v1alpha1.Resources{
						ResourceRequirements: v12.ResourceRequirements{
							Limits: v12.ResourceList{
								"memory": resource.MustParse("3Gi"),
							},
						},
					},
				},
				Meta: v1alpha1.MetaConfiguration{
					Resources: v1alpha1.ResourcesWithVolume{
						ResourceRequirements: v12.ResourceRequirements{
							Limits: v12.ResourceList{
								"memory": resource.MustParse("4Gi"),
							},
						},
						VolumeCapacity: "5Gi",
					},
				},
				Prometheus: v1alpha1.PrometheusConfiguration{
					Resources: v1alpha1.ResourcesWithVolume{
						ResourceRequirements: v12.ResourceRequirements{
							Limits: v12.ResourceList{
								"memory": resource.MustParse("6Gi"),
							},
						},
						VolumeCapacity: "7Gi",
					},
				},
			},
		},
	}

	config := GetEnvVars(&syndesis)
	assert.Equal(t, "ns", config[string(EnvOpenshiftProject)])
	assert.Equal(t, "is", config[string(EnvImageStreamNamespace)])
	assert.Equal(t, "true", config[string(EnvDemoDataEnabled)])
	assert.Equal(t, "2", config[string(EnvMaxIntegrationsPerUser)])
	assert.Equal(t, "120", config[string(EnvIntegrationStateCheckInterval)])
	assert.Equal(t, "myhost", config[string(EnvRouteHostname)])
	assert.Equal(t, "registry", config[string(EnvSyndesisRegistry)])
	assert.Equal(t, "false", config[string(EnvControllersIntegrationEnabled)])

	assert.Equal(t, "dbis", config[string(EnvPostgresqlImageStreamNamespace)])
	assert.Equal(t, "db", config[string(EnvPostgresqlDatabase)])
	assert.Equal(t, "user", config[string(EnvPostgresqlUser)])
	assert.Equal(t, "1Gi", config[string(EnvPostgresqlMemoryLimit)])
	assert.Equal(t, "2Gi", config[string(EnvPostgresqlVolumeCapacity)])

	assert.Equal(t, "3Gi", config[string(EnvServerMemoryLimit)])

	assert.Equal(t, "4Gi", config[string(EnvMetaMemoryLimit)])
	assert.Equal(t, "5Gi", config[string(EnvMetaVolumeCapacity)])

	assert.Equal(t, "6Gi", config[string(EnvPrometheusMemoryLimit)])
	assert.Equal(t, "7Gi", config[string(EnvPrometheusVolumeCapacity)])
}