FROM syndesis/syndesis-s2i

USER 0

# Copy over all Camel-K dependencies to docker maven repo
# The integration expects all dependencies in /tmp/artifacts/m2
# so you cannot change the location of the local repo!
ADD repository /tmp/artifacts/m2
RUN chown -R 1000 /tmp/artifacts/m2

USER 1000
