#!/bin/bash

if [ -z "${1}" ]; then
  echo "ERROR: ${0} <target-file>"
  exit 1
fi

TARGET_FILE="${1}"

if [ -f "${TARGET_FILE}" ]; then
  echo "INFO: The pull-secret credentials file already exists ... skipping creation."
  exit 0
fi

#
# Test the credentials entered and give a couple of subsequent tries
# then exit if still not right
#
iterations=0
max=2
while [  $iterations -le $max ];
do
  echo "creating pull secret 'syndesis-pull-secret' ..."
  echo "enter username for redhat registry access and press [ENTER]: "
  read username
  echo "enter password for redhat registry access and press [ENTER]: "
  read -s password

  # Testing access that credentials are correct
  reply=$(curl -IsL -u ${username}:${password} "https://sso.redhat.com/auth/realms/rhcc/protocol/redhat-docker-v2/auth?service=docker-registry&client_id=curl&scope=repository:rhel:pull")

  # Does reply contain "200 OK".
  if [ -z "${reply##*200 OK*}" ]; then
    # All good so break out of loop & carry on ...
    break
  else
    # Credentials wrong ... give a couple more tries or exit
    echo "ERROR: Credentials cannot be verified with redhat registry."
    if [ $iterations -lt $max ]; then
      echo "Please try again ... ($((iterations+1))/$((max+1)))"
    else
      echo "Exiting ... ($((iterations+1))/$((max+1)))"
      exit 1
    fi
  fi

  let iterations=iterations+1
done

#
# Need to ensure there are no extra carriage returns
#
auth=$(printf "%s:%s" "${username}" "${password}" | base64 -w 0)

#
# Encode the whole pull secret for both registries
#
cat <<EOF > ${TARGET_FILE}
{
  "auths": {
    "registry.redhat.io": {
      "auth":"${auth}"
    },
    "registry.connect.redhat.com": {
      "auth":"${auth}"
    }
  }
}
EOF
