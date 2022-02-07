package action_test

import (
	"context"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	synapi "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta3"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/action"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	syntesting "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/testing"
	v1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
)

func renderResource(t *testing.T, ctx context.Context, clientTools *clienttools.ClientTools, syndesis *synapi.Syndesis, resourcePath string) []unstructured.Unstructured {
	configuration, err := configuration.GetProperties(ctx, "../../../build/conf/config-test.yaml", clientTools, syndesis)
	require.NoError(t, err)

	resources, err := generator.Render(resourcePath, configuration)
	require.NoError(t, err)
	assert.True(t, len(resources) > 0)
	return resources
}

func TestPreProcessForAffinityTolerationsNotDeploymentConfig(t *testing.T) {
	syndesis := &synapi.Syndesis{
		Spec: synapi.SyndesisSpec{
			InfraScheduling: synapi.SchedulingSpec{
				Affinity:    &v1.Affinity{},
				Tolerations: []v1.Toleration{},
			},
		},
	}

	clientTools := syntesting.FakeClientTools()
	rtClient, err := clientTools.RuntimeClient()
	require.NoError(t, err)

	ctx := context.TODO()
	resources := renderResource(t, ctx, clientTools, syndesis, "assets/infrastructure/02-syndesis-secrets.yml.tmpl")

	copy := (&resources[0]).DeepCopy()
	err = action.PreProcessForAffinityTolerations(ctx, rtClient, syndesis, &resources[0])
	require.NoError(t, err)
	assert.Equal(t, *copy, resources[0])
}

func TestPreProcessForAffinityTolerationsNoInfraScheduling(t *testing.T) {
	syndesis := &synapi.Syndesis{
		Spec: synapi.SyndesisSpec{},
	}

	clientTools := syntesting.FakeClientTools()
	rtClient, err := clientTools.RuntimeClient()
	require.NoError(t, err)

	ctx := context.TODO()
	resources := renderResource(t, ctx, clientTools, syndesis, "assets/infrastructure/04-syndesis-server.yml.tmpl")

	copy := (&resources[1]).DeepCopy()
	err = action.PreProcessForAffinityTolerations(ctx, rtClient, syndesis, &resources[1])
	require.NoError(t, err)
	assert.Equal(t, *copy, resources[1])
}

func TestPreProcessForAffinityTolerations(t *testing.T) {
	syndesis := &synapi.Syndesis{
		Spec: synapi.SyndesisSpec{
			InfraScheduling: synapi.SchedulingSpec{
				Affinity: &v1.Affinity{
					NodeAffinity: &v1.NodeAffinity{
						RequiredDuringSchedulingIgnoredDuringExecution: &v1.NodeSelector{
							NodeSelectorTerms: []v1.NodeSelectorTerm{
								{
									MatchExpressions: []v1.NodeSelectorRequirement{
										{
											Key:      "failure-domain.beta.kubernetes.io/zone",
											Operator: v1.NodeSelectorOpIn,
											Values:   []string{"us-west-1c"},
										},
										{
											Key:      "failure-domain.beta.kubernetes.io/region",
											Operator: v1.NodeSelectorOpIn,
											Values:   []string{"us-west-1"},
										},
									},
								},
							},
						},
					},
				},
				Tolerations: []v1.Toleration{
					{
						Key:      "tol_333",
						Operator: v1.TolerationOpEqual,
						Effect:   v1.TaintEffectNoSchedule,
					},
					{
						Key:      "key3333",
						Operator: v1.TolerationOpEqual,
						Value:    "value2",
						Effect:   v1.TaintEffectNoSchedule,
					},
				},
			},
		},
	}

	clientTools := syntesting.FakeClientTools()
	rtClient, err := clientTools.RuntimeClient()
	require.NoError(t, err)

	ctx := context.TODO()
	resources := renderResource(t, ctx, clientTools, syndesis, "assets/infrastructure/04-syndesis-server.yml.tmpl")
	copy := (&resources[1]).DeepCopy()
	err = action.PreProcessForAffinityTolerations(ctx, rtClient, syndesis, &resources[1])
	require.NoError(t, err)
	assert.NotEqual(t, *copy, resources[1])

	affinity, found, err := unstructured.NestedFieldNoCopy(resources[1].UnstructuredContent(), "spec", "template", "spec", "affinity")
	require.NoError(t, err)
	assert.True(t, found)
	assert.NotNil(t, affinity)

	toleration, found, err := unstructured.NestedFieldNoCopy(resources[1].UnstructuredContent(), "spec", "template", "spec", "tolerations")
	require.NoError(t, err)
	assert.True(t, found)
	assert.NotNil(t, toleration)
}

func TestPreProcessJaegerCR(t *testing.T) {
	syndesis := &synapi.Syndesis{
		Spec: synapi.SyndesisSpec{
			InfraScheduling: synapi.SchedulingSpec{
				Affinity: &v1.Affinity{
					NodeAffinity: &v1.NodeAffinity{
						RequiredDuringSchedulingIgnoredDuringExecution: &v1.NodeSelector{
							NodeSelectorTerms: []v1.NodeSelectorTerm{
								{
									MatchExpressions: []v1.NodeSelectorRequirement{
										{
											Key:      "failure-domain.beta.kubernetes.io/zone",
											Operator: v1.NodeSelectorOpIn,
											Values:   []string{"us-west-1c"},
										},
										{
											Key:      "failure-domain.beta.kubernetes.io/region",
											Operator: v1.NodeSelectorOpIn,
											Values:   []string{"us-west-1"},
										},
									},
								},
							},
						},
					},
				},
				Tolerations: []v1.Toleration{
					{
						Key:      "tol_333",
						Operator: v1.TolerationOpEqual,
						Effect:   v1.TaintEffectNoSchedule,
					},
					{
						Key:      "key3333",
						Operator: v1.TolerationOpEqual,
						Value:    "value2",
						Effect:   v1.TaintEffectNoSchedule,
					},
				},
			},
		},
	}

	clientTools := syntesting.FakeClientTools()
	rtClient, err := clientTools.RuntimeClient()
	require.NoError(t, err)

	ctx := context.TODO()
	resources := renderResource(t, ctx, clientTools, syndesis, "assets/addons/jaeger/syndesis-jaeger.yml.tmpl")
	copy := (&resources[0]).DeepCopy()

	err = action.PreProcessForAffinityTolerations(ctx, rtClient, syndesis, &resources[0])
	require.NoError(t, err)
	assert.NotEqual(t, *copy, resources[0])

	affinity, found, err := unstructured.NestedFieldNoCopy(resources[0].UnstructuredContent(), "spec", "affinity")
	require.NoError(t, err)
	assert.True(t, found)
	assert.NotNil(t, affinity)

	toleration, found, err := unstructured.NestedFieldNoCopy(resources[0].UnstructuredContent(), "spec", "tolerations")
	require.NoError(t, err)
	assert.True(t, found)
	assert.NotNil(t, toleration)
}
