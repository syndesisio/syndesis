package addons

import (
	"io/ioutil"
	"path/filepath"

	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
)

func GetAddonsResources(addonsDir string) ([]*unstructured.Unstructured, error) {
	files, err := ioutil.ReadDir(addonsDir)
	if err != nil {
		return nil, err
	}

	res := make([]*unstructured.Unstructured, 0)
	for _, f := range files {
		if f.IsDir() {
			// we may want to recursively install addons
			continue
		}

		addon, err := util.LoadUnstructuredObjectFromFile(filepath.Join(*configuration.AddonsDirLocation, f.Name()))
		if err != nil {
			return nil, err
		}
		res = append(res, addon)
	}

	return res, nil
}
