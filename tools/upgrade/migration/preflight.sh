#!/bin/bash
# Preflight checks before an upgrade can be performed. This script can and probably
# should be updated for each version upgrade


# ================================================
# Adapt this function for every upgrade version and check whether the current version
# is compatible to the target version for this upgrade
check_upgrade_from_current_version() {
    local current_version="${1}"
    local current_minor_version="${2}"
    local target_version="${3}"


    # TODO: Please add checks whether the current version can be updated to the target. Typically
    # this is probably only possibly from one version before, but could be also less restrictive.
    # The check can be done very individually as this script file is also part of the target version
    # release and hence can be changed from release to release
    echo "###"
    echo "### --> OK"
}

# ======================================================================
# Being called from upgrade.sh
preflight_version_check() {
    local current_version="${1}"
    local target_version="${2}"
    local current_minor_version="$(extract_minor_version $current_version)"

    echo "### -----------------------------------------"
    echo "### PREFLIGHT CHECK"
    echo "### Upgrade from $current_version ($current_minor_version) --> $target_version"
    # When running in a container then this env variable is set to
    # the version to update to. This must be the same as the target version
    # provided as argument
    if [ -n "${SYNDESIS_VERSION:-}" ]; then
        local target_minor_version="$(extract_minor_version $target_version)"
        local syndesis_minor_version="$(extract_minor_version $SYNDESIS_VERSION)"
        if [ "${syndesis_minor_version}" != "${target_minor_version}" ]; then
            echo "Internal error: Container template's version is not the same as upgrade container tag"
            echo "- Container version:               $SYNDESIS_VERSION"
            echo "- Version extracted from template: $target_version"
            exit 1
        fi
    fi

    check_upgrade_from_current_version "$current_version" "$current_minor_version" "$target_version"
    echo "### -----------------------------------------"
    echo
}


extract_minor_version() {
    local version=$1
    if [ $version = "latest" ]; then
        echo "latest"
        return
    fi
    local minor_version=$(echo "$version"XX | sed 's/^\([0-9]\+\.[0-9]\+\).*$/\1/')
    if [ $minor_version = "${version}XX" ]; then
        echo "ERROR: Cannot extract minor version from '$version'"
        return
    fi
    echo $minor_version
}
