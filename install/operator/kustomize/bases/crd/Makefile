ASSETS := ../../../pkg/generator/assets
TMPL := tmpl
YAML := yaml

CRD := ./syndesis-crd.gen

.PHONY: sync init

# start-sync
#
# Copy the go template from the src directory
#
sync:
	cp $(ASSETS)/install/cluster/syndesis.yml $(CRD).$(TMPL)
# end-sync

init: sync
	cp $(CRD).$(TMPL) $(CRD).$(YAML)
