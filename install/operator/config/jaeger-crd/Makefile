ASSETS := ../../pkg/generator/assets
TMPL := tmpl
YAML := yaml
NAMESPACE_VAR := {NAMESPACE}
USER_VAR := {KUBE_USER}

JAEGER_CRD := ./jaeger-crd.gen

.PHONY: sync init

# start-sync
#
# Copy the go template from the src directory
# Convert the go template to a formatted yaml file:
# - Delete any line beginning with '{{'
# - Delete any empty lines and comments
#
sync:
	cp $(ASSETS)/install/cluster/jaeger.yml.tmpl $(JAEGER_CRD).$(TMPL)
	sed -i '/^{{\|^#\|^$$/d' $(JAEGER_CRD).$(TMPL)
# end-sync

init: sync
	cp $(JAEGER_CRD).$(TMPL) $(JAEGER_CRD).$(YAML)
