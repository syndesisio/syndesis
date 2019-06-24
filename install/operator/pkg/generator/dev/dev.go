package dev

import (
	"github.com/shurcooL/httpfs/filter"
	"github.com/syndesisio/syndesis/install/operator/pkg/build"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"net/http"
	"os"
	"path/filepath"
	"time"
)

func GetAssetsFS() http.FileSystem {
	assetsDir := filepath.Join(build.GO_MOD_DIRECTORY, "pkg", "generator", "assets")
	return util.NewFileInfoMappingFS(filter.Keep(http.Dir(assetsDir), func(path string, fi os.FileInfo) bool {
		if fi.Name() == ".DS_Store" {
			return false
		}
		if fi.Name() == "assets_generate.go" {
			return false
		}
		return true
	}), func(fi os.FileInfo) (os.FileInfo, error) {
		return &zeroTimeFileInfo{fi}, nil
	})
}

type zeroTimeFileInfo struct {
	os.FileInfo
}

func (*zeroTimeFileInfo) ModTime() time.Time {
	return time.Time{}
}
