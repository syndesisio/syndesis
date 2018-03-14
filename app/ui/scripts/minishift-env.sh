# This script should only be sourced in your terminal not run, i.e. . ./minishift-env.sh

eval $(minishift oc-env)
eval $(minishift docker-env)
