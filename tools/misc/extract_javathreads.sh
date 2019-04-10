#!/bin/sh

# Extract Java thred dumps with CPU usage from target POD. 
#
# Usage:
# extract_javathreads.sh <pod name> [<dir>]
#
# <pod name> : Name of  pod. It could be an integration pod
# <dir> : Where to store the source (default: .)
#
# Required tools:
# - bash
# - oc

set -euo pipefail

pod=${1:-xxx}
if [ $pod = "xxx" ]; then
  echo "Usage $0 <pod> [directory]"
  exit 1
fi
dest=${2:-.}
if [ ! -d $dest ]; then
  echo "$dest is not a directory"
  exit 1
fi

pod=$(oc get pods -o name | grep -v -- "-build" | grep $pod | sed -e "s/^pods\///")

pid=$(oc exec $pod ps aux | grep java | awk '{print $2}');
oc exec $pod -- bash -c "mkdir -p /deployments/tmp";
oc exec $pod -- bash -c "for x in {1..10}; do jstack -l $pid >> /deployments/tmp/jstack.out; top -b -n 1 -H -p $pid >> /deployments/tmp/top.out; sleep 20; done";
oc rsync $pod:/deployments/tmp/jstack.out .
oc rsync $pod:/deployments/tmp/top.out .
oc exec $pod -- bash -c "rm /deployments/tmp/jstack.out /deployments/tmp/top.out";
