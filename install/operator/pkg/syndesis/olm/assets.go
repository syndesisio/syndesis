/*
 * Copyright (C) 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
//go:generate go run assets/assets_generate.go
package olm

import (
	"io/ioutil"
	"net/http"
	"os"
	"path/filepath"
	"sort"
	"time"

	"github.com/pkg/errors"
	"github.com/shurcooL/httpfs/filter"
	"github.com/syndesisio/syndesis/install/operator/pkg/build"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
)

func GetAssetsFS() http.FileSystem {
	assetsDir := filepath.Join(build.GO_MOD_DIRECTORY, "pkg", "syndesis", "olm", "assets")
	return util.NewFileInfoMappingFS(filter.Keep(http.Dir(assetsDir), func(path string, fi os.FileInfo) bool {
		if fi.Name() == "assets_generate.go" {
			return false
		}
		return true
	}), func(fi os.FileInfo) (os.FileInfo, error) {
		return &zeroTimeFileInfo{fi}, nil
	})
}

func AssetAsBytes(path string) ([]byte, error) {
	file, err := GetAssetsFS().Open(path)
	if err != nil {
		return nil, err
	}
	defer file.Close()
	fileData, err := ioutil.ReadAll(file)
	if err != nil {
		return nil, err
	}
	return fileData, nil
}

func isDirectory(path string) bool {
	f, err := GetAssetsFS().Open(path)
	if err != nil {
		return false
	}
	defer f.Close()

	info, err := f.Stat()
	if err != nil {
		return false
	}

	return info.IsDir()
}

func ReadDir(directory string) ([]string, error) {
	return ReadFSDir(GetAssetsFS(), directory)
}

func ReadFSDir(assets http.FileSystem, directory string) ([]string, error) {
	f, err := assets.Open(directory)
	if err != nil {
		return nil, err
	}
	defer f.Close()

	files, err := f.Readdir(-1)
	if err != nil {
		return nil, err
	}
	sort.Slice(files, func(i, j int) bool {
		return files[i].Name() < files[j].Name()
	})

	var content []string
	for _, f := range files {
		filePath := directory + "/" + f.Name()
		c, err := Read(filePath)
		if err != nil {
			return nil, err
		}
		content = append(content, c)
	}
	return content, nil
}

/*
 * Can read a file or directory of files, the latter being
 * done by delegating to readDir if file is a directory.
 */
func Read(filePath string) (string, error) {
	if isDirectory(filePath) {
		return "", errors.Errorf("The file path specified passed to Read() is a directory: %s", filePath)
	}

	c, err := AssetAsBytes(filePath)
	if err != nil {
		return "", err
	}

	return string(c), nil
}

type zeroTimeFileInfo struct {
	os.FileInfo
}

func (*zeroTimeFileInfo) ModTime() time.Time {
	return time.Time{}
}
