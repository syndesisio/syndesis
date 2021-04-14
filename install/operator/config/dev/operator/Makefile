TMPL := tmpl
YAML := yaml
GEN := gen
TAG_VAR := {TAG}

PATCH_ANNOTATION := ./annotation-patch
PATCH_IMG_STREAM := ./image-stream-patch

# User customisable variables
TAG ?= 1.12

.PHONY: init

# start-sync
sync:
	#@ Nothing required but needs to be present
# end-sync

#
# Injects the TAG values into the patches
#
init: sync
	for resource in $(PATCH_ANNOTATION) $(PATCH_IMG_STREAM); do \
		cp $${resource}.$(TMPL) $${resource}.$(GEN).$(YAML); \
		sed -i 's/$(TAG_VAR)/$(TAG)/' $${resource}.$(GEN).$(YAML); \
	done
