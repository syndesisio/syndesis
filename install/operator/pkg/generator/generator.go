//
//go:generate go run assets/assets_generate.go
package generator

import (
	"errors"
	"fmt"
	"github.com/hoisie/mustache"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"io/ioutil"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"net/http"
	"regexp"
	"sort"
	"strings"
)

type supportImages struct {
	Postgresql       string
	OAuthProxy       string
	OAuthProxyImage  string
	Prometheus       string
	PostgresExporter string
}

type syndesisImages struct {
	Rest     string
	Ui       string
	Verifier string
	S2i      string
	Upgrade  string
}

type images struct {
	Support                     supportImages
	Syndesis                    syndesisImages
	ImageStreamNamespace        string
	SyndesisImagesPrefix        string
	OAuthProxyImagePrefix       string
	PrometheusImagePrefix       string
	PostgresExporterImagePrefix string
}

type tags struct {
	Syndesis         string
	Postgresql       string
	OAuthProxy       string
	Prometheus       string
	Upgrade          string
	PostgresExporter string
}

type Context struct {
	AllowLocalHost   bool
	WithDockerImages bool
	Productized      bool
	EarlyAccess      bool
	Oso              bool
	Ocp              bool
	Tag              string
	Registry         string
	Images           images
	Tags             tags
	Debug            bool
	PrometheusRules  string
	Env              map[string]string
}

// TODO: Could be added from a local configuration file

func CreateSyndesisContext() Context {
	return assignPrometheusConfig(Context{
		Images: images{
			SyndesisImagesPrefix:        "syndesis",
			OAuthProxyImagePrefix:       "quay.io/openshift",
			PrometheusImagePrefix:       "prom",
			PostgresExporterImagePrefix: "wrouesnel",
			Support: supportImages{
				Postgresql:       "postgresql",
				OAuthProxy:       "oauth-proxy",
				OAuthProxyImage:  "origin-oauth-proxy",
				Prometheus:       "prometheus",
				PostgresExporter: "postgres_exporter",
			},
			Syndesis: syndesisImages{
				Rest:     "syndesis-server",
				Ui:       "syndesis-ui",
				Verifier: "syndesis-meta",
				S2i:      "syndesis-s2i",
				Upgrade:  "syndesis-upgrade",
			},
		},
		Tags: tags{
			Postgresql:       "9.5",
			OAuthProxy:       "v4.0.0",
			Prometheus:       "v2.1.0",
			PostgresExporter: "v0.4.7",
		},
	})
}

// TODO: Update with product image references here
func CreateProductContext() Context {
	return assignPrometheusConfig(Context{
		Images: images{
			ImageStreamNamespace:        "fuse-ignite",
			SyndesisImagesPrefix:        "fuse7",
			OAuthProxyImagePrefix:       "openshift",
			PrometheusImagePrefix:       "prom",
			PostgresExporterImagePrefix: "wrouesnel",
			Support: supportImages{
				Postgresql:       "postgresql",
				OAuthProxy:       "oauth-proxy",
				Prometheus:       "prometheus",
				PostgresExporter: "postgres_exporter",
			},
			Syndesis: syndesisImages{
				Rest:     "fuse-ignite-server",
				Ui:       "fuse-ignite-ui",
				Verifier: "fuse-ignite-meta",
				S2i:      "fuse-ignite-s2i",
				Upgrade:  "fuse-ignite-upgrade",
			},
		},
		Tags: tags{
			Postgresql:       "9.5",
			OAuthProxy:       "v1.1.0",
			Prometheus:       "v2.1.0",
			PostgresExporter: "v0.4.7",
		},
		Registry: "registry.fuse-ignite.openshift.com",
	})
}

func assignPrometheusConfig(context Context) Context {
	prometheusRules, err := AssetAsBytes("/prometheus-config.yml")
	if err != nil {
		panic(err)
	}
	var prometheusRulesIdentEx = regexp.MustCompile(`(.+)`)
	context.PrometheusRules = prometheusRulesIdentEx.ReplaceAllString(string(prometheusRules), "      $1")
	return context
}

func AssetAsBytes(path string) ([]byte, error) {
	file, err := GetAssetsFS().Open(path)
	if err != nil {
		return nil, err
	}
	defer file.Close()
	prometheusRules, err := ioutil.ReadAll(file)
	if err != nil {
		return nil, err
	}
	return prometheusRules, nil
}

func RenderDir(directory string, context interface{}) ([]unstructured.Unstructured, error) {
	return RenderFSDir(GetAssetsFS(), directory, context)
}

func RenderFSDir(assets http.FileSystem, directory string, context interface{}) ([]unstructured.Unstructured, error) {
	f, err := assets.Open(directory)
	if err != nil {
		return nil, err
	}
	defer f.Close()

	files, err := f.Readdir(-1)
	if err != nil {
		return nil, err
	}
	sort.Slice(files, func(i, j int) bool {
		return files[i].Name() < files[j].Name()
	})

	response := []unstructured.Unstructured{}
	for _, f := range files {
		filePath := directory + f.Name()
		if strings.HasSuffix(f.Name(), ".yml.mustache") {
			template, err := AssetAsBytes(filePath)
			if err != nil {
				return nil, err
			}

			rawYaml := mustache.Render(string(template), context)
			var obj interface{} = nil
			err = util.UnmarshalYaml([]byte(rawYaml), &obj)
			if err != nil {
				return nil, fmt.Errorf("%s:\n%s\n", err, rawYaml)
			}

			switch v := obj.(type) {
			case []interface{}:
				for _, value := range v {
					if x, ok := value.(map[string]interface{}); ok {
						u := unstructured.Unstructured{x}
						//annotatedForDebugging(u, name, rawYaml)
						response = append(response, u)
					} else {
						return nil, errors.New("list did not contain objects")
					}
				}
			case map[string]interface{}:
				u := unstructured.Unstructured{v}
				//annotatedForDebugging(u, name, rawYaml)
				response = append(response, u)
			case nil:
				// It's ok if a template chooses not to generate any resources..

			default:
				return nil, fmt.Errorf("Unexptected yaml unmarshal type: %v", obj)
			}
		}
	}

	return response, nil
}

func annotatedForDebugging(u unstructured.Unstructured, location string, rawYaml string) {
	annotations := u.GetAnnotations()
	if annotations == nil {
		annotations = map[string]string{}
	}
	annotations["template-location"] = location
	annotations["template-rendered"] = rawYaml
	u.SetAnnotations(annotations)
}
