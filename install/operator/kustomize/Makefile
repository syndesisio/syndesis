
#
# Paths
#
ROOT := ..
ASSETS := $(ROOT)/pkg/generator/assets

# Resources that provide foundational base
BASES := ./bases
CRD := $(BASES)/crd/syndesis-crd.yml
DEPLOYMENT := $(BASES)/deployment/deployment.yml
GRANT := $(BASES)/grant/grant.yml
GRANT_JAEGER := $(BASES)/grant/grant-jaeger.yml
GRANT_KAFKA := $(BASES)/grant/grant-kafka.yml
GRANT_OLM := $(BASES)/grant/grant-olm.yml
GRANT_PUBLIC_API := $(BASES)/grant/grant-public-api.yml
ROLE := $(BASES)/role/role.yml
ROLE_JAEGER := $(BASES)/role/cluster-role-jaeger.yml
ROLE_KAFKA := $(BASES)/role/cluster-role-kafka.yml
ROLE_PUBLIC_API := $(BASES)/role/cluster-role-public-api.yml
ROLE_OLM := $(BASES)/role/cluster-role-olm.yml
OPERATOR_INSTALL := $(BASES)/deployment/operator-install.yml

# Resources that overlay the bases
ADDONS := ./addons
CR := ./app/syndesis-cr.yml
JAEGER_CRD :=  ./setup-jaeger/jaeger-crd.yml

DEV := ./dev
PATCH_ANNOTATION_SRC := $(DEV)/operator/annotation-patch.yaml
PATCH_ANNOTATION_TGT := $(DEV)/operator/annotation-patch.yml
PATCH_ENV_VAR_SRC := $(DEV)/operator/env-var-patch.yaml
PATCH_ENV_VAR_TGT := $(DEV)/operator/env-var-patch.yml
PATCH_IMG_STREAM_SRC := $(DEV)/operator/image-stream-patch.yaml
PATCH_IMG_STREAM_TGT := $(DEV)/operator/image-stream-patch.yml

#
# Vars
#
DB_IMAGE := centos/postgresql-10-centos7
DEV_IMAGE := syndesis-operator
ROLENAME := syndesis-operator

#
# Vars that can be overridden by external env vars
#
IMAGE ?= docker.io/syndesis/syndesis-operator
NAMESPACE ?= syndesis
KUBE_USER ?= developer
TAG ?= latest

.PHONY: kustomize sync-crd sync-jaeger-crd sync-role sync-grant sync-operator-install sync-deployment sync-cr setup setup-jaeger operator app

kubectl:
ifeq (, $(shell which kubectl))
$(error "No kubectl found in PATH. Please install and re-run")
endif

#
# Copy the go template from the src directory
#
sync-crd:
	cp $(ASSETS)/install/cluster/syndesis.yml $(CRD)

#
# Copy the go template from the src directory
# Convert the go template to a formatted yaml file:
# - Delete any line beginning with '{{'
# - Delete any empty lines and comments
#
sync-jaeger-crd: sync-crd
	cp $(ASSETS)/install/cluster/jaeger.yml.tmpl $(JAEGER_CRD)
	sed -i '/^{{\|^#\|^$$/d' $(JAEGER_CRD)

#
# Copy the go template from the src directory
# Convert the go template to a formatted yaml file:
# - Convert go-style array to yaml format, ie. remove '-' & double space
# - Replace Kind & Role placeholders
#
sync-role:
	cp $(ASSETS)/install/role.yml.tmpl $(ROLE)
	sed -i 's/{{.Kind}}/Role/' $(ROLE)
	sed -i 's/{{.Role}}/$(ROLENAME)/' $(ROLE)
	sed -i 's/- kind:/kind:/' $(ROLE)
	sed -i 's/^  //' $(ROLE)
	cp $(ASSETS)/install/cluster_role_jaeger.yml.tmpl $(ROLE_JAEGER)
	sed -i '/^{{\|^#\|^$$/d' $(ROLE_JAEGER)
	cp $(ASSETS)/install/cluster_role_kafka.yml.tmpl $(ROLE_KAFKA)
	cp $(ASSETS)/install/cluster_role_olm.yml.tmpl $(ROLE_OLM)
	sed -i '/^{{\|^$$/d' $(ROLE_OLM)
	sed -i 's/^- /\n---\n/' $(ROLE_OLM)
	sed -i 's/^  //' $(ROLE_OLM)
	sed -i '/{{- if\|{{- end/d' $(ROLE_OLM)
	cp $(ASSETS)/install/cluster_role_public_api.yml.tmpl $(ROLE_PUBLIC_API)

