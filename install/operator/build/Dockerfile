FROM registry.access.redhat.com/ubi7/ubi-minimal:latest

ENV OPERATOR=/usr/local/bin/operator \
    USER_UID=1001 \
    USER_NAME=operator

# install operator binary
COPY build/_output/bin/operator ${OPERATOR}
COPY build/bin /usr/local/bin
RUN  /usr/local/bin/user_setup
USER ${USER_UID}

# Add conf directory
ADD build/conf /conf

ENTRYPOINT ["/usr/local/bin/entrypoint", "--template-config", "/conf/template-config.yaml"]
