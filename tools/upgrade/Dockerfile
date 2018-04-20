FROM centos:7

ARG version=latest
ARG openshift_yum_repo=centos-release-openshift-origin37
ARG openjdk_rpm=java-1.8.0-openjdk-headless

WORKDIR /opt

LABEL maintainer="dev@syndesis.io"

ENV KUBECONFIG=/opt/kube-config \
    SYNDESIS_VERSION=${version}

# Install jq & oc
RUN yum install -y "$openshift_yum_repo" \
 && yum install -y \
        "$openjdk_rpm" \
        "tree" \
        "jq" \
        "origin-clients" \
 && mkdir /opt/backup \
 && chgrp -R 0 /opt \
 && chmod -R g=u /opt

COPY syndesis.yml /opt/template/
COPY upgrade.sh common.sh syndesis-cli.jar /opt/
COPY steps /opt/steps
COPY migration /opt/migration


ENTRYPOINT [ \
  "/bin/bash", \
  "/opt/upgrade.sh", \
  "--template", "/opt/template/syndesis.yml", \
  "--migration", "/opt/migration" \
]
