package util

import "strings"

func TagOf(image string) string {
	splits := strings.Split(image, ":")
	if len(splits) == 1 {
		return "latest"
	}
	return splits[len(splits)-1]
}
