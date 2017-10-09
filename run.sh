#!/bin/bash
# 
# This script is used to update/customize the Docker build 
#
#
set -euo pipefail
dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


while [ $# -gt 0 ]; do
	case $1 in
		--from) 
			perl -p -i -e "s|FROM .*|FROM $2|" Dockerfile
			shift 2 
		;;
		--update-project) 
			# todo: download the jar from mvn.  This should be done in the pipeline
			# curl -O syndesis-builder-image-generator.jar 
			java -jar syndesis-builder-image-generator.jar --to=$dir/project
			shift 1
		;;
		--to) 
			docker build $dir -t $2
			shift 2 
		;;
	esac
done



