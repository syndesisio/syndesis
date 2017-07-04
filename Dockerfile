FROM syndesis/caddy:v0.10.4
MAINTAINER Syndesis Developers <syndesis@googlegroups.com>

COPY Caddyfile /etc/Caddyfile
COPY dist /srv
