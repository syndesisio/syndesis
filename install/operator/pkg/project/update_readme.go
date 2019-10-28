package main

//go:generate go run update_readme.go

import (
	"io/ioutil"
	"k8s.io/apiextensions-apiserver/pkg/apis/apiextensions"
	"log"
	"os"
	"sigs.k8s.io/yaml"
	"text/template"
)

type property struct {
	DataType    string
	Description string
}

func main() {
	bytes, err := ioutil.ReadFile("../../deploy/crds/syndesis_v1alpha1_syndesis_crd.yaml")
	handle(err)

	var syndesis apiextensions.CustomResourceDefinition
	err = yaml.Unmarshal(bytes, &syndesis)
	handle(err)

	fields := remap(syndesis)

	readmeTemplate, err := template.ParseFiles("../../README.md.template")
	handle(err)

	readme, err := os.Create("../../README.md")
	handle(err)
	defer readme.Close()
	readmeTemplate.Execute(readme, fields)
	readme.Sync()
}

func handle(err error) {
	if err != nil {
		log.Fatal(err)
	}
}

func remap(crd apiextensions.CustomResourceDefinition) map[string]property {
	spec := crd.Spec.Validation.OpenAPIV3Schema.Properties["spec"]

	return remapProperties("spec", spec)
}

func remapProperties(path string, props apiextensions.JSONSchemaProps) map[string]property {
	var ret map[string]property = make(map[string]property)
	for k, v := range props.Properties {
		if v.Type == "object" {
			for k, v := range remapProperties(path+"."+k, v) {
				ret[k] = v
			}
		} else {
			ret[path+"."+k] = property{
				v.Type,
				v.Description,
			}
		}
	}

	return ret
}
