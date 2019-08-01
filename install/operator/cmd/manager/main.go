package main

import (
	"context"
	"fmt"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd"
	_ "k8s.io/client-go/plugin/pkg/client/auth/gcp"
	"math/rand"
	"os"
	"path/filepath"
	"strings"
	"time"
)

func main() {
	rand.Seed(time.Now().UTC().UnixNano())
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel() // Cancel ctx as soon as main returns

	syndesis, err := cmd.NewOperator(ctx)
	exeName := filepath.Base(os.Args[0])
	if !strings.Contains(exeName, "go_build_main_go") {
		syndesis.Use = exeName
	}
	exitOnError(err)

	err = syndesis.Execute()
	exitOnError(err)
}

func exitOnError(err error) {
	if err != nil {
		fmt.Println("error:", err)
		os.Exit(1)
	}
}
