package configuration

import (
	"k8s.io/apimachinery/pkg/api/resource"
	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
	"strconv"
)

var log = logf.Log.WithName("controller")

func getString(config map[string]string, key SyndesisEnvVar) (string, bool) {
	v, ok := config[string(key)]
	return v, ok
}

func getBool(config map[string]string, key SyndesisEnvVar) (bool, bool) {
	if v, ok := config[string(key)]; ok {
		boolValue, err := strconv.ParseBool(v)
		if err != nil {
			log.Info(" property has not a bool value", "key", string(key), "value", v)
			return false, false
		}
		return boolValue, true
	}
	return false, false
}

func getInt(config map[string]string, key SyndesisEnvVar) (int, bool) {
	if v, ok := config[string(key)]; ok {
		intValue, err := strconv.Atoi(v)
		if err != nil {
			log.Info(" property has not a int value", "key", string(key), "value", v)
			return 0, false
		}
		return intValue, true
	}
	return 0, false
}

func getQuantity(config map[string]string, key SyndesisEnvVar) (resource.Quantity, bool) {
	if v, ok := config[string(key)]; ok {
		q, err := resource.ParseQuantity(v)
		if err != nil {
			log.Info(" property has not a quantity value ", "key", string(key), "value", v)
			return resource.Quantity{}, false
		}
		return q, true
	}
	return resource.Quantity{}, false
}
