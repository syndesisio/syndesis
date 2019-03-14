#!/bin/bash
set -euo pipefail

dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
targetdir=${TARGET_DIR:-${dir}/..}

mkdir -p ${targetdir}

MESSAGE=$(cat <<'EOF'
#
# Do not edit, this is a generated file.  To regenerate,  run: ./generator/run.sh
#
EOF
)

go get -u github.com/spf13/cobra github.com/spf13/pflag github.com/hoisie/mustache github.com/imdario/mergo

cd $dir

echo "$MESSAGE" > ${targetdir}/syndesis.yml
go run syndesis-template.go --name=syndesis --prometheus-rules-file=../../app/integration/project-generator/src/main/resources/io/syndesis/integration/project/generator/templates/prometheus-config.yml $* >> ${targetdir}/syndesis.yml

# For re-enabling plain Docker images, use --with-docker-images when generating the template
