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

			from="fabric8/s2i-java:2.0.2"
			from=${2:-$from}			

			echo "Updating Image: FROM $from"
			perl -p -i -e "s|FROM .*|FROM $from|" Dockerfile
			shift 2 
		;;
		--update-project) 

			jarlocation="https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=io.syndesis&a=syndesis-builder-image-generator&v=0.1-SNAPSHOT&e=jar"
			jarlocation=${2:-$jarlocation}

			echo "Updating project using: $jarlocation"
			if [[ $jarlocation == http* ]] ; then 
				wget --quiet -O syndesis-builder-image-generator.jar "$jarlocation"
				jarlocation=syndesis-builder-image-generator.jar
			fi

			echo java -jar ${jarlocation} --to=$dir/project
			java -jar ${jarlocation} --to=$dir/project
			shift 2
		;;
		--to) 
			to="syndesis/syndesis-s2i-builder"
			to=${2:-$to}			

			echo "Building Image: $to"
			docker build $dir -t $to
			shift 2 
		;;
	esac
done



