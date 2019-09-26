package util

import (
	"context"
	"encoding/base64"
	"fmt"
	"github.com/spf13/pflag"
	"k8s.io/apimachinery/pkg/api/resource"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime"
	"reflect"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

var FlagSet *pflag.FlagSet = nil
var showResourceDiffs = false

func init() {
	FlagSet = pflag.NewFlagSet("util", pflag.ExitOnError)
	FlagSet.BoolVar(&showResourceDiffs, "print-resource-diffs", false, "Enable printing resource diffs for resources that get updated.")
}

func CreateOrUpdate(ctx context.Context, cl client.Client, o runtime.Object, skipFields ...string) (*unstructured.Unstructured, controllerutil.OperationResult, error) {

	desired, err := ToUnstructured(o)
	if err != nil {
		return desired, controllerutil.OperationResultNone, err
	}

	originalYaml := ""
	updatedYaml := ""

	createdCopy := desired.DeepCopy()
	modType, err := controllerutil.CreateOrUpdate(ctx, cl, createdCopy, func(o runtime.Object) error {

		existing := o.(*unstructured.Unstructured)
		originalYaml = Dump(existing)

		mergePath := desired.GetAPIVersion() + "/" + desired.GetKind()
		if len(skipFields) == 0 {
			skipFields = append(skipFields, "kind", "apiVersion", "status")
		}

		skip := map[string]bool{}
		for _, value := range skipFields {
			skip[mergePath+"/"+value] = true
		}

		mergeMap(mergePath, existing.Object, desired.Object, skip)
		updatedYaml = Dump(existing)

		//if d.GetKind() == "DeploymentConfig" && d.GetName() == "syndesis-meta" {
		//	Debug("existing:", existing, "(index .Object.spec.template.spec.containers 0).resources.limits.memory")
		//}
		return nil
	})

	if showResourceDiffs && modType == controllerutil.OperationResultUpdated {
		fmt.Println("resource", desired.GetKind(), "update:", desired.GetName())
		fmt.Println(UnifiedDiff(originalYaml, updatedYaml))
	}
	return createdCopy, modType, err
}

func mergeMap(path string, to map[string]interface{}, from map[string]interface{}, skip map[string]bool) {
	if path == "v1/Secret" {
		mergeSecretValues(to, from)
	}
	if path == "image.openshift.io/v1/ImageStream/spec/tags/#" {
		// ImageStreamTag kinds are imply any previous importPolicy has to be removed.
		if MustRenderGoTemplate("{{.from.kind}}", from) == "ImageStreamTag" {
			to["importPolicy"] = map[string]interface{}{}
		}
	}
	for key, value := range from {
		field := path + "/" + key
		if skip[field] {
			continue
		}

		// handle cases like https://issues.jboss.org/browse/ENTESB-11711 setting a env value to "" does not work well, k8s gives delete
		// the value field under the covers, and we keep trying to set it again to the "" value.
		if field == "apps.openshift.io/v1/DeploymentConfig/spec/template/spec/containers/#/env/#/value" && (value == nil || value == "") {
			delete(to, key)
			continue
		}

		to[key] = mergeValue(field, to[key], value, skip)
	}
}

func mergeArray(path string, to []interface{}, from []interface{}, skip map[string]bool) []interface{} {
	nexPath := path + "/#"
	for key, value := range from {
		if key < len(to) {
			to[key] = mergeValue(nexPath, to[key], value, skip)
		} else {
			to = append(to, mergeValue(nexPath, nil, value, skip))
		}
	}
	return to
}

func mergeValue(path string, to interface{}, from interface{}, skip map[string]bool) interface{} {

	if skip[path] {
		return to
	}

	switch from := from.(type) {
	case map[string]interface{}:
		if to, ok := to.(map[string]interface{}); ok {
			mergeMap(path, to, from, skip)
			return to
		}
	case []interface{}:
		if toMap, ok := to.([]interface{}); ok {
			return mergeArray(path, toMap, from, skip)
		}
	}
	if from == to || from == nil {
		return to
	}
	if to == nil {
		return from
	}

	// Looks like we might have a different value...

	// Apply special handling for some fields.
	switch path {
	case "apps.openshift.io/v1/DeploymentConfig/spec/template/spec/containers/#/image":
		return to
	case "apps.openshift.io/v1/DeploymentConfig/spec/triggers/#/imageChangeParams/from/namespace":
		return to
	case "v1/PersistentVolumeClaim/spec/resources/requests/storage":
		return to
	case "apps.openshift.io/v1/DeploymentConfig/spec/template/spec/containers/#/resources/limits/memory":
		// This might be the same value, in a different format.
		fromQ := resource.MustParse(fmt.Sprint(from))
		fromI, _ := fromQ.AsInt64()
		toQ := resource.MustParse(fmt.Sprint(to))
		toI, _ := toQ.AsInt64()
		if fromI == toI {
			return to
		} else {
			return from
		}
	}

	fromT := reflect.TypeOf(from)
	toT := reflect.TypeOf(to)
	if fromT != toT && fromT.ConvertibleTo(toT) {
		from = reflect.ValueOf(from).Convert(toT).Interface()
		if from == to {
			return to
		} else {
			return from
		}
	}

	return from
}

func mergeSecretValues(to map[string]interface{}, from map[string]interface{}) {
	if from["stringData"] != nil && to["data"] != nil {
		stringData := from["stringData"].(map[string]interface{})
		data := to["data"].(map[string]interface{})

		updates := map[string]interface{}{}
		for key, value := range stringData {
			if plain, ok := value.(string); ok {
				encoded := base64.StdEncoding.EncodeToString([]byte(plain))
				if data[key] != encoded {
					updates[key] = value
				}
			}
		}
		if len(updates) > 0 {
			from["stringData"] = updates
		} else {
			delete(from, "stringData")
		}
	}
}
