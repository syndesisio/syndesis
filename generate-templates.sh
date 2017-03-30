#!/bin/bash

MESSAGE=$(cat <<'EOF'
#
# Do not edit, this is a generated file.  To regenerate,  run: ./generate-templates.sh
#
EOF)

go get -u github.com/spf13/cobra github.com/spf13/pflag github.com/hoisie/mustache

echo "$MESSAGE" > redhat-ipaas.yml
go run ipaas-template.go --name=redhat-ipaas >> redhat-ipaas.yml

echo "$MESSAGE" > redhat-ipaas-dev.yml
go run ipaas-template.go --name=redhat-ipaas-dev --dev-mode >> redhat-ipaas-dev.yml

echo "$MESSAGE" > redhat-ipaas-single-tenant.yml
go run ipaas-template.go --name=redhat-ipaas-single-tenant --single-tenant >> redhat-ipaas-single-tenant.yml

echo "$MESSAGE" > redhat-ipaas-ephemeral-single-tenant.yml
go run ipaas-template.go --name=redhat-ipaas-single-tenant --ephemeral --single-tenant >> redhat-ipaas-ephemeral-single-tenant.yml

echo "$MESSAGE" > redhat-ipaas-dev-single-tenant.yml
go run ipaas-template.go --name=redhat-ipaas-dev-single-tenant --single-tenant --dev-mode >> redhat-ipaas-dev-single-tenant.yml