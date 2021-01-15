ASSETS := ../../../pkg/generator/assets
DB_IMAGE ?= centos/postgresql-10-centos7
DEPLOYMENT := ./deployment.yml
IMAGE ?= docker.io/syndesis/syndesis-operator
NAMESPACE ?= syndesis
OPERATOR_INSTALL := ./operator-install.yml
ROLENAME ?= syndesis-operator
TAG ?= latest

.PHONY: sync

#
# Copy the go template from the src directory
# Convert the go template to a formatted yaml file:
# - Delete any line beginning with '{{'
# - Delete any empty lines
# - Convert go-style array '- xxx' to yaml format: '---\n' & remove double-spaces at start of lines
# - Replace Role, Namespace, Tag, Image placeholders
# - Delete DevSupport conditional, ImageStreamTag line, else and end codewords
#
sync:
	cp $(ASSETS)/install/operator_install.yml.tmpl $(OPERATOR_INSTALL)
	sed -i '/^{{\|^$$/d' $(OPERATOR_INSTALL)
	sed -i 's/^- /\n---\n/' $(OPERATOR_INSTALL)
	sed -i 's/^  //' $(OPERATOR_INSTALL)
	sed -i 's/{{ .Role }}/$(ROLENAME)/' $(OPERATOR_INSTALL)
	sed -i 's/{{.Namespace}}/$(NAMESPACE)/' $(OPERATOR_INSTALL)
	sed -i 's/{{.Tag}}/$(TAG)/' $(OPERATOR_INSTALL)
	sed -i 's_{{.Image}}_$(IMAGE)_' $(OPERATOR_INSTALL)
	sed -i '/{{if .DevSupport}}/d' $(OPERATOR_INSTALL)
	sed -i '/kind: ImageStreamTag/d' $(OPERATOR_INSTALL)
	sed -i '/{{else}}\|{{end}}/d' $(OPERATOR_INSTALL)
	cp $(ASSETS)/install/operator_deployment.yml.tmpl $(DEPLOYMENT)
	sed -i '/{{- if/,/{{- end}}\|{{end}}/d' $(DEPLOYMENT)
	sed -i 's/{{.Tag}}/$(TAG)/' $(DEPLOYMENT)
	sed -i 's_{{.Image}}_$(IMAGE)_' $(DEPLOYMENT)
	sed -i 's_{{.DatabaseImage}}_$(DB_IMAGE)_' $(DEPLOYMENT)
