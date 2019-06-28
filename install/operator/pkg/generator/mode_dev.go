// +build dev

package generator

import (
	"github.com/syndesisio/syndesis/install/operator/pkg/generator/dev"
	"net/http"
)

func GetAssetsFS() http.FileSystem {
	return dev.GetAssetsFS()
}
