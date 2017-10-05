FROM registry.access.redhat.com/jboss-fuse-6/fis-java-openshift:latest

ADD project /tmp/project
RUN /bin/bash /tmp/project/setup.sh

USER root
RUN rm -rf /tmp/project
USER 1000
