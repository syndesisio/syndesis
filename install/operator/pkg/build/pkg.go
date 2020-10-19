package build

import (
	"os"
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
