FROM fabric8/s2i-java:2.0.2

ADD project /tmp/project
RUN \
  cd /tmp/project; \
  mvn clean package

USER root
RUN rm -rf /tmp/project
USER 1000
