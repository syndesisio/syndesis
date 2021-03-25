ASSETS := ../../pkg/generator/assets
TMPL := tmpl
YAML := yaml
NAMESPACE_VAR := {NAMESPACE}
USER_VAR := {KUBE_USER}

ROLENAME := syndesis-operator
ROLE := ./role.gen
ROLE_JAEGER := ./cluster-role-jaeger.gen
ROLE_KAFKA := ./cluster-role-kafka.gen
ROLE_PUBLIC_API := ./cluster-role-public-api.gen
ROLE_OLM := ./cluster-role-olm.gen

.PHONY: sync init

# start-sync
#
# Copy the go template from the src directory
# Convert the go template to a formatted yaml file:
# - Convert go-style array to yaml format, ie. remove '-' & double space
# - Replace Kind & Role placeholders
#
sync:
	cp $(ASSETS)/install/role.yml.tmpl $(ROLE).$(TMPL)
	sed -i 's/{{.Kind}}/Role/' $(ROLE).$(TMPL)
	sed -i 's/{{.Role}}/$(ROLENAME)/' $(ROLE).$(TMPL)
	sed -i 's/- kind:/kind:/' $(ROLE).$(TMPL)
	sed -i 's/^  //' $(ROLE).$(TMPL)
	cp $(ASSETS)/install/cluster_role_jaeger.yml.tmpl $(ROLE_JAEGER).$(TMPL)
	sed -i '/^{{\|^#\|^$$/d' $(ROLE_JAEGER).$(TMPL)
	cp $(ASSETS)/install/cluster_role_kafka.yml.tmpl $(ROLE_KAFKA).$(TMPL)
	cp $(ASSETS)/install/cluster_role_olm.yml.tmpl $(ROLE_OLM).$(TMPL)
	sed -i '/^{{\|^$$/d' $(ROLE_OLM).$(TMPL)
	sed -i 's/^- /\n---\n/' $(ROLE_OLM).$(TMPL)
	sed -i 's/^  //' $(ROLE_OLM).$(TMPL)
	sed -i '/{{- if\|{{- end/d' $(ROLE_OLM).$(TMPL)
	cp $(ASSETS)/install/cluster_role_public_api.yml.tmpl $(ROLE_PUBLIC_API).$(TMPL)
# end-sync

init: sync
	for resource in $(ROLE) $(ROLE_JAEGER) $(ROLE_KAFKA) $(ROLE_OLM) $(ROLE_PUBLIC_API); do \
		cp $${resource}.$(TMPL) $${resource}.$(YAML); \
	done
