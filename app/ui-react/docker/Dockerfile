FROM ${docker-base-image}

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
COPY maven/licenses /opt/ipaas/licenses

EXPOSE 8080 8443

COPY maven/nginx-syndesis.conf /tmp/src/nginx-default-cfg/
COPY maven/build /tmp/src/

RUN $STI_SCRIPTS_PATH/assemble

USER default

CMD ["/bin/sh", "-c", "$STI_SCRIPTS_PATH/run"]
