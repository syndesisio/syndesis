#!/usr/bin/env bats

source ./common_funcs
source ./openshift_funcs

@test "test output from a minishift oc command, it should compare correctly and return OK" {
    function oc() {
      printf "oc v3.10.0+dd10d17
kubernetes v1.10.0+b81c8f8
features: Basic-Auth GSSAPI Kerberos SPNEGO

Server https://api.crc.testing:6443
kubernetes v1.14.6+73b5d76";
    }
    export -f oc

    result=$(check_oc_version)
    echo "Result: $result"

    unset oc
    [ "$result" == "OK" ]
}

@test "test output from a crc oc command, it should compare correctly and return OK" {
    function oc() {
      printf "Client Version: v4.3.0
Server Version: 4.2.0-0.nightly-2019-09-26-192831
Kubernetes Version: v1.14.6+73b5d76";
    }
    export -f oc

    result=$(check_oc_version)
    echo "Result: $result"

    unset oc
    [ "$result" == "OK" ]
}

@test "test output from a oc 4.2.0 command, it should compare correctly and return OK" {
    function oc() {
      printf "Client Version: openshift-clients-4.2.0-201910041700
Kubernetes Version: v1.11.0+d4cacc0";
    }
    export -f oc

    result=$(check_oc_version)
    echo "Result: $result"

    unset oc
    [ "$result" == "OK" ]
}

@test "test output from a oc theoretical command (3-digit minor version), it should compare correctly and return OK" {
    function oc() {
      printf "Client Version: openshift-clients-4.101.0-201910041700
Kubernetes Version: v1.11.0+d4cacc0";
    }
    export -f oc

    result=$(check_oc_version)
    echo "Result: $result"

    unset oc
    [ "$result" == "OK" ]
}
