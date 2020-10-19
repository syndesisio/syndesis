//
// This whole directory is used to generate the assets_vfsdata.go file.  It's not compiled into the binary.
//
package main

import (
	"log"

	"github.com/shurcooL/vfsgen"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/olm"
)

func main() {
	err := vfsgen.Generate(olm.GetAssetsFS(), vfsgen.Options{
		PackageName: "olm",
	})
	if err != nil {
		log.Fatalln(err)
	}
}
