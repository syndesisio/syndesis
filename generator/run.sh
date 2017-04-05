#!/bin/bash

dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

MESSAGE=$(cat <<'EOF'
#
# Do not edit, this is a generated file.  To regenerate,  run: ./generator/run.sh
#
EOF)

go get -u github.com/spf13/cobra github.com/spf13/pflag github.com/hoisie/mustache

cd $dir

echo "$MESSAGE" > ${dir}/../ipaas.yml
go run ipaas-template.go --name=ipaas >> ${dir}/../ipaas.yml

echo "$MESSAGE" > ${dir}/../ipaas-dev.yml
go run ipaas-template.go --name=ipaas-dev --dev >> ${dir}/../ipaas-dev.yml

echo "$MESSAGE" > ${dir}/../ipaas-restricted.yml
go run ipaas-template.go --name=ipaas-restricted --restricted >> ${dir}/../ipaas-restricted.yml

echo "$MESSAGE" > ${dir}/../ipaas-ephemeral-restricted.yml
go run ipaas-template.go --name=ipaas-ephemeral-restricted --ephemeral --restricted >> ${dir}/../ipaas-ephemeral-restricted.yml

echo "$MESSAGE" > ${dir}/../ipaas-dev-restricted.yml
go run ipaas-template.go --name=ipaas-dev-restricted --restricted --dev >> ${dir}/../ipaas-dev-restricted.yml
