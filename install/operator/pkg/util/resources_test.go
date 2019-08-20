package util

import (
	appsv1 "github.com/openshift/api/apps/v1"
	"github.com/stretchr/testify/assert"
	"k8s.io/apimachinery/pkg/runtime"
	"testing"
)

func init() {
	scheme = runtime.NewScheme()
	if err := appsv1.AddToScheme(scheme); err != nil {
		panic("Can't register appsv1 schemes")
	}
}

var scheme *runtime.Scheme

func TestLoadResources(t *testing.T) {
	object, err := LoadResourceFromFile(scheme, "../../tests/resources/dep.json")
	assert.Nil(t, err)

	assert.NotNil(t, object)

	_, ok := object.(*appsv1.DeploymentConfig)
	assert.True(t, ok)
}

func TestLoadYamlResources(t *testing.T) {
	object, err := LoadResourceFromFile(scheme, "../../tests/resources/dep.yml")
	assert.Nil(t, err)

	assert.NotNil(t, object)

	_, ok := object.(*appsv1.DeploymentConfig)
	assert.True(t, ok)
}
