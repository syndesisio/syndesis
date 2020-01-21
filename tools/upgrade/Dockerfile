FROM centos:7

ARG version=latest
ARG openjdk_rpm=java-1.8.0-openjdk-headless

WORKDIR /opt

LABEL maintainer="dev@syndesis.io"

# Install jq & oc
# Enable EPEL for "uniguruma" package (required by jq)
RUN yum clean all \
 && yum update -y \
 && yum install -y "epel-release" \
 && sed -i -e 's/#baseurl/baseurl/g' -e 's/metalink/#metalink/g' /etc/yum.repos.d/epel.repo \
 && yum install -y \
        "$openjdk_rpm" \
 && chgrp -R 0 /opt \
 && chmod -R g=u /opt

# Copy licenses
RUN mkdir -p /opt/ipaas/licenses
COPY licenses /opt/ipaas/licenses

COPY entrypoint.sh common.sh syndesis-cli.jar /opt/

ENTRYPOINT ["/opt/entrypoint.sh"]
