#!/usr/bin/env bash

load operator_funcs

setup() {
    if [[ -f "${OPERATOR_BINARY}" ]]; then
        rm "${OPERATOR_BINARY}"
    fi
}

teardown() {
    if [[ -f "${OPERATOR_BINARY}" ]]; then
        rm "${OPERATOR_BINARY}"
    fi
}

@test "invoke check_for_command with wrong command, it should return 1" {
    run check_for_command curll
    [ "$status" -eq 1 ]
}

@test "invoke check_for_command with existing command, it should return 0" {
    run check_for_command curl
    [ "$status" -eq 0 ]
}

@test "invoke check_operator_binary with a nonexistent file returns returns 1" {
    OPERATOR_BINARY=foo run check_operator_binary
    [ "$status" -eq 1 ]
}

@test "check prepare_operator_binary_path path" {
    fail=1
    run prepare_operator_binary_path
    [ "$status" -eq 0 ]

    if [[ -d $(dirname ${OPERATOR_BINARY}) ]]; then
        fail=0
    fi
    [ "$fail" -eq 0 ]
}

@test "invoke download_operator_binary and download the binary" {
    fail=1
    run download_operator_binary
    [ "$status" -eq 0 ]
    [ "${lines[1]}" = "operator binary successfully downloaded" ]

    if [[ "${lines[0]}" == *"operator binary not found under"* ]]; then
        fail=0
    fi
    [ "$fail" -eq 0 ]
}
