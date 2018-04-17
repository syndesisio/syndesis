FROM centos:7

ARG version=latest

WORKDIR /opt

LABEL maintainer="dev@syndesis.io"

ENV KUBECONFIG=/opt/kube-config \
    SYNDESIS_VERSION=${version}

# Install jq & oc
RUN yum install -y tree \
 && curl -L https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64 -o /usr/local/bin/jq \
 && chmod +x /usr/local/bin/jq \
 && curl -L https://github.com/openshift/origin/releases/download/v3.7.2/openshift-origin-client-tools-v3.7.2-282e43f-linux-64bit.tar.gz -o oc.tar.gz \
 && tar zxvf oc.tar.gz \
 && mv openshift-origin-*/oc /usr/local/bin \
 && rm -rf oc.tar.gz openshift-origin-* \
 && mkdir /opt/backup \
 && chgrp -R 0 /opt \
 && chmod -R g=u /opt

COPY syndesis.yml /opt/template/
COPY upgrade.sh /opt/
COPY steps /opt/steps
COPY migration /opt/migration


ENTRYPOINT [ \
  "/bin/bash", \
  "/opt/upgrade.sh", \
  "--template", "/opt/template/syndesis.yml", \
  "--migration", "/opt/migration" \
]
