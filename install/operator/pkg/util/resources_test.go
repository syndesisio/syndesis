package util

import (
	appsv1 "github.com/openshift/api/apps/v1"
	"github.com/operator-framework/operator-sdk/pkg/util/k8sutil"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestLoadResources(t *testing.T) {
	k8sutil.AddToSDKScheme(appsv1.AddToScheme)

	object, err := LoadKubernetesResourceFromFile("../../test/resources/dep.json")
	assert.Nil(t, err)

	assert.NotNil(t, object)

	_, ok := object.(*appsv1.DeploymentConfig)
	assert.True(t, ok)
}

func TestLoadYamlResources(t *testing.T) {
	k8sutil.AddToSDKScheme(appsv1.AddToScheme)

	object, err := LoadKubernetesResourceFromFile("../../test/resources/dep.yml")
	assert.Nil(t, err)

	assert.NotNil(t, object)

	_, ok := object.(*appsv1.DeploymentConfig)
	assert.True(t, ok)
}
