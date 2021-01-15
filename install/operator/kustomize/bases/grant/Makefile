ASSETS := ../../../pkg/generator/assets
GRANT := ./grant.yml
GRANT_JAEGER := ./grant-jaeger.yml
GRANT_KAFKA := ./grant-kafka.yml
GRANT_OLM := ./grant-olm.yml
GRANT_PUBLIC_API := ./grant-public-api.yml
KUBE_USER ?= developer
NAMESPACE ?= syndesis
ROLENAME ?= syndesis-operator

.PHONY: sync

#
# Copy the go template from the src directory
# Convert the go template to a formatted yaml file:
# - Replace Kind,  Role, User, Namespace placeholders
# - Delete any if & end codeword lines
#
sync:
	cp $(ASSETS)/install/grant/grant_role.yml.tmpl $(GRANT)
	sed -i 's/{{ .Kind }}/Role/' $(GRANT)
	sed -i 's/{{ .Role }}/$(ROLENAME)/' $(GRANT)
	sed -i 's/{{ .User }}/$(KUBE_USER)/' $(GRANT)
	sed -i 's/{{ .Namespace }}/$(NAMESPACE)/' $(GRANT)
	sed -i '/{{- if\|{{- end/d' $(GRANT)
	cp $(ASSETS)/install/grant/grant_cluster_role_jaeger.yml.tmpl $(GRANT_JAEGER)
	sed -i '/^{{\|^$$/d' $(GRANT_JAEGER)
	sed -i 's/^- /\n---\n/' $(GRANT_JAEGER)
	sed -i 's/^  //' $(GRANT_JAEGER)
	sed -i 's/{{ .Namespace }}/$(NAMESPACE)/' $(GRANT_JAEGER)
	sed -i 's/{{ .User }}/$(KUBE_USER)/' $(GRANT_JAEGER)
	cp $(ASSETS)/install/grant/grant_cluster_role_kafka.yml.tmpl $(GRANT_KAFKA)
	sed -i 's/{{ .Namespace }}/$(NAMESPACE)/' $(GRANT_KAFKA)
	cp $(ASSETS)/install/grant/grant_cluster_role_olm.yml.tmpl $(GRANT_OLM)
	sed -i '/^{{\|^$$/d' $(GRANT_OLM)
	sed -i 's/^- /\n---\n/' $(GRANT_OLM)
	sed -i 's/^  //' $(GRANT_OLM)
	sed -i 's/{{ .Role }}/$(ROLENAME)/' $(GRANT_OLM)
	sed -i 's/{{ .User }}/$(KUBE_USER)/' $(GRANT_OLM)
	cp $(ASSETS)/install/grant/grant_cluster_role_public_api.yml.tmpl $(GRANT_PUBLIC_API)
	sed -i 's/{{.Namespace}}/$(NAMESPACE)/' $(GRANT_PUBLIC_API)