#
# Copy the go template from the src directory
# Convert the go template to a formatted yaml file:
# - Replace Kind,  Role, User, Namespace placeholders
# - Delete any if & end codeword lines
#
sync-grant:
	cp $(ASSETS)/install/grant/grant_role.yml.tmpl $(GRANT)
	sed -i 's/{{ .Kind }}/Role/' $(GRANT)
	sed -i 's/{{ .Role }}/$(ROLENAME)/' $(GRANT)
	sed -i 's/{{ .User }}/$(KUBE_USER)/' $(GRANT)
	sed -i 's/{{ .Namespace }}/$(NAMESPACE)/' $(GRANT)
	sed -i '/{{- if\|{{- end/d' $(GRANT)
	cp $(ASSETS)/install/grant/grant_cluster_role_jaeger.yml.tmpl $(GRANT_JAEGER)
	sed -i '/^{{\|^$$/d' $(GRANT_JAEGER)
	sed -i 's/^- /\n---\n/' $(GRANT_JAEGER)
	sed -i 's/^  //' $(GRANT_JAEGER)
	sed -i 's/{{ .Namespace }}/$(NAMESPACE)/' $(GRANT_JAEGER)
	sed -i 's/{{ .User }}/$(KUBE_USER)/' $(GRANT_JAEGER)
	cp $(ASSETS)/install/grant/grant_cluster_role_kafka.yml.tmpl $(GRANT_KAFKA)
	sed -i 's/{{ .Namespace }}/$(NAMESPACE)/' $(GRANT_KAFKA)
	cp $(ASSETS)/install/grant/grant_cluster_role_olm.yml.tmpl $(GRANT_OLM)
	sed -i '/^{{\|^$$/d' $(GRANT_OLM)
	sed -i 's/^- /\n---\n/' $(GRANT_OLM)
	sed -i 's/^  //' $(GRANT_OLM)
	sed -i 's/{{ .Role }}/$(ROLENAME)/' $(GRANT_OLM)
	sed -i 's/{{ .User }}/$(KUBE_USER)/' $(GRANT_OLM)
	cp $(ASSETS)/install/grant/grant_cluster_role_public_api.yml.tmpl $(GRANT_PUBLIC_API)
	sed -i 's/{{.Namespace}}/$(NAMESPACE)/' $(GRANT_PUBLIC_API)

#
# Copy the go template from the src directory
# Convert the go template to a formatted yaml file:
# - Delete any line beginning with '{{'
# - Delete any empty lines
# - Convert go-style array '- xxx' to yaml format: '---\n' & remove double-spaces at start of lines
# - Replace Role, Namespace, Tag, Image placeholders
# - Delete DevSupport conditional, ImageStreamTag line, else and end codewords
#
sync-operator-install:
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

#
# Copy the go template from the src directory
# Convert the go template to a formatted yaml file:
# - Remove any if/else/end conditional sections (not required)
# - Replace Tag, Image placeholders
#
sync-deployment:
	cp $(ASSETS)/install/operator_deployment.yml.tmpl $(DEPLOYMENT)
	sed -i '/{{- if/,/{{- end}}\|{{end}}/d' $(DEPLOYMENT)
	sed -i 's/{{.Tag}}/$(TAG)/' $(DEPLOYMENT)
	sed -i 's_{{.Image}}_$(IMAGE)_' $(DEPLOYMENT)
	sed -i 's_{{.DatabaseImage}}_$(DB_IMAGE)_' $(DEPLOYMENT)

