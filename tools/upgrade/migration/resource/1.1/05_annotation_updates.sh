#!/bin/bash
oc annotate route/syndesis --overwrite=true console.alpha.openshift.io/overview-app-route=true
