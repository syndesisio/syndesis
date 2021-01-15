ASSETS := ../../../pkg/generator/assets
CR := ./syndesis-cr.yml

.PHONY: sync

#
# Copy the go template from the src directory
# Convert the go template to a formatted yaml file:
# - Remove any range/end sections (not required)
#
sync:
	cp $(ASSETS)/install/app.yml.tmpl $(CR)
	sed -i '/{{- range/,/{{- end }}\|{{end}}/d' $(CR)
