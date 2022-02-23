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
# Configuration vars
#
include vars/Makefile

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
	$(MAKE) $(MK_OPTIONS) -C $(MANAGER) init
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
	go get sigs.k8s.io/controller-tools/cmd/controller-gen@$(CONTROLLER_GEN_VERSION) ;\
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
	go get sigs.k8s.io/kustomize/kustomize/v4@$(KUSTOMIZE_VERSION) ;\
	rm -rf $$KUSTOMIZE_GEN_TMP_DIR ;\
	}
KUSTOMIZE=$(GOBIN)/kustomize
else
KUSTOMIZE=$(shell which kustomize)
endif

opm:
ifeq (, $(shell which opm))
	@{ \
	set -e ;\
	OPM_GEN_TMP_DIR=$$(mktemp -d) ;\
	cd $$OPM_GEN_TMP_DIR ;\
	go mod init tmp ;\
	go get \
		-ldflags '-w -extldflags "-static"' -tags "json1" \
		-ldflags "-X '$(OPM_PKG)/cmd/opm/version.opmVersion=$(OPM_VERSION)' -X '$(OPM_PKG)/cmd/opm/version.buildDate=$(BUILD_TIME)'" \
		$(OPM_PKG)/cmd/opm@$(OPM_VERSION) ;\
	rm -rf $$OPM_GEN_TMP_DIR ;\
	}
OPM=$(GOBIN)/opm
else
OPM=$(shell which opm)
endif

operator-sdk:
ifeq (, $(shell which operator-sdk))
	@{ \
	set -e ;\
	curl \
		-L https://github.com/operator-framework/operator-sdk/releases/download/$(OPERATOR_SDK_VERSION)/operator-sdk_linux_amd64 \
		-o operator-sdk ;\
	chmod +x operator-sdk ;\
	mv operator-sdk $(GOBIN)/ ;\
	}
OPERATOR_SDK=$(GOBIN)/operator-sdk
else
OPERATOR_SDK=$(shell which operator-sdk)
endif

.PHONY: kustomize opm operator-sdk bundle manifests generate generate-deepcopy generate-crds

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

# Tailor the manifest according to default values for this project
# Note. to make the bundle this name must match that specified in PROJECT
#
pre-bundle:
# bundle name must match that which appears in PROJECT file
	@sed -i 's/projectName: .*/projectName: $(PACKAGE)/' PROJECT
# finds the single CSV file and renames it
	@find $(MANIFESTS)/bases -type f -name "*.clusterserviceversion.yaml" -execdir mv '{}' $(CSV_FILENAME) ';'
	@sed -i 's~^    containerImage: .*~    containerImage: $(IMAGE):$(TAG)~' $(CSV_PATH)
	@sed -i 's/^    support: .*/    support: $(CSV_SUPPORT)/' $(CSV_PATH)
	@sed -i 's/^  name: .*.\(v.*\)/  name: $(PACKAGE).v$(VERSION)/' $(CSV_PATH)
	@sed -i 's/^      displayName: .*/      displayName: $(CSV_DISPLAY_NAME)/' $(CSV_PATH)
# Updates the icon in the csv
	@sed -i 's/^  - base64data:.*/  - base64data: $(CSV_ICON)/' $(CSV_PATH)
	@sed -i 's/^  replaces: .*/  replaces: $(CSV_REPLACES)/' $(CSV_PATH)
	@sed -i 's/^  version: .*/  version: $(VERSION)/' $(CSV_PATH)

# Generate bundle manifests and metadata, then validate generated files.
bundle: generate-crds operator-sdk kustomize pre-bundle
	@$(MAKE) $(MK_OPTIONS) -C $(ROLE) init
	@$(MAKE) $(MK_OPTIONS) -C $(GRANT) init
	@$(MAKE) $(MK_OPTIONS) -C $(MANAGER) init
# (Re)-generate the manifests directory
	@$(OPERATOR_SDK) generate kustomize manifests \
		--apis-dir $(PKG)/apis \
		--input-dir $(MANIFESTS) \
		--output-dir $(MANIFESTS)
# Sets the operator image to the preferred image:tag
	@cd $(MANIFESTS) && $(KUSTOMIZE) edit set image $(DEFAULT_IMAGE)=$(IMAGE):$(TAG)
# Generates the bundle complete with manifests
	@$(KUSTOMIZE) build $(KOPTIONS) $(MANIFESTS) | \
		$(OPERATOR_SDK) generate bundle \
			-q --overwrite --version $(VERSION) \
			--kustomize-dir $(MANIFESTS) $(BUNDLE_METADATA_OPTS)
# Remove unserved CRDs from owned section
	@sed -i '/owned:/,/Schema/{/owned:/! {/Schema/! d } }' $(BUNDLE_DIR)/$(MANIFESTS)/$(CSV_FILENAME)
# Add a timestamp to the new bundle manifest
	@sed -i '/^    containerImage: .*/ a \ \ \ \ createdAt: $(TIMESTAMP)' $(BUNDLE_DIR)/$(MANIFESTS)/$(CSV_FILENAME)
# Removes any clusterrolebindings necessary for clusterroles to be added but not required for the manifests
	@rm -f $(BUNDLE_DIR)/$(MANIFESTS)/*clusterrolebinding.yaml
# Workaround for https://bugzilla.redhat.com/show_bug.cgi?id=1934080
	@rm -f $(BUNDLE_DIR)/$(MANIFESTS)/*serviceaccount.yaml
# Workaround for extra cluster roles required to be present
# The operator looks to install ClusterRoleBindings to these cluster roles
# Without them, the operator installation does not finish
	@cp -rf $(ROLE)/$(CLUSTER_ROLE_KAFKA).gen.yaml $(BUNDLE_DIR)/$(MANIFESTS)/$(CLUSTER_ROLE_KAFKA).yaml
	@cp -rf $(ROLE)/$(CLUSTER_ROLE_PUBLIC_API).gen.yaml $(BUNDLE_DIR)/$(MANIFESTS)/$(CLUSTER_ROLE_PUBLIC_API).yaml
# Moves the docker file into the bundle directory
	@mv bundle.Dockerfile $(BUNDLE_DIR)/Dockerfile && \
		sed -i 's/bundle\///g' $(BUNDLE_DIR)/Dockerfile
	@$(OPERATOR_SDK) bundle validate $(BUNDLE_DIR)

# Build the bundle image.
.PHONY: bundle-build
bundle-build: bundle
	cd bundle && docker build -f Dockerfile -t $(BUNDLE_IMG) .

.PHONY: bundle-push
bundle-push: bundle-build
	docker push $(BUNDLE_IMG)

.PHONY: test-catalog
test-catalog: opm bundle-push
	SRC_CATALOG=$(SRC_CATALOG) CATALOG_DIR=$(CATALOG) PACKAGE=$(PACKAGE) \
	OPM=$(OPM) BUNDLE_IMAGE=$(BUNDLE_IMG) CSV_NAME=$(CSV_NAME) \
	CSV_REPLACES=$(CSV_REPLACES) CHANNEL=$(DEFAULT_CHANNEL)-$(VERSION) ./script/build-catalog.sh

clean:
	@find . -name "*.gen.*" -delete
ifneq ($(CSV_PATH), $(DEFAULT_CSV))
ifneq ("$(wildcard $(CSV_PATH))","")
	@mv $(CSV_PATH)
endif
endif
	@rm -rf bundle
