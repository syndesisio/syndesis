FROM centos:7

ARG version=latest
ARG openshift_yum_repo=centos-release-openshift-origin39
ARG openjdk_rpm=java-1.8.0-openjdk-headless

WORKDIR /opt

LABEL maintainer="dev@syndesis.io"

ENV KUBECONFIG=/opt/kube-config \
    SYNDESIS_VERSION=${version}

# Install jq & oc
# Enable EPEL for "uniguruma" package (required by jq)
RUN yum clean all \
 && yum update -y \
 && yum install -y "epel-release" \
 && sed -i -e 's/#baseurl/baseurl/g' -e 's/metalink/#metalink/g' /etc/yum.repos.d/epel.repo \
 && yum install -y "$openshift_yum_repo" \
 && yum install -y \
        "$openjdk_rpm" \
        "tree" \
        "jq" \
        "origin-clients" \
        "PyYAML" \
 && mkdir /opt/backup \
 && chgrp -R 0 /opt \
 && chmod -R g=u /opt

# Copy licenses
RUN mkdir -p /opt/ipaas/licenses
COPY licenses /opt/ipaas/licenses

COPY upgrade.sh common.sh syndesis-cli.jar /opt/
COPY steps /opt/steps
COPY migration /opt/migration

ENTRYPOINT [ \
  "/bin/bash", \
  "/opt/upgrade.sh", \
  "--migration", "/opt/migration" \
]
