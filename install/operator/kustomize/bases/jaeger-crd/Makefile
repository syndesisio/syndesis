ASSETS := ../../../pkg/generator/assets
JAEGER_CRD :=  ./jaeger-crd.yml

.PHONY: sync

#
# Copy the go template from the src directory
# Convert the go template to a formatted yaml file:
# - Delete any line beginning with '{{'
# - Delete any empty lines and comments
#
sync:
	cp $(ASSETS)/install/cluster/jaeger.yml.tmpl $(JAEGER_CRD)
	sed -i '/^{{\|^#\|^$$/d' $(JAEGER_CRD)
