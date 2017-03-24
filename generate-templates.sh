#!/bin/bash

go get -u github.com/spf13/cobra github.com/spf13/pflag github.com/hoisie/mustache

cat > redhat-ipaas.yml <<EOF
#
# Do not edit, this is a generated file.
#
EOF
go run ipaas-template.go --name=redhat-ipaas >> redhat-ipaas.yml

cat > redhat-ipaas-dev.yml <<EOF
#
# Do not edit, this is a generated file.
#
EOF
go run ipaas-template.go --name=redhat-ipaas-dev --dev-mode >> redhat-ipaas-dev.yml

cat > redhat-ipaas-single-tenant.yml <<EOF
#
# Do not edit, this is a generated file.
#
EOF
go run ipaas-template.go --name=redhat-ipaas-single-tenant --single-tenant >> redhat-ipaas-single-tenant.yml

cat > redhat-ipaas-dev-single-tenant.yml <<EOF
#
# Do not edit, this is a generated file.
#
EOF
go run ipaas-template.go --name=redhat-ipaas-dev-single-tenant --single-tenant --dev-mode >> redhat-ipaas-dev-single-tenant.yml