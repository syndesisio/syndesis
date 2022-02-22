#!/bin/bash

check_env_var() {
  if [ -z "${2}" ]; then
    echo "Error: ${1} env var not defined"
    exit 1
  fi
}

check_env_var "PACKAGE" ${PACKAGE}
check_env_var "SRC_CATALOG" ${SRC_CATALOG}
check_env_var "CATALOG_DIR" ${CATALOG_DIR}
check_env_var "OPM" ${OPM}
check_env_var "BUNDLE_IMAGE" ${BUNDLE_IMAGE}
check_env_var "CSV_NAME" ${CSV_NAME}
check_env_var "CSV_REPLACES" ${CSV_REPLACES}
check_env_var "CHANNEL" ${CHANNEL}

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
STATUS=$(${OPM} alpha generate dockerfile ${CATALOG_DIR} 2>&1)
if [ $? != 0 ]; then
  echo "Failed"
  echo "Error: ${STATUS}"
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
