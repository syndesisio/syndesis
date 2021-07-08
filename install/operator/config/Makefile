#
# Make Options
#
MK_OPTIONS := -s

#
# Allows for resources to be loaded from outside the root location of
# the kustomize config file. Ensures that resource don't need to be
# copied around the file system.
#
# See https://kubectl.docs.kubernetes.io/faq/kustomize
#
KOPTIONS := --load-restrictor LoadRestrictionsNone

#
# Vars that can be overridden by external env vars
#
VERSION ?= 1.12.0
DEBUG ?= false
DB_IMAGE ?= centos/postgresql-10-centos7
DEFAULT_IMAGE ?= quay.io/syndesis/syndesis-operator
IMAGE ?= $(DEFAULT_IMAGE)
TAG ?= $(VERSION:.0=)
NAMESPACE ?= syndesis
KUBE_USER ?= developer
LEGACY ?= false

CHANNELS ?= $(VERSION:.0=.x)
DEFAULT_CHANNEL ?= $(VERSION:.0=.x)
PACKAGE := syndesis
BUNDLE_DIR := bundle
PKG := ../pkg
CRD := crd/bases
MANAGER := manager
MANIFESTS := manifests
GRANT := rbac-grant
ROLE := rbac-role

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
	$(MAKE) $(MK_OPTIONS) -C $(ROLE) init
	$(MAKE) $(MK_OPTIONS) -C $(GRANT) init
ifeq ($(LEGACY), true)
	@cd setup && \
		$(KUSTOMIZE) edit remove resource ../crd/bases && \
		$(KUSTOMIZE) edit add resource ../crd/deprecated
else
	@cd setup && \
		$(KUSTOMIZE) edit remove resource ../crd/deprecated && \
		$(KUSTOMIZE) edit add resource ../crd/bases
endif
	#@ Must be invoked by a user with cluster-admin privileges
ifeq ($(DEBUG), false)
	@$(KUSTOMIZE) build $(KOPTIONS) setup | kubectl apply -f -
else
	@$(KUSTOMIZE) build $(KOPTIONS) setup
endif

#
# Alternative to setup that in addition will install the jaeger crd.
# This step is only applicable for environments that do not run and OLM
# OperatorHub.
#
setup-jaeger: kubectl
	$(MAKE) $(MK_OPTIONS) -C jaeger-crd init
	$(MAKE) $(MK_OPTIONS) -C $(ROLE) init
	$(MAKE) $(MK_OPTIONS) -C $(GRANT) init
ifeq ($(LEGACY), true)
	@cd setup-jaeger && \
		$(KUSTOMIZE) edit remove resource ../jaeger-crd && \
		$(KUSTOMIZE) edit add resource ../jaeger-crd/deprecated
else
	@cd setup-jaeger && \
		$(KUSTOMIZE) edit remove resource ../jaeger-crd/deprecated && \
		$(KUSTOMIZE) edit add resource ../jaeger-crd
endif
	#@ *************************** Warning ******************************
	#@ ** This should only be invoked on a non-OperatorHub environment **
	#@ ******************************************************************
	#@ Must be invoked by a user with cluster-admin privileges
ifeq ($(DEBUG), false)
	@$(KUSTOMIZE) build $(KOPTIONS) setup-jaeger | kubectl apply -f -
else
	@$(KUSTOMIZE) build $(KOPTIONS) setup-jaeger
endif

#
# Install the operator deployment and related resources
#
operator: kubectl
	$(MAKE) $(MK_OPTIONS) -C $(MANAGER) init
	#@ Must be invoked by <user> previously granted permissions using `KUBE_USER=<user> make setup`
ifeq ($(DEBUG), false)
	@$(KUSTOMIZE) build $(KOPTIONS) operator | kubectl apply -f -
else
	@$(KUSTOMIZE) build $(KOPTIONS) operator
endif

