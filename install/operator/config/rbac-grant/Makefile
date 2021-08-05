ASSETS := ../../pkg/generator/assets
TMPL := tmpl
YAML := yaml
NAMESPACE_VAR := {NAMESPACE}
USER_VAR := {KUBE_USER}

ROLENAME := syndesis-operator
GRANT_USER := ./grant.gen
GRANT_JAEGER := ./grant-jaeger.gen
GRANT_KAFKA := ./grant-kafka.gen
GRANT_OLM := ./grant-olm.gen
GRANT_PUBLIC_API := ./grant-public-api.gen

# User customisable variables
include ../vars/Makefile

.PHONY: sync init

# start-sync
#
# Copy the go template from the src directory
# Convert the go template to a formatted yaml file:
# - Replace Kind,  Role, User, Namespace placeholders
# - Delete any if & end codeword lines
#
sync:
	cp $(ASSETS)/install/grant/grant_role.yml.tmpl $(GRANT_USER).$(TMPL)
	sed -i 's/{{ .Kind }}\|{{.Kind}}/Role/' $(GRANT_USER).$(TMPL)
	sed -i 's/{{ .Role }}\|{{.Role}}/$(ROLENAME)/' $(GRANT_USER).$(TMPL)
	sed -i 's/{{ .User }}\|{{.User}}/$(USER_VAR)/' $(GRANT_USER).$(TMPL)
	sed -i 's/{{ .Namespace }}\|{{.Namespace}}/$(NAMESPACE_VAR)/' $(GRANT_USER).$(TMPL)
	sed -i '/{{- if\|{{- end/d' $(GRANT_USER).$(TMPL)
	cp $(ASSETS)/install/grant/grant_cluster_role_jaeger.yml.tmpl $(GRANT_JAEGER).$(TMPL)
	sed -i 's/{{ .User }}\|{{.User}}/$(USER_VAR)/' $(GRANT_JAEGER).$(TMPL)
	sed -i 's/{{ .Namespace }}\|{{.Namespace}}/$(NAMESPACE_VAR)/' $(GRANT_JAEGER).$(TMPL)
	sed -i '/^{{\|^$$/d' $(GRANT_JAEGER).$(TMPL)
	sed -i 's/^- /\n---\n/' $(GRANT_JAEGER).$(TMPL)
	sed -i 's/^  //' $(GRANT_JAEGER).$(TMPL)
	cp $(ASSETS)/install/grant/grant_cluster_role_kafka.yml.tmpl $(GRANT_KAFKA).$(TMPL)
	sed -i 's/{{ .User }}\|{{.User}}/$(USER_VAR)/' $(GRANT_KAFKA).$(TMPL)
	sed -i 's/{{ .Namespace }}\|{{.Namespace}}/$(NAMESPACE_VAR)/' $(GRANT_KAFKA).$(TMPL)
	cp $(ASSETS)/install/grant/grant_cluster_role_olm.yml.tmpl $(GRANT_OLM).$(TMPL)
	sed -i 's/{{ .Role }}\|{{.Role}}/$(ROLENAME)/' $(GRANT_OLM).$(TMPL)
	sed -i 's/{{ .User }}\|{{.User}}/$(USER_VAR)/' $(GRANT_OLM).$(TMPL)
	sed -i 's/{{ .Namespace }}\|{{.Namespace}}/$(NAMESPACE_VAR)/' $(GRANT_OLM).$(TMPL)
	sed -i '/^{{\|^$$/d' $(GRANT_OLM).$(TMPL)
	sed -i 's/^- /\n---\n/' $(GRANT_OLM).$(TMPL)
	sed -i 's/^  //' $(GRANT_OLM).$(TMPL)
	cp $(ASSETS)/install/grant/grant_cluster_role_public_api.yml.tmpl $(GRANT_PUBLIC_API).$(TMPL)
	sed -i 's/{{ .User }}\|{{.User}}/$(USER_VAR)/' $(GRANT_PUBLIC_API).$(TMPL)
	sed -i 's/{{.Namespace}}\|{{.Namespace}}/$(NAMESPACE_VAR)/' $(GRANT_PUBLIC_API).$(TMPL)
# end-sync

init: sync
	for resource in $(GRANT_USER) $(GRANT_JAEGER) $(GRANT_KAFKA) $(GRANT_OLM) $(GRANT_PUBLIC_API); do \
		cp $${resource}.$(TMPL) $${resource}.$(YAML); \
		sed -i 's/$(NAMESPACE_VAR)/$(NAMESPACE)/' $${resource}.$(YAML); \
		sed -i 's/$(USER_VAR)/$(KUBE_USER)/' $${resource}.$(YAML); \
	done
