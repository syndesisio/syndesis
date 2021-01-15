ASSETS := ../../../pkg/generator/assets
ROLE := ./role.yml
ROLE_JAEGER := ./cluster-role-jaeger.yml
ROLE_KAFKA := ./cluster-role-kafka.yml
ROLE_PUBLIC_API := ./cluster-role-public-api.yml
ROLE_OLM := ./cluster-role-olm.yml
ROLENAME ?= syndesis-operator

.PHONY: sync

#
# Copy the go template from the src directory
# Convert the go template to a formatted yaml file:
# - Convert go-style array to yaml format, ie. remove '-' & double space
# - Replace Kind & Role placeholders
#
sync:
	cp $(ASSETS)/install/role.yml.tmpl $(ROLE)
	sed -i 's/{{.Kind}}/Role/' $(ROLE)
	sed -i 's/{{.Role}}/$(ROLENAME)/' $(ROLE)
	sed -i 's/- kind:/kind:/' $(ROLE)
	sed -i 's/^  //' $(ROLE)
	cp $(ASSETS)/install/cluster_role_jaeger.yml.tmpl $(ROLE_JAEGER)
	sed -i '/^{{\|^#\|^$$/d' $(ROLE_JAEGER)
	cp $(ASSETS)/install/cluster_role_kafka.yml.tmpl $(ROLE_KAFKA)
	cp $(ASSETS)/install/cluster_role_olm.yml.tmpl $(ROLE_OLM)
	sed -i '/^{{\|^$$/d' $(ROLE_OLM)
	sed -i 's/^- /\n---\n/' $(ROLE_OLM)
	sed -i 's/^  //' $(ROLE_OLM)
	sed -i '/{{- if\|{{- end/d' $(ROLE_OLM)
	cp $(ASSETS)/install/cluster_role_public_api.yml.tmpl $(ROLE_PUBLIC_API)
