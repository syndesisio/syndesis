#
# Make Options
#
MK_OPTIONS := -s

#
# Vars that can be overridden by external env vars
#
DEBUG ?= false
DB_IMAGE ?= centos/postgresql-10-centos7
IMAGE ?= quay.io/syndesis/syndesis-operator
TAG ?= 1.12
NAMESPACE ?= syndesis
KUBE_USER ?= developer

.PHONY: kubectl setup setup-jaeger operator app dev-operator dev-app

kubectl:
ifeq (, $(shell which kubectl))
$(error "No kubectl found in PATH. Please install and re-run")
endif

#
# Setup the installation by installing crds, roles and granting
# privileges for the installing user.
#
setup: kubectl
	$(MAKE) $(MK_OPTIONS) -C bases/crd init
	$(MAKE) $(MK_OPTIONS) -C bases/role init
	$(MAKE) $(MK_OPTIONS) -C bases/grant init
	#@ Must be invoked by a user with cluster-admin privileges
ifeq ($(DEBUG), false)
	kubectl apply -k setup
else
	kubectl kustomize setup
endif

#
# Alternative to setup that in addition will install the jaeger crd.
# This step is only applicable for environments that do not run and OLM
# OperatorHub.
#
setup-jaeger: kubectl
	$(MAKE) $(MK_OPTIONS) -C bases/crd init
	$(MAKE) $(MK_OPTIONS) -C bases/jaeger-crd init
	$(MAKE) $(MK_OPTIONS) -C bases/role init
	$(MAKE) $(MK_OPTIONS) -C bases/grant init
	#@ *************************** Warning ******************************
	#@ ** This should only be invoked on a non-OperatorHub environment **
	#@ ******************************************************************
	#@ Must be invoked by a user with cluster-admin privileges
ifeq ($(DEBUG), false)
	kubectl apply -k setup-jaeger
else
	kubectl kustomize setup-jaeger
endif

#
# Install the operator deployment and related resources
#
operator: kubectl
	$(MAKE) $(MK_OPTIONS) -C bases/deployment init
	#@ Must be invoked by <user> previously granted permissions using `KUBE_USER=<user> make setup`
ifeq ($(DEBUG), false)
	kubectl apply -k operator
else
	kubectl kustomize operator
endif

#
# Installs the operator deployment and in addition installs a default CR
#
app: kubectl
	$(MAKE) $(MK_OPTIONS) -C bases/deployment init
	$(MAKE) $(MK_OPTIONS) -C app init
	#@ Must be invoked by <user> previously granted permissions using `KUBE_USER=<user> make setup`
ifeq ($(DEBUG), false)
	kubectl apply -k app
else
	kubectl kustomize app
endif

#
#################### Commands for installing in development mode ###################
#

#
# Installs the operator with DevSupport turned on
# - Patches the imagestream to use a locally built image
#   rather than a downloaded docker image
#
dev-operator: kubectl
	$(MAKE) $(MK_OPTIONS) -C bases/deployment init
	$(MAKE) $(MK_OPTIONS) -C dev/operator init
	#@ Used for development purposes only - allows for a locally compiled operator
	#@ Must be invoked by <user> previously granted permissions using `KUBE_USER=<user> make setup`
ifeq ($(DEBUG), false)
	kubectl apply -k dev/operator
else
	kubectl kustomize dev/operator
endif

#
# Installs the operator deployment in development mode
# and in addition installs a default CR
#
dev-app: kubectl
	$(MAKE) $(MK_OPTIONS) -C bases/deployment init
	$(MAKE) $(MK_OPTIONS) -C dev/operator init
	$(MAKE) $(MK_OPTIONS) -C dev/app init
	#@ Must be invoked by <user> previously granted permissions using `KUBE_USER=<user> make setup`
ifeq ($(DEBUG), false)
	kubectl apply -k dev/app
else
	kubectl kustomize dev/app
endif
