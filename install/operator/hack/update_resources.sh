#!/bin/sh

# Embeds resources in the `resources` dir into the `bindata.go` file.
# Run it after changing content of any file in `resources/*`.

# Requires go-bindata in the path

basedir=$(dirname "$0")

cd $basedir/../resources
go-bindata -pkg resources .
