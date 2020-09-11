#!/bin/bash
#
# Change camel version in syndesis
#
# Ussage:
#   - ./syndesisCamelVersion.sh <camel version>
#
# Example:
#   - ./syndesisCamelVersion.sh 2.23.2.fuse-760024


new_camel=${1:-xxx}
if [ $new_camel = "xxx" ]; then
  echo "Usage $0 <camel version> (eg. 2.23.2.fuse-760024)"
  exit 1
fi

# test if the new camel version is available on maven repo
artifact_url=https://repository.jboss.org/nexus/content/groups/ea/org/apache/camel/camel-core/${new_camel}/camel-core-${new_camel}.pom
artifact_ok=$(curl -k -X GET -I ${artifact_url} 2>/dev/null | head -n 1 | cut -d$' ' -f2)
if [ ${artifact_ok} != 200 ]; then
  echo "Camel is not available in ${artifact_url}"
  echo "Camel version did not change."
  exit 1
fi

# get the current camel version
current_camel=$(grep '<camel.version>' app/pom.xml  | head -1 | cut -d '>' -f 2|cut -d '<' -f 1)
echo "Changing camel version from \"$current_camel\" to \"$new_camel\""

sed -i "s/$current_camel/$new_camel/g" \
    app/pom.xml \
    app/extension/bom/pom.xml \
    app/integration/bom/pom.xml \
    install/operator/build/conf/config.yaml \
    install/operator/pkg/generator/assets_vfsdata.go

cd install/operator
go generate ./pkg/...
cd -

git status
echo "You should use git to add the files and commit them"
