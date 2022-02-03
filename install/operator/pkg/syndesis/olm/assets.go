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

package olm

import (
	"embed"
	"io/ioutil"
	"os"
	"sort"
	"time"

	"github.com/pkg/errors"
)

//go:embed assets/*
var assets embed.FS

func AssetAsBytes(path string) ([]byte, error) {
	file, err := assets.Open(path)
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
	f, err := assets.Open(path)
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
	files, err := assets.ReadDir(directory)
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
