#!/bin/sh

version=1.10

docker build -t syndesis/godev:${version} . && \
docker push syndesis/godev:${version} && \
docker tag syndesis/godev:${version} syndesis/godev:latest && \
docker push syndesis/godev:latest
