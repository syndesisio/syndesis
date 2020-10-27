package build

import (
	"os"
	"path/filepath"
)

var (
	GoModDirectory string
)

func FileExists(name string) bool {
	stat, err := os.Stat(name)
	if err != nil {
		if os.IsNotExist(err) {
			return false
		}
	}
	return !stat.IsDir()
}

func init() {

	// Save the original directory the process started in.
	wd, err := os.Getwd()
	if err != nil {
		panic(err)
	}
	initialDir, err := filepath.Abs(wd)
	if err != nil {
		panic(err)
	}

	// Find the module dir..
	current := ""
	for next := initialDir; current != next; next = filepath.Dir(current) {
		current = next
		if FileExists(filepath.Join(current, "go.mod")) && FileExists(filepath.Join(current, "go.sum")) {
			GoModDirectory = current
			break
		}
	}

	if GoModDirectory == "" {
		panic("could not find the root module directory")
	}
}
