#
# Make Options
#
MK_OPTIONS := -s

#
# Vars
#
DEV_IMAGE := syndesis-operator
ROLENAME := syndesis-operator

#
# Vars that can be overridden by external env vars
#
DB_IMAGE ?= centos/postgresql-10-centos7
IMAGE ?= docker.io/syndesis/syndesis-operator
NAMESPACE ?= syndesis
KUBE_USER ?= developer
TAG ?= latest

.PHONY: kustomize setup setup-jaeger operator app product

kubectl:
ifeq (, $(shell which kubectl))
$(error "No kubectl found in PATH. Please install and re-run")
endif

#
# Setup the installation by installing crds, roles and granting
# privileges for the installing user.
#
setup: kubectl
	$(MAKE) $(MK_OPTIONS) -C bases/crd
	$(MAKE) $(MK_OPTIONS) -C bases/role
	$(MAKE) $(MK_OPTIONS) -C bases/grant
	#@ Must be invoked by a user with cluster-admin privileges
	kubectl apply -k setup

#
# Alternative to setup that in addition will install the jaeger crd.
# This step is only applicable for environments that do not run and OLM
# OperatorHub.
#
setup-jaeger: kubectl
	$(MAKE) $(MK_OPTIONS) -C bases/crd
	$(MAKE) $(MK_OPTIONS) -C bases/jaeger-crd
	$(MAKE) $(MK_OPTIONS) -C bases/role
	$(MAKE) $(MK_OPTIONS) -C bases/grant
	#@ *************************** Warning ******************************
	#@ ** This should only be invoked on a non-OperatorHub environment **
	#@ ******************************************************************
	#@ Must be invoked by a user with cluster-admin privileges
	kubectl apply -k setup-jaeger

#
# Install the operator deployment and related resources
#
operator: kubectl
	$(MAKE) $(MK_OPTIONS) -C bases/deployment
	#@ Must be invoked by a user previously granted permissions using `KUBE_USER=<user> make install-setup`
	kubectl apply -k operator

#
# Installs the operator deployment and in addition installs a default CR
#
app: kubectl
	$(MAKE) $(MK_OPTIONS) -C bases/deployment
	$(MAKE) $(MK_OPTIONS) -C app
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
dev-operator: kubectl
	$(MAKE) $(MK_OPTIONS) -C bases/deployment
	$(MAKE) $(MK_OPTIONS) -C dev/operator
	#@ Used for development purposes only - allows for a locally compiled operator
	#@ Must be invoked by a user previously granted permissions using `KUBE_USER=<user> make install-setup`
	kubectl apply -k dev/operator

#
# Installs the operator deployment in development mode
# and in addition installs a default CR
#
dev-app: kubectl
	$(MAKE) $(MK_OPTIONS) -C bases/deployment
	$(MAKE) $(MK_OPTIONS) -C dev/operator
	$(MAKE) $(MK_OPTIONS) -C dev/app
	#@ Must be invoked by a user previously granted permissions using `KUBE_USER=<user> make install-setup`
	kubectl apply -k dev/app
