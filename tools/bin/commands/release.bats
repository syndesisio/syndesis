#!/usr/bin/env bash
source ./release

OPERATOR_PATH=$(realpath $PWD/../../../install/operator/)
RELEASE_PATH=${OPERATOR_PATH}/releases
BINARY_PATH=${OPERATOR_PATH}/dist
RELEASE_VERSION=1.9.99

@test "test check for env variables" {
    run get_github_username
    [ "$status" -eq 0 ]

    run get_github_access_token
    [ "$status" -eq 0 ]
}

@test "test from build to publish" {
    fail=0

    # run "$OPERATOR_PATH/build.sh" --operator-build docker --image-build skip --souce-gen skip

    # Prepare binaries for release
    run prepare_binaries "${BINARY_PATH}" "${RELEASE_PATH}"
    [ "$status" -eq 0 ]

    if ! [[ -x $RELEASE_PATH/syndesis-operator-darwin-amd64 ]]; then
        fail=1
    fi
    [ "$fail" -eq 0 ]

    if ! [[ -x $RELEASE_PATH/syndesis-operator-linux-amd64 ]]; then
        fail=1
    fi
    [ "$fail" -eq 0 ]

    if ! [[ -x $RELEASE_PATH/syndesis-operator-windows-amd64 ]]; then
        fail=1
    fi
    [ "$fail" -eq 0 ]

    # Release the binaries
    publish_artifacts "${RELEASE_PATH}" "$RELEASE_VERSION"
}

@test "test prepare files for release" {
    skip
    fail=0

    # Prepare binaries for release
    run prepare_binaries "${BINARY_PATH}" "${RELEASE_PATH}"
    [ "$status" -eq 0 ]

    if ! [[ -x $RELEASE_PATH/syndesis-operator-darwin-amd64 ]]; then
        fail=1
    fi
    [ "$fail" -eq 0 ]

    if ! [[ -x $RELEASE_PATH/syndesis-operator-linux-amd64 ]]; then
        fail=1
    fi
    [ "$fail" -eq 0 ]

    if ! [[ -x $RELEASE_PATH/syndesis-operator-windows-amd64 ]]; then
        fail=1
    fi
    [ "$fail" -eq 0 ]
}
