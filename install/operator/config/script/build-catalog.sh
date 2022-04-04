#!/bin/bash

check_env_var() {
  if [ -z "${2}" ]; then
    echo "Error: ${1} env var not defined"
    exit 1
  fi
}

version_to_number() {
  echo $(echo "${1}" | awk -F. '{ printf("%d%03d%03d%03d\n", $1,$2,$3,$4); }')
}

check_env_var "SRC_CATALOG" ${SRC_CATALOG}
check_env_var "CATALOG_DIR" ${CATALOG_DIR}
check_env_var "OPM" ${OPM}
check_env_var "BUNDLE_IMAGE" ${BUNDLE_IMAGE}
check_env_var "CSV_NAME" ${CSV_NAME}
check_env_var "CSV_REPLACES" ${CSV_REPLACES}
check_env_var "CHANNEL" ${CHANNEL}
check_env_var "PACKAGE" ${PACKAGE}

if ! command -v ${OPM} &> /dev/null
then
  echo "Error: opm is not available. Was OPM env var defined correctly: ${OPM}"
  exit 1
fi

if [ -d "${CATALOG_DIR}" ]; then
  rm -rf "${CATALOG_DIR}"
fi

if [ -f "${CATALOG_DIR}.Dockerfile" ]; then
  rm -f "${CATALOG_DIR}.Dockerfile"
fi

#
# Check version of opm
#
version=$(${OPM} version | sed -n 's/.*OpmVersion\:"v\([0-9.]\+\)", .*/\1/p')
if [ $(version_to_number ${version}) -lt $(version_to_number "1.21.0") ]; then
  echo "Error: opm version is ${version}. Should be 1.21.0+"
  exit 1
fi

mkdir "${CATALOG_DIR}"

${OPM} render ${SRC_CATALOG} -o yaml > ${CATALOG_DIR}/bundles.yaml
if [ $? != 0 ]; then
  echo "Error: failed to render the base catalog"
  exit 1
fi

${OPM} render --skip-tls -o yaml \
  ${BUNDLE_IMAGE} > ${CATALOG_DIR}/${PACKAGE}.yaml
if [ $? != 0 ]; then
  echo "Error: failed to render the ${PACKAGE} bundle catalog"
  exit 1
fi

cat << EOF >> ${CATALOG_DIR}/${PACKAGE}.yaml
---
schema: olm.channel
package: ${PACKAGE}
name: ${CHANNEL}
entries:
  - name: ${CSV_NAME}
    replaces: ${CSV_REPLACES}
EOF

echo -n "Validating catalog ... "
STATUS=$(${OPM} validate ${CATALOG_DIR} 2>&1)
if [ $? != 0 ]; then
  echo "Failed"
  echo "Error: ${STATUS}"
  exit 1
else
  echo "OK"
fi

echo -n "Generating catalog dockerfile ... "
STATUS=$(${OPM} generate dockerfile ${CATALOG_DIR} 2>&1)
if [ $? != 0 ]; then
  echo "Failed"
  echo "Error: ${STATUS}"
  exit 1
elif [ ! -f ${CATALOG_DIR}.Dockerfile ]; then
  echo "Failed"
  echo "Error: Dockerfile failed to be created"
  exit 1
else
  echo "OK"
fi

echo -n "Building catalog image ... "
BUNDLE_INDEX_IMAGE="${BUNDLE_IMAGE%:*}-index":"${BUNDLE_IMAGE#*:}"
STATUS=$(docker build . -f ${CATALOG_DIR}.Dockerfile -t ${BUNDLE_INDEX_IMAGE} 2>&1)
if [ $? != 0 ]; then
  echo "Failed"
  echo "Error: ${STATUS}"
  exit 1
else
  echo "OK"
  echo "Index image ${BUNDLE_INDEX_IMAGE} can be pushed"
fi
