package validation

import (
	"fmt"

	"github.com/ghodss/yaml"
	"github.com/go-openapi/spec"
	"github.com/go-openapi/strfmt"
	"github.com/go-openapi/validate"
)

type Schema interface {
	GetMissingEntries(crInstance interface{}) []SchemaEntry
	Validate(data interface{}) error
}

func New(crd []byte) (Schema, error) {
	object := &customResourceDefinition{}
	err := yaml.Unmarshal(crd, object)
	if err != nil {
		return nil, err
	}
	return &openAPIV3Schema{&object.Spec.Validation.OpenAPIV3Schema}, nil
}

func NewVersioned(crd []byte, version string) (Schema, error) {
	object := &customResourceDefinition{}
	err := yaml.Unmarshal(crd, object)
	if err != nil {
		return nil, err
	}
	for _, v := range object.Spec.Versions {
		if v.Name == version {
			return &openAPIV3Schema{&v.Schema.OpenAPIV3Schema}, nil
		}
	}
	return &openAPIV3Schema{}, fmt.Errorf("no version %s detected in crd", version)
}

type openAPIV3Schema struct {
	schema *spec.Schema
}

func (schema *openAPIV3Schema) GetMissingEntries(crInstance interface{}) []SchemaEntry {
	return getMissingEntries(schema.schema, crInstance)
}

func (schema *openAPIV3Schema) Validate(data interface{}) error {
	return validate.AgainstSchema(schema.schema, data, strfmt.Default)
}

type customResourceDefinition struct {
	Spec customResourceDefinitionSpec `json:"spec,omitempty"`
}

type customResourceDefinitionSpec struct {
	Versions   []customResourceDefinitionVersion  `json:"versions,omitempty"`
	Validation customResourceDefinitionValidation `json:"validation,omitempty"`
}

type customResourceDefinitionVersion struct {
	Name   string                             `json:"Name,omitempty"`
	Schema customResourceDefinitionValidation `json:"schema,omitempty"`
}

type customResourceDefinitionValidation struct {
	OpenAPIV3Schema spec.Schema `json:"openAPIV3Schema,omitempty"`
}