#
# Installs the operator deployment and in addition installs a default CR
#
app: kubectl
	$(MAKE) $(MK_OPTIONS) -C $(MANAGER) init
	$(MAKE) $(MK_OPTIONS) -C app init
	#@ Must be invoked by <user> previously granted permissions using `KUBE_USER=<user> make setup`
ifeq ($(DEBUG), false)
	@$(KUSTOMIZE) build $(KOPTIONS) app | kubectl apply -f -
else
	@$(KUSTOMIZE) build $(KOPTIONS) app
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
	$(MAKE) $(MK_OPTIONS) -C $(MANGER) init
	$(MAKE) $(MK_OPTIONS) -C dev/operator init
	#@ Used for development purposes only - allows for a locally compiled operator
	#@ Must be invoked by <user> previously granted permissions using `KUBE_USER=<user> make setup`
ifeq ($(DEBUG), false)
	@$(KUSTOMIZE) build $(KOPTIONS) dev/operator | kubectl apply -f -
else
	@$(KUSTOMIZE) build $(KOPTIONS) dev/operator
endif

#
# Installs the operator deployment in development mode
# and in addition installs a default CR
#
dev-app: kubectl
	$(MAKE) $(MK_OPTIONS) -C $(MANAGER) init
	$(MAKE) $(MK_OPTIONS) -C dev/operator init
	$(MAKE) $(MK_OPTIONS) -C dev/app init
	#@ Must be invoked by <user> previously granted permissions using `KUBE_USER=<user> make setup`
ifeq ($(DEBUG), false)
	@$(KUSTOMIZE) build $(KOPTIONS) dev/app | kubectl apply -f -
else
	@$(KUSTOMIZE) build $(KOPTIONS) dev/app
endif

#
##################################
# B U N D L E  G E N E R A T I O N
##################################
#
# Default bundle image tag
BUNDLE_IMG ?= $(IMAGE)-bundle:$(VERSION)

# Options for 'bundle-build'
ifneq ($(origin CHANNELS), undefined)
BUNDLE_CHANNELS := --channels=$(CHANNELS)
endif
ifneq ($(origin DEFAULT_CHANNEL), undefined)
BUNDLE_DEFAULT_CHANNEL := --default-channel=$(DEFAULT_CHANNEL)
endif
ifneq ($(origin PACKAGE), undefined)
BUNDLE_PACKAGE := --package=$(PACKAGE)
endif
BUNDLE_METADATA_OPTS ?= $(BUNDLE_CHANNELS) $(BUNDLE_DEFAULT_CHANNEL) $(BUNDLE_PACKAGE)

# Produce CRDs that work back to Kubernetes 1.11 (no version conversion)
CRD_OPTIONS ?= "crd:trivialVersions=true"

# Get the currently used golang install path (in GOPATH/bin, unless GOBIN is set)
ifeq (,$(shell go env GOBIN))
GOBIN=$(shell go env GOPATH)/bin
else
GOBIN=$(shell go env GOBIN)
endif

# find or download controller-gen
# download controller-gen if necessary
controller-gen:
ifeq (, $(shell which controller-gen))
	@{ \
	set -e ;\
	CONTROLLER_GEN_TMP_DIR=$$(mktemp -d) ;\
	cd $$CONTROLLER_GEN_TMP_DIR ;\
	go mod init tmp ;\
	go get sigs.k8s.io/controller-tools/cmd/controller-gen@v0.3.0 ;\
	rm -rf $$CONTROLLER_GEN_TMP_DIR ;\
	}
CONTROLLER_GEN=$(GOBIN)/controller-gen
else
CONTROLLER_GEN=$(shell which controller-gen)
endif

kustomize:
ifeq (, $(shell which kustomize))
	@{ \
	set -e ;\
	KUSTOMIZE_GEN_TMP_DIR=$$(mktemp -d) ;\
	cd $$KUSTOMIZE_GEN_TMP_DIR ;\
	go mod init tmp ;\
	go get sigs.k8s.io/kustomize/kustomize/v4@v4.0.5 ;\
	rm -rf $$KUSTOMIZE_GEN_TMP_DIR ;\
	}
