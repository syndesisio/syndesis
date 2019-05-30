//
// This whole directory is used to generate the assets_vfsdata.go file.  It's not compiled into the binary.
//
package main

import (
    "github.com/shurcooL/httpfs/filter"
    "log"
    "net/http"
    "os"

    "github.com/shurcooL/vfsgen"
)

func main() {
    dir := filter.Keep(http.Dir("./assets"), func(path string, fi os.FileInfo) bool {
        if fi.Name() == ".DS_Store" {
            return false
        }
        if fi.Name() == "assets_generate.go" {
            return false
        }
        return true
    })

    err := vfsgen.Generate(dir, vfsgen.Options{
        PackageName: "generator",
    })
    if err != nil {
        log.Fatalln(err)
    }
}
