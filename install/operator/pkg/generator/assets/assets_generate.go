//
// This whole directory is used to generate the assets_vfsdata.go file.  It's not compiled into the binary.
//
package main

import (
	"github.com/shurcooL/vfsgen"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator/dev"
	"log"
)

func main() {
	err := vfsgen.Generate(dev.GetAssetsFS(), vfsgen.Options{
		PackageName: "generator",
	})
	if err != nil {
		log.Fatalln(err)
	}
}
