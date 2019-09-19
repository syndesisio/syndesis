//
//go:generate go run assets/assets_generate.go
package generator

import (
	"bytes"
	"fmt"
	"io/ioutil"
	"net/http"
	"reflect"
	"sort"
	"strings"
	"text/template"

	"github.com/pkg/errors"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	v1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
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
	Komodo   string
}

type images struct {
	Support                     supportImages
	Syndesis                    syndesisImages
	ImageStreamNamespace        string
	SyndesisImagesPrefix        string
	OAuthProxyImagePrefix       string
	PrometheusImagePrefix       string
	PostgresExporterImagePrefix string
	KomodoImagesPrefix          string
	CamelKBaseImagePrefix       string
}

type tags struct {
	Syndesis         string
	Postgresql       string
	OAuthProxy       string
	Prometheus       string
	Upgrade          string
	PostgresExporter string
	Komodo           string
	CamelKBase       string
}

type Context struct {
	ProductName      string
	AllowLocalHost   bool
	Productized      bool
	EarlyAccess      bool
	Oso              bool
	Ocp              bool
	TagMinor         string
	TagMajor         string
	Registry         string
	Images           images
	Tags             tags
	Debug            bool
	PrometheusRules  string
	Env              map[string]string
	Syndesis         *v1alpha1.Syndesis
	ImagePullSecrets []string
	Versions         map[string]string
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

var templateFunctions = template.FuncMap{
	"mapHasKey": func(item reflect.Value, key reflect.Value) (bool, error) {
		if item.Kind() != reflect.Map {
			return false, fmt.Errorf("mapHasKey requires a map type")
		}
		if x := item.MapIndex(key); x.IsValid() {
			return true, nil
		} else {
			return false, nil
		}
	},
	"memoryLimit": func(limits v1.ResourceList) (string, error) {
		if l := limits.Memory(); l != nil && l.Value() > 0 {
			return l.String(), nil
		}

		return "", nil
	},
	"addonsValue": func(addons v1alpha1.AddonsSpec, key1 string, key2 string) (string, error) {
		if addons == nil || len(key1) == 0 || len(key2) == 0 {
			return "", nil
		}

		return addons[key1][key2], nil
	},
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
		r, err := Render(filePath, context)
		if err != nil {
			return nil, err
		}
		response = append(response, r...)
	}
	return response, nil
}

func Render(filePath string, context interface{}) ([]unstructured.Unstructured, error) {
	var obj interface{} = nil
	response := []unstructured.Unstructured{}

	// We can load plain yml files..
	if strings.HasSuffix(filePath, ".yml") || strings.HasSuffix(filePath, ".yaml") {
		fileData, err := AssetAsBytes(filePath)
		if err != nil {
			return nil, err
		}

		err = util.UnmarshalYaml(fileData, &obj)
		if err != nil {
			return nil, errors.Errorf("%s:\n%s\n", err, string(fileData))
		}
	}

	// We can process go lang templates.
	if strings.HasSuffix(filePath, ".yml.tmpl") || strings.HasSuffix(filePath, ".yaml.tmpl") {
		fileData, err := AssetAsBytes(filePath)
		if err != nil {
			return nil, err
		}

		tmpl, err := template.New(filePath).Funcs(templateFunctions).Parse(string(fileData))
		if err != nil {
			return nil, err
		}

		buffer := &bytes.Buffer{}
		err = tmpl.Execute(buffer, context)
		if err != nil {
			return nil, err
		}
		rawYaml := buffer.Bytes()

		err = util.UnmarshalYaml(rawYaml, &obj)
		if err != nil {
			return nil, errors.Errorf("%s: %s: \n%s\n", filePath, err, string(rawYaml))
		}
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

	return response, nil
}

func Must(data []byte, err error) []byte {
	if err != nil {
		panic(err)
	}
	return data
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
