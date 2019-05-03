FROM ${docker-base-image}

# This refers to the files copied by assembly configuration in pom.xml
# Change this to e.g. "dist" if running the dockerfile from top-level e.g.
# docker build --build-arg SRC_DIR=dist --build-arf CONTEXT_DIR=docker -f docker/Dockerfile
ARG SRC_DIR="maven/syndesis/build"
ARG CONTEXT_DIR="."

LABEL name="syndesis/ui" \
      maintainer="Syndesis Authors <syndesis@googlegroups.com>" \
      summary="Presentation layer for Syndesis" \
      description="Presentation layer for Syndesis - a flexible, customizable, cloud-hosted platform that provides core integration capabilities as a service." \
      url="https://syndesis.io/" \
      io.k8s.display-name="Syndesis" \
      io.k8s.description="Presentation layer for Syndesis - a flexible, customizable, cloud-hosted platform that provides core integration capabilities as a service." \
      io.openshift.tags="syndesis,integration"

USER root
# Copy licenses
RUN mkdir -p /opt/ipaas/licenses
COPY licenses /opt/ipaas/licenses
USER default

EXPOSE 8080 8443

COPY ${CONTEXT_DIR}/nginx-syndesis.conf /tmp/src/nginx-default-cfg/
COPY ${SRC_DIR} /tmp/src/

RUN $STI_SCRIPTS_PATH/assemble

CMD ["/bin/sh", "-c", "$STI_SCRIPTS_PATH/run"]
