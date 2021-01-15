DEV_IMAGE ?= syndesis-operator
TAG ?= latest

PATCH_ANNOTATION_SRC := ./annotation-patch.yaml
PATCH_ANNOTATION_TGT := ./annotation-patch.yml
PATCH_ENV_VAR_SRC := ./env-var-patch.yaml
PATCH_ENV_VAR_TGT := ./env-var-patch.yml
PATCH_IMG_STREAM_SRC := ./image-stream-patch.yaml
PATCH_IMG_STREAM_TGT := ./image-stream-patch.yml

.PHONY: sync

#
# Injects the DEV_IMAGE & TAG values into the patches
#
sync:
	cp $(PATCH_ANNOTATION_SRC) $(PATCH_ANNOTATION_TGT)
	sed -i 's/{{.Tag}}/$(TAG)/' $(PATCH_ANNOTATION_TGT)
	cp $(PATCH_ENV_VAR_SRC) $(PATCH_ENV_VAR_TGT)
	cp $(PATCH_IMG_STREAM_SRC) $(PATCH_IMG_STREAM_TGT)
	sed -i 's_{{.Image}}_$(DEV_IMAGE)_' $(PATCH_IMG_STREAM_TGT)
	sed -i 's/{{.Tag}}/$(TAG)/' $(PATCH_IMG_STREAM_TGT)
