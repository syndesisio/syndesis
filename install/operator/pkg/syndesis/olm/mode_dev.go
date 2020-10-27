// +build dev

package olm

import (
	"net/http"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/olm/dev"
)

func GetAssetsFS() http.FileSystem {
	return dev.GetAssetsFS()
}
