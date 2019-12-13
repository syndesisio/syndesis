#!/bin/bash
#
# Change camel version in syndesis
#
# Ussage:
#   - ./syndesisCamelVersion.sh <camel version>
#
# Example:
#   - ./syndesisCamelVersion.sh 2.23.2.fuse-760009


new_camel=${1:-xxx}
if [ $new_camel = "xxx" ]; then
  echo "Usage $0 <camel version>"
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

# prepare the camel-catalog file, as the version is in the file name
current_camel_catalog=$(ls install/operator/pkg/generator/assets/addons/camelk/camel-catalog-*.tmpl)
new_camel_catalog=$(echo $current_camel_catalog | sed "s/${current_camel}/${new_camel}/g")

sed -i "s/$current_camel/$new_camel/g" \
    app/pom.xml \
    app/extension/bom/pom.xml \
    app/integration/bom-camel-k/pom.xml \
    app/integration/bom/pom.xml \
    install/operator/build/conf/config.yaml \
    install/operator/build/conf/config-test.yaml \
    install/operator/pkg/generator/assets_vfsdata.go \
    install/operator/pkg/syndesis/configuration/configuration_test.go \
    ${current_camel_catalog}

# rename camel-catalog file, as the version is in the file name
git mv ${current_camel_catalog} ${new_camel_catalog}

# must regenerate go files
cd install/operator
go generate ./pkg/...
cd -

git status
echo "You should use git to add the files and commit them"
