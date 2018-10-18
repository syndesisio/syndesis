#!/bin/bash

update_template_params_for_sar_project() {
    local params=$(mktemp)
    oc get secret syndesis-global-config -o jsonpath={.data.params} | base64 --decode > ${params}

    set +e
    if ! grep -q SAR_PROJECT "${params}"; then
        local sar_project=$(grep OPENSHIFT_PROJECT ${params} | sed -e s/OPENSHIFT_PROJECT/SAR_PROJECT/)
        local tmp=$(mktemp)
        echo $sar_project > ${tmp}
        cat "${params}" >> ${tmp}
        mv "${tmp}" "${params}"
        set -e
        oc patch secret syndesis-global-config -p "{\"data\": { \"params\": \"$(cat $params | base64)\" }}"
    fi
    set -e
    #rm "$params"
}

update_template_params_for_sar_project
