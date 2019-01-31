#!/bin/bash

function print_exit {
  echo $1 && exit 1
}

function print_info {
  echo "[ INFO ] $1"
}

DEPENDENCIES='oc docker syndesis'
M2=$HOME/.m2/repository/io/syndesis

for d in $DEPENDENCIES; do
  which $d > /dev/null 2>&1 || print_exit "No binary found for $d"
done

initial_status=`minishift status | grep DiskUsage | awk '{print $2}'`

[[ $HOME ]] || print_exit '$HOME env variable is not defined not defined'

# Remove syndesis artifacts, this grows to eternity
#print_info "Attempting to remove content from $M2"
#[[ -d $M2 ]] && rm -rf $M2 && print_info "Dir $M2 removed ..."

# Run syndesis cleanup
print_info "Attempting to run syndesis cleanup"
CURRENT_USER=`oc whoami`
oc login -u admin > /dev/null 2>&1 
syndesis dev --cleanup

oc login -u $CURRENT_USER > /dev/null 2>&1 

# Prune unused resources from docker
print_info "Attempting to run docker system prune"
eval $(minishift docker-env)
docker system prune

print_info "Deleting s2i base image and integration images ..."
docker rmi $(docker images -f "reference=172.30.1.1:5000/syndesis/i-*" -q) > /dev/null 2>&1 
docker rmi $(docker images -f "reference=172.30.1.1:5000/syndesis/syndesis-s2i" -q) > /dev/null 2>&1 

final_status=`minishift status | grep DiskUsage | awk '{print $2}'`

print_info "Disk usage in minishift lowered from ${initial_status} to ${final_status}"
