package util

import (
	"bytes"
	"fmt"
	"sigs.k8s.io/yaml"
	"text/template"
)

//
// Debug lets you print to the console a message and a value or fields of the value.
// if no fields are specified the whole value is dumped to the console in yaml format.
//
func Debug(msg string, o interface{}, fields ...string) {
	s := Dump(o, fields...)
	if s != "" {
		fmt.Println(msg, s)
	}
}

//
// Dump returns the fields of the value o as string.  If no fields
// are specified, then the whole value is dumped as yaml.
// fields are specified using go template expression like ".MyField"
func Dump(o interface{}, fields ...string) string {
	if len(fields) == 0 {
		data, err := yaml.Marshal(o)
		if err != nil {
			panic(err)
		} else {
			return string(data)
		}
	}

	rc := ""
	for _, field := range fields {
		text, err := RenderGoTemplate("{{"+field+"}}", o)
		if err != nil {
			return ""
		}
		if len(rc) > 0 {
			rc += "\n    "
		}
		rc += field + " => " + text
	}
	return rc
}

func RenderGoTemplate(goTemplate string, o interface{}) (string, error) {
	t, err := template.New("RenderTemplate").Parse(goTemplate)
	if err != nil {
		panic(err)
	}
	buffer := &bytes.Buffer{}
	err = t.Execute(buffer, o)
	return string(buffer.Bytes()), err
}

func MustRenderGoTemplate(goTemplate string, o interface{}) string {
	return MustString(RenderGoTemplate(goTemplate, o))
}
