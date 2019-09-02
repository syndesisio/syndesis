package action

import (
	"encoding/json"
	"github.com/stretchr/testify/require"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/build"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"path/filepath"
	"strings"
	"testing"
)

const succeed = "\u2713"
const failed = "\u2717"

func TestTagsDefautValues(t *testing.T) {
	templateConfig, err := util.LoadJsonFromFile(filepath.Join(build.GO_MOD_DIRECTORY, "build", "conf", "config.yaml"))
	require.NoError(t, err)

	// Parse the config
	gen := &generator.Context{}
	err = json.Unmarshal(templateConfig, gen)
	require.NoError(t, err)

	syndesis := &v1alpha1.Syndesis{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: "TEST",
		},
		Spec: v1alpha1.SyndesisSpec{},
	}
	gen.Syndesis = syndesis

	err = template.SetupRenderContext(gen, syndesis, template.ResourceParams{}, map[string]string{})
	require.NoError(t, err)

	configuration.SetConfigurationFromEnvVars(gen.Env, syndesis)

	var flagtests = []struct {
		name  string
		value string
		def   string
	}{
		{"Server", syndesis.Spec.Components.Server.Tag, gen.Tags.Syndesis},
		{"Meta", syndesis.Spec.Components.Meta.Tag, gen.Tags.Syndesis},
		{"UI", syndesis.Spec.Components.UI.Tag, gen.Tags.Syndesis},
		{"Komodo", syndesis.Spec.Components.Komodo.Tag, gen.Tags.Komodo},
		{"Oauth", syndesis.Spec.Components.Oauth.Tag, gen.Tags.OAuthProxy},
		{"Prometheus", syndesis.Spec.Components.Prometheus.Tag, gen.Tags.Prometheus},
		{"Postgres exporter", syndesis.Spec.Components.PostgresExporter.Tag, gen.Tags.PostgresExporter},
		{"Postgresql", syndesis.Spec.Components.Db.Tag, gen.Tags.Postgresql},
	}

	{
		t.Logf("\tTest: When creating a CR without any values for tags.")
		for _, tt := range flagtests {
			if strings.Compare(tt.value, tt.name) == 0 {
				t.Fatalf("\t%s\t%s tag is missing in CR, it should have a default value of [%s] and got [%s]", failed,
					tt.name,
					tt.value,
					tt.def)
			}
			t.Logf("\t%s\t%s tag is missing in CR, its default value is correct", succeed, tt.name)
		}
	}
}

func TestCheckTags(t *testing.T) {
	gen := &generator.Context{
		TagMajor: "1.6",
		TagMinor: "1.4",
	}
	syndesis := &v1alpha1.Syndesis{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: "TEST",
		},
		Spec: v1alpha1.SyndesisSpec{
			Components: v1alpha1.ComponentsSpec{
				Server: v1alpha1.ServerConfiguration{
					Tag: "1.4.2",
				},
				Meta: v1alpha1.MetaConfiguration{
					Tag: "1.5.0",
				},
				UI: v1alpha1.UIConfiguration{
					Tag: "1.5-beta",
				},
				S2I: v1alpha1.S2IConfiguration{
					Tag: "1.5.0-RC1",
				},
			},
		},
	}
	gen.Syndesis = syndesis

	// Check that image tags are correctly validated
	{
		t.Logf("\tTest: When testing all image tags.")
		err := checkTags(gen)
		if err != nil {
			t.Fatalf("\t%s\tAll tags are valid but got error: (%v)", failed, err)
		}
		t.Logf("\t%s\tAll tags are valid.", succeed)

		gen.Syndesis.Spec.Components.Server.Tag = "1.7"
		err = checkTags(gen)
		if err == nil {
			t.Fatalf("\t%s\tAll tags are valid but server tag [1.7 not in range: >=1.4 <1.6], it should fail: (%v)", failed, err)
		}
		t.Logf("\t%s\tServer tag is invalid [1.7 not in range: >=1.4 <1.6], and it should fail.", succeed)

		gen.Syndesis.Spec.Components.Server.Tag = "some_invalid_tag"
		err = checkTags(gen)
		if err == nil {
			t.Fatalf("\t%s\tAll tags are valid but server tag [some_invalid_tag has an invalid format], it should fail: (%v)", failed, err)
		}
		t.Logf("\t%s\tAll tags are valid but server tag [some_invalid_tag has an invalid format], and it should fail.", succeed)
	}
}
