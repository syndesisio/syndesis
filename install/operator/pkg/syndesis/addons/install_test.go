package addons

import (
	"github.com/stretchr/testify/assert"
	"testing"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
)

func TestGetAddonResources(t *testing.T) {
	syndesis := v1alpha1.Syndesis{
		ObjectMeta: v1.ObjectMeta{
			Namespace: "ns",
		},
		Spec: v1alpha1.SyndesisSpec{
			ImageStreamNamespace: "is",
			RouteHostName:        "myhost",
			Components: v1alpha1.ComponentsSpec{
				Db: v1alpha1.DbConfiguration{
					ImageStreamNamespace: "dbis",
					Database:             "db",
					User:                 "user",
					SampleDbPassword:     "password",
				},
			},
		},
	}

	addons, err := GetAddonsResources("../../../../addons", &syndesis)
	assert.NoError(t, err)

	foundHost := false
	for _, addon := range addons {
		host, f, _ := unstructured.NestedString(addon.UnstructuredContent(), "spec", "host")
		if f {
			foundHost = true
			assert.Equal(t, "todo-myhost", host)
		}
	}

	assert.True(t, foundHost)
}
