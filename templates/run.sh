#!/bin/bash
set -euo pipefail

dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
targetdir=${TARGET_DIR:-${dir}/..}
#go get -u github.com/spf13/cobra github.com/spf13/pflag github.com/hoisie/mustache github.com/imdario/mergo
cd $dir

rm -rf  ${targetdir}/rhel/*
go run image-template.go $* \
   --target=${targetdir}/rhel \
   --from=registry.access.redhat.com/jboss-fuse-6/fis-java-openshift:latest

rm -rf  ${targetdir}/centos/*
go run image-template.go $* \
   --target=${targetdir}/centos \
   --from=fabric8/s2i-java:latest

