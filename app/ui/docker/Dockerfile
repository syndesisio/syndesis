#FROM scratch
FROM golang:1.11.0

LABEL maintainer="Syndesis Authors <syndesis@googlegroups.com>" \
      vendor="Syndesis Authors" \
      version="1.0.0" \
      release="1" \
      summary="Syndesis" \
      description="Harness the Power of Open Source in a Truly Hybrid Integration Platform."

### Required labels above - recommended below
LABEL url="https://github.com/syndesisio/syndesis" \
      io.k8s.display-name="Syndesis" \
      io.openshift.expose-services="http:8080" \
      io.openshift.tags="syndesis"

# Copy licenses
USER 998
EXPOSE 8080

#
# the maven dir has additional files added in by the
# fabric8-maven-plugin
COPY licenses /licenses
COPY maven/target/go/linux-amd64/syndesis-ui /syndesis-ui
COPY maven/dist /html
CMD [  "/syndesis-ui", "--port", "8080", "--path", "/html", "--etags", "--spa"]
