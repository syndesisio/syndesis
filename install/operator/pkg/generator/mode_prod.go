// +build !dev

package generator

import (
	"net/http"
)

func GetAssetsFS() http.FileSystem {
	return assets
}