#
# Copy the go template from the src directory
# Convert the go template to a formatted yaml file:
# - Remove any range/end sections (not required)
#
sync-cr:
	cp $(ASSETS)/install/app.yml.tmpl $(CR)
	sed -i '/{{- range/,/{{- end }}\|{{end}}/d' $(CR)

#
# Injects the DEV_IMAGE & TAG values into the patches
#
sync-operator-dev:
	cp $(PATCH_ANNOTATION_SRC) $(PATCH_ANNOTATION_TGT)
	sed -i 's/{{.Tag}}/$(TAG)/' $(PATCH_ANNOTATION_TGT)
	cp $(PATCH_ENV_VAR_SRC) $(PATCH_ENV_VAR_TGT)
	cp $(PATCH_IMG_STREAM_SRC) $(PATCH_IMG_STREAM_TGT)
	sed -i 's_{{.Image}}_$(DEV_IMAGE)_' $(PATCH_IMG_STREAM_TGT)
	sed -i 's/{{.Tag}}/$(TAG)/' $(PATCH_IMG_STREAM_TGT)

#
# Setup the installation by installing crds, roles and granting
# privileges for the installing user.
#
setup: kubectl sync-crd sync-role sync-grant
	#@ Must be invoked by a user with cluster-admin privileges
	kubectl apply -k setup

#
# Alternative to setup that in addition will install the jaeger crd.
# This step is only applicable for environments that do not run and OLM
# OperatorHub.
#
setup-jaeger: kubectl sync-jaeger-crd sync-role sync-grant
	#@ *************************** Warning ******************************
	#@ ** This should only be invoked on a non-OperatorHub environment **
	#@ ******************************************************************
	#@ Must be invoked by a user with cluster-admin privileges
	kubectl apply -k setup-jaeger

#
# Install the operator deployment and related resources
#
operator: kubectl sync-operator-install sync-deployment
	#@ Must be invoked by a user previously granted permissions using `KUBE_USER=<user> make install-setup`
	kubectl apply -k operator

#
# Installs the operator deployment and in addition installs a default CR
#
app: kubectl sync-cr sync-operator-install sync-deployment
	#@ Must be invoked by a user previously granted permissions using `KUBE_USER=<user> make install-setup`
	kubectl apply -k app

#
#################### Commands for installing in development mode ###################
#

#
# Installs the operator with DevSupport turned on
# - Patches the imagestream to use a locally built image
#   rather than a downloaded docker image
#
operator-dev: kubectl sync-operator-install sync-deployment sync-operator-dev
	#@ Used for development purposes only - allows for a locally compiled operator
	#@ Must be invoked by a user previously granted permissions using `KUBE_USER=<user> make install-setup`
	kubectl apply -k dev/operator

#
# Installs the operator deployment in development mode
# and in addition installs a default CR
#
app-dev: kubectl sync-cr sync-operator-install sync-deployment sync-operator-dev
		#@ Must be invoked by a user previously granted permissions using `KUBE_USER=<user> make install-setup`
		kubectl apply -k dev/app

#
# Installs the product operator using kustomize to override
# the images in the deployment and imagestream.
# First checks for a syndesis-pull-secret for accessing the
# Red Hat registry. If not present then it request credentials
# and creates accordingly.
#
product: kubectl sync-operator-install sync-deployment
	#@ Credentials required for accessing the redhat registry
	kubectl get secret syndesis-pull-secret &> /dev/null || (\
	 read -p "enter username for registry.redhat.io and press [ENTER]: " username; \
	 read -p "enter password for registry.redhat.io and press [ENTER]: " password; \
	 read -p "enter email address for registry.redhat.io and press [ENTER]: " email; \
	 kubectl create secret docker-registry syndesis-pull-secret \
	   --docker-server=registry.redhat.io \
		 --docker-username=$$username --docker-password=$$password --docker-email=$$email)
	kubectl apply -k product
