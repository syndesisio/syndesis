FROM centos:7

ARG version=latest 
ARG OPENSHIFT_YUM_REPO=centos-release-openshift-origin37
ARG OPENJDK_RPM=java-1.8.0-openjdk-headless

WORKDIR /opt

LABEL maintainer="dev@syndesis.io"

ENV KUBECONFIG=/opt/kube-config \
    SYNDESIS_VERSION=${version} 

# Install jq & oc
RUN yum install -y "$OPENSHIFT_YUM_REPO" "$OPENJDK_RPM" tree\
 && yum install -y jq origin-clients \
 && mkdir /opt/backup \
 && chgrp -R 0 /opt \
 && chmod -R g=u /opt

COPY syndesis.yml /opt/template/
COPY upgrade.sh /opt/
COPY steps /opt/steps
COPY migration /opt/migration
COPY syndesis-cli.jar /opt


ENTRYPOINT [ \
  "/bin/bash", \
  "/opt/upgrade.sh", \
  "--template", "/opt/template/syndesis.yml", \
  "--migration", "/opt/migration" \
]