KUSTOMIZE=$(GOBIN)/kustomize
else
KUSTOMIZE=$(shell which kustomize)
endif

.PHONY: bundle manifests generate generate-deepcopy generate-crds

generate-deepcopy: controller-gen
	@$(CONTROLLER_GEN) paths="$(PKG)/..." object

generate-crds: controller-gen
	@$(CONTROLLER_GEN) crd crd:crdVersions=v1 \
		paths=$(PKG)/apis/... \
		output:artifacts:config=$(CRD) \
		output:crd:dir=$(CRD)
	@sed -i '/creationTimestamp:/a\  labels:\n    app: syndesis' \
		$(CRD)/syndesis.io_syndesises.yaml
	@sed -i '/creationTimestamp: null/d' $(CRD)/syndesis.io_syndesises.yaml
	@echo "***"
	@echo "*** Copies CRD to operator internals - Requires recompilation of operator to update resources.go ***"
	@echo "***"
	@cp $(CRD)/syndesis.io_syndesises.yaml $(PKG)/generator/assets/install/cluster/syndesis.yml

# Generate code
generate: generate-deepcopy generate-crds

CSV_ICON=$(shell cat $(PKG)/syndesis/olm/assets/icon)
CREATED=$(shell date -u +%FT%TZ)

# Generate bundle manifests and metadata, then validate generated files.
bundle: generate-crds kustomize
	@$(MAKE) $(MK_OPTIONS) -C $(ROLE) init
	@$(MAKE) $(MK_OPTIONS) -C $(GRANT) init
	@$(MAKE) $(MK_OPTIONS) -C $(MANAGER) init
# (Re)-generate the manifests directory
	@operator-sdk generate kustomize manifests \
		--apis-dir $(PKG)/apis \
		--input-dir $(MANIFESTS) \
		--output-dir $(MANIFESTS)
# Updates the container-image attribute
	@sed -i 's~containerImage:.*~containerImage: $(IMAGE):$(TAG)~' $(MANIFESTS)/bases/*.clusterserviceversion.yaml
# Updates the icon in the csv
	@sed -i 's/base64data:.*/base64data: $(CSV_ICON)/' $(MANIFESTS)/bases/*.clusterserviceversion.yaml
# Sets the operator image to the preferred image:tag
	@cd $(MANIFESTS) && $(KUSTOMIZE) edit set image $(DEFAULT_IMAGE)=$(IMAGE):$(TAG)
# Generates the bundle complete with manifests
	@$(KUSTOMIZE) build $(KOPTIONS) $(MANIFESTS) | \
		operator-sdk generate bundle \
			-q --overwrite --version $(VERSION) \
			--kustomize-dir $(MANIFESTS) $(BUNDLE_METADATA_OPTS)
# Remove unserved CRDs from owned section
	@sed -i '/- kind: Syndesis/,/version: v1beta1/d' $(BUNDLE_DIR)/$(MANIFESTS)/*.clusterserviceversion.yaml
# Removes any clusterrolebindings necessary for clusterroles to be added but not required for the manifests
	@rm -f $(BUNDLE_DIR)/manifests/*clusterrolebinding.yaml
# Moves the docker file into the bundle directory
	@mv bundle.Dockerfile $(BUNDLE_DIR)/Dockerfile && \
		sed -i 's/bundle\///g' $(BUNDLE_DIR)/Dockerfile
	@operator-sdk bundle validate $(BUNDLE_DIR)

# Build the bundle image.
.PHONY: bundle-build
bundle-build: bundle
	cd bundle && docker build -f Dockerfile -t $(BUNDLE_IMG) .

clean:
	find . -name "*.gen.*" -delete
	rm -rf bundle
