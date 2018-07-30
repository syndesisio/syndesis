package util

import (
	"testing"
	"github.com/stretchr/testify/assert"
	appsv1 "github.com/openshift/api/apps/v1"
)


func TestLoadResources(t *testing.T) {

	object, err := LoadKubernetesResourceFromFile("../../test/resources/dep.json")
	assert.Nil(t, err)

	assert.NotNil(t, object)

	_, ok := object.(*appsv1.DeploymentConfig)
	assert.True(t, ok)
}

func TestLoadYamlResources(t *testing.T) {

	object, err := LoadKubernetesResourceFromFile("../../test/resources/dep.yml")
	assert.Nil(t, err)

	assert.NotNil(t, object)

	_, ok := object.(*appsv1.DeploymentConfig)
	assert.True(t, ok)
}
