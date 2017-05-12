FROM jimmidyson/caddy:v0.9.5
MAINTAINER Syndesis Developers <syndesis@googlegroups.com>

COPY Caddyfile /etc/Caddyfile
COPY dist /srv
