#!/bin/bash
google-chrome http://$(oc get routes syndesis --template "{{.spec.host}}")
