// +build !dev

package olm

import (
	"net/http"
)

func GetAssetsFS() http.FileSystem {
	return assets
}
