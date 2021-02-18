ASSETS := ../../../pkg/generator/assets
TMPL := tmpl
YAML := yaml

CR := ./syndesis-cr.gen

.PHONY: sync init

# start-sync
#
# Copy the go template from the src directory
# Convert the go template to a formatted yaml file:
# - Remove any range/end sections (not required)
#
sync:
	cp $(ASSETS)/install/app.yml.tmpl $(CR).$(TMPL)
	sed -i '/{{- range/,/{{- end }}\|{{end}}/d' $(CR).$(TMPL)
# end-sync

init: sync
	cp $(CR).$(TMPL) $(CR).$(YAML)
