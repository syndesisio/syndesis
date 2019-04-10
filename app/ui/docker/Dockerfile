FROM docker.io/centos:7

ENV NGINX_VERSION 1.13.4-1.el7

# This refers to the files copied by assembly configuration in pom.xml
# Change this to e.g. "dist" if running the dockerfile from top-level e.g.
# docker build --build-arg SRC_DIR=dist --build-arf CONTEXT_DIR=docker -f docker/Dockerfile
ARG SRC_DIR="maven/dist"
ARG CONTEXT_DIR="."

LABEL name="nginxinc/nginx" \
      maintainer="Syndesis Authors <syndesis@googlegroups.com>" \
      vendor="NGINX Inc." \
      version="${NGINX_VERSION}" \
      release="1" \
      summary="NGINX" \
      description="nginx will do ....."
### Required labels above - recommended below
LABEL url="https://www.nginx.com/" \
      io.k8s.display-name="NGINX" \
      io.openshift.expose-services="http:8080" \
      io.openshift.tags="nginx,nginxinc"

ADD ${CONTEXT_DIR}/nginx.repo /etc/yum.repos.d/nginx.repo

RUN curl -sO http://nginx.org/keys/nginx_signing.key && \
    rpm --import ./nginx_signing.key && \
    yum -y install --setopt=tsflags=nodocs nginx-${NGINX_VERSION}.ngx && \
    rm -f ./nginx_signing.key && \
    yum clean all

# forward request and error logs to docker log collector
# - Change pid file location & remove nginx user & change port to 8080
# - modify perms for non-root runtime
RUN ln -sf /dev/stdout /var/log/nginx/access.log && \
    ln -sf /dev/stderr /var/log/nginx/error.log && \
    sed -i 's/\/var\/run\/nginx.pid/\/var\/cache\/nginx\/nginx.pid/g' /etc/nginx/nginx.conf && \
    sed -i -e '/user/!b' -e '/nginx/!b' -e '/nginx/d' /etc/nginx/nginx.conf && \
    rm -f /etc/nginx/conf.d/default.conf && \
    chown -R 998 /var/cache/nginx /etc/nginx && \
    chmod -R g=u /var/cache/nginx /etc/nginx


# Copy licenses
RUN mkdir -p /opt/ipaas/licenses
COPY licenses /opt/ipaas/licenses

#VOLUME ["/var/cache/nginx"]

EXPOSE 8080 8443

# Add symbolic link to config.json to avoid mounting issues
RUN ln -sf /usr/share/nginx/html/config/config.json /usr/share/nginx/html/config.json

USER 998

CMD ["nginx", "-g", "daemon off;"]

COPY ${CONTEXT_DIR}/nginx-syndesis.conf /etc/nginx/conf.d
COPY ${SRC_DIR} /usr/share/nginx/html
