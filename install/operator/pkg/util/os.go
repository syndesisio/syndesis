package util

import (
	"fmt"
	"os"
)

func ExitOnError(err error) {
	if err != nil {
		fmt.Println("error:", err)
		os.Exit(1)
	}
}
