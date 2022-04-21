FROM registry.access.redhat.com/ubi7/ubi-minimal:latest

ENV OPERATOR=/usr/local/bin/syndesis-operator \
    OPINIT=/usr/local/bin/operator-init \
    USER_UID=1001 \
    USER_NAME=operator

# install operator binary
COPY build/_output/bin/syndesis-operator ${OPERATOR}
COPY build/_output/bin/operator-init ${OPINIT}
COPY build/bin /usr/local/bin
RUN  /usr/local/bin/user_setup
USER ${USER_UID}

# Add conf directory
ADD build/conf /conf

ENTRYPOINT ["/usr/local/bin/entrypoint", "run"]
