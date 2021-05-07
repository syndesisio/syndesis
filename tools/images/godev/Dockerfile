FROM golang:1.16.3-alpine

# Install extras
RUN apk update \
 && apk add dep=0.4.1-r0 git openssh \
 && mkdir -p /gopath/bin /gopath/src /gopath/pkg \
 && chgrp -R 0 /gopath \
 && chmod -R g=u /gopath

WORKDIR /gopath

ENV GOPATH=/gopath
