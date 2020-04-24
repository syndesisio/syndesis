#!/bin/bash

#############
#
# Start minikube by providing a few useful extra properties
# * Add -f to completely delete minikube install and start again
#
#############

while getopts :f option
do
  case "${option}"
  in
    f) RESET=1 ;;
    \?) echo "Usage: $0 (-f); exit ;;
  esac
done

if [ -n "$RESET" ]; then
  echo "Clearing out and resetting ..."
  minikube delete
  rm -rf ~/.minikube
fi

DRIVER="kvm2" # Modify to suit Operating System
DISK="60GB"   # Modify to suit hdd size
MEMORY="12GB" # Modify to suit physical installed RAM
CPUS=4        # Modify to suit number of CPUs

REGS=()       # Add any insecure registeries

OPTIONS="--driver ${DRIVER} --disk-size ${DISK} --memory ${CPUS}"

for REG in ${REGS[@]}; do
  OPTIONS="${OPTIONS} --insecure-registry ${REG}"
done

OPTIONS="${OPTIONS} --bootstrapper=kubeadm"

# Start minikube (unsurprisingly!)
minikube start ${OPTIONS} &
