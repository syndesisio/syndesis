#!/usr/bin/env bash

load operator_funcs

setup() {
    touch /tmp/foo
}

teardown() {
    rm /tmp/foo
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
    run check_operator_binary path file
    [ "$status" -eq 1 ]
}

@test "check default binary path" {
    fail=1
    run default_binary_path
    if [[ $output == *"syndesis/tools/bin/commands/binaries"* ]]; then
        fail=0
    fi

    [ "$fail" -eq 0 ]
}
