FROM registry.access.redhat.com/ubi8/openjdk-8

LABEL maintainer="dev@syndesis.io"

# Copy licenses
RUN mkdir -p /opt/ipaas/licenses
COPY licenses /opt/ipaas/licenses

COPY entrypoint.sh common.sh syndesis-cli.jar /opt/

ENTRYPOINT ["/opt/entrypoint.sh"]
