package validation

import (
	"fmt"
	"github.com/go-openapi/spec"
	"reflect"
	"strings"
)

type SchemaEntry struct {
	Path string
	Type string
}

func getMissingEntries(schema *spec.Schema, crInstance interface{}) []SchemaEntry {
	var entries []SchemaEntry
	crStruct := reflect.ValueOf(crInstance).Elem().Type()
	if field, found := crStruct.FieldByName("Spec"); found {
		entries = validateField(entries, *schema, "", field)
	}
	if field, found := crStruct.FieldByName("Status"); found {
		entries = validateField(entries, *schema, "", field)
	}
	return entries
}

func validateField(entries []SchemaEntry, schema spec.Schema, context string, field reflect.StructField) []SchemaEntry {
	reflectType := getActualType(field)
	if !field.Anonymous {
		name := getFieldName(field)
		context = fmt.Sprintf("%s/%s", context, name)
		schema = schema.Properties[name]
		expectedType := equivalentSchemaType(reflectType.Kind())
		if !schema.Type.Contains(expectedType) {
			entries = append(entries, SchemaEntry{context, expectedType})
		}
	}
	if isArray(reflectType) {
		reflectType = reflectType.Elem()
		if schema.Items != nil {
			schema = *schema.Items.Schema
		}
	}
	for _, field := range getChildren(field) {
		entries = validateField(entries, schema, context, field)
	}
	return entries
}

func getChildren(field reflect.StructField) []reflect.StructField {
	reflectType := getActualType(field)
	if reflectType.Kind() == reflect.Struct {
		return getFields(reflectType)
	} else if isArray(reflectType) {
		elem := reflectType.Elem()
		if elem.Kind() == reflect.Struct {
			return getFields(elem)
		}
	}
	return nil
}

func isArray(fieldType reflect.Type) bool {
	switch fieldType.Kind() {
	case reflect.Slice:
		return true
	case reflect.Array:
		return true
	default:
		return false
	}
}

func getFields(fieldType reflect.Type) []reflect.StructField {
	var children []reflect.StructField
	for index := 0; index < fieldType.NumField(); index++ {
		children = append(children, fieldType.Field(index))
	}
	return children
}

func getActualType(field reflect.StructField) reflect.Type {
	reflectType := field.Type
	if reflectType.Kind() == reflect.Ptr {
		reflectType = reflectType.Elem()
	}
	return reflectType
}

func equivalentSchemaType(kind reflect.Kind) string {
	switch kind {
	case reflect.String:
		return "string"
	case reflect.Float32:
		return "number"
	case reflect.Float64:
		return "number"
	case reflect.Int:
		return "integer"
	case reflect.Int8:
		return "integer"
	case reflect.Int16:
		return "integer"
	case reflect.Int32:
		return "integer"
	case reflect.Int64:
		return "integer"
	case reflect.Bool:
		return "boolean"
	case reflect.Struct:
		return "object"
	case reflect.Ptr:
		return "object"
	case reflect.Map:
		return "object"
	case reflect.Array:
		return "array"
	case reflect.Slice:
		return "array"
	default:
		return ""
	}
}

func getFieldName(field reflect.StructField) string {
	tag := string(field.Tag)
	parts := strings.Split(tag, ":")
	if len(parts) == 1 || parts[0] != "json" {
		return field.Name
	} else {
		quotesRemoved := strings.Replace(parts[1], "\"", "", -1)
		commaDelimited := strings.Split(quotesRemoved, ",")
		spaceDelimited := strings.Split(commaDelimited[0], " ")
		return spaceDelimited[0]
	}
}
