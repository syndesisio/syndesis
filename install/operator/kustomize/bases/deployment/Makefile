ASSETS := ../../../pkg/generator/assets
TMPL := tmpl
YAML := yaml
NAMESPACE_VAR := {NAMESPACE}
USER_VAR := {KUBE_USER}
TAG_VAR := {TAG}
IMAGE_VAR := {IMAGE}
DB_IMAGE_VAR := {DB_IMAGE}

ROLENAME := syndesis-operator
DEPLOYMENT := ./deployment.gen
OPERATOR_INSTALL := ./operator-install.gen

# User customisable variables
DB_IMAGE ?= centos/postgresql-10-centos7
IMAGE ?= quay.io/syndesis/syndesis-operator
NAMESPACE ?= syndesis
TAG ?= 1.12

.PHONY: sync init

# start-sync
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
	cp $(ASSETS)/install/operator_install.yml.tmpl $(OPERATOR_INSTALL).$(TMPL)
	sed -i 's/{{ .Role }}/$(ROLENAME)/' $(OPERATOR_INSTALL).$(TMPL)
	sed -i 's/{{.Namespace}}/$(NAMESPACE_VAR)/' $(OPERATOR_INSTALL).$(TMPL)
	sed -i 's/{{.Tag}}/$(TAG_VAR)/' $(OPERATOR_INSTALL).$(TMPL)
	sed -i 's/{{.Image}}/$(IMAGE_VAR)/' $(OPERATOR_INSTALL).$(TMPL)
	sed -i '/{{if .DevSupport}}/d' $(OPERATOR_INSTALL).$(TMPL)
	sed -i '/kind: ImageStreamTag/d' $(OPERATOR_INSTALL).$(TMPL)
	sed -i '/{{- \|{{else}}\|{{end}}\|^{{\|^$$/d' $(OPERATOR_INSTALL).$(TMPL)
	sed -i 's/^- /\n---\n/' $(OPERATOR_INSTALL).$(TMPL)
	sed -i 's/^  //' $(OPERATOR_INSTALL).$(TMPL)
	cp $(ASSETS)/install/operator_deployment.yml.tmpl $(DEPLOYMENT).$(TMPL)
	sed -i '/{{- if/,/{{- end}}\|{{end}}/d' $(DEPLOYMENT).$(TMPL)
	sed -i 's/{{.Tag}}/$(TAG_VAR)/' $(DEPLOYMENT).$(TMPL)
	sed -i 's/{{.Image}}/$(IMAGE_VAR)/' $(DEPLOYMENT).$(TMPL)
	sed -i 's/{{.DatabaseImage}}/$(DB_IMAGE_VAR)/' $(DEPLOYMENT).$(TMPL)
	sed -i '/{{- \|{{else}}\|{{end}}\|^$$/d' $(DEPLOYMENT).$(TMPL)
# end-sync

init: sync
	for resource in $(OPERATOR_INSTALL) $(DEPLOYMENT); do \
		cp $${resource}.$(TMPL) $${resource}.$(YAML); \
		sed -i 's/$(NAMESPACE_VAR)/$(NAMESPACE)/' $${resource}.$(YAML); \
		sed -i 's/$(TAG_VAR)/$(TAG)/' $${resource}.$(YAML); \
		sed -i 's~$(IMAGE_VAR)~$(IMAGE)~' $${resource}.$(YAML); \
		sed -i 's~$(DB_IMAGE_VAR)~$(DB_IMAGE)~' $${resource}.$(YAML); \
	done
