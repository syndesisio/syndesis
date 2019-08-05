#!/bin/bash

source ../util/common_funcs
source ../util/openshift_funcs

#
# Test standard approach
#
setup_oc

#
# Test other possibilities
#
echo "=== Expected to be OK ==="
compare_version '3.9.0' '3.9.0'
compare_version '3.9.1' '3.9.0'
compare_version '3.10.0' '3.9.0'
compare_version '3.10.1' '3.9.0'
compare_version '4.1.0' '3.9.0'
compare_version '4.4.5' '3.4.9'
compare_version '4.4.5' '4.4.4'


echo "=== Expected to ERROR ==="
echo "(will exit after testing each one)"
#compare_version '3.1.0' '3.9.0'
#compare_version '3.8.9' '3.9.0'
#compare_version '2.9.0' '3.9.0'
#compare_version '4.4.4' '4.4.5'
compare_version '3.9.0' '3.9.0.1'
