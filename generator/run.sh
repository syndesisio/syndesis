#!/bin/bash

dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

MESSAGE=$(cat <<'EOF'
#
# Do not edit, this is a generated file.  To regenerate,  run: ./generator/run.sh
#
EOF
)

go get -u github.com/spf13/cobra github.com/spf13/pflag github.com/hoisie/mustache

cd $dir

echo "$MESSAGE" > ${dir}/../syndesis.yml
go run syndesis-template.go --name=syndesis >> ${dir}/../syndesis.yml

echo "$MESSAGE" > ${dir}/../syndesis-dev.yml
go run syndesis-template.go --name=syndesis-dev --dev >> ${dir}/../syndesis-dev.yml

echo "$MESSAGE" > ${dir}/../syndesis-restricted.yml
go run syndesis-template.go --name=syndesis-restricted --restricted >> ${dir}/../syndesis-restricted.yml

echo "$MESSAGE" > ${dir}/../syndesis-ephemeral-restricted.yml
go run syndesis-template.go --name=syndesis-ephemeral-restricted --ephemeral --restricted >> ${dir}/../syndesis-ephemeral-restricted.yml

echo "$MESSAGE" > ${dir}/../syndesis-dev-restricted.yml
go run syndesis-template.go --name=syndesis-dev-restricted --restricted --dev >> ${dir}/../syndesis-dev-restricted.yml
