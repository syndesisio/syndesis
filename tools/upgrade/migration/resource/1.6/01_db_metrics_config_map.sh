#!/bin/bash
# Add ConfigMap for configuring the postgres-exporter
cat <<EOT | oc create -f -
apiVersion: v1
kind: ConfigMap
metadata:
    name: syndesis-db-metrics-config
    labels:
        app: syndesis
        syndesis.io/app: syndesis
        syndesis.io/type: infrastructure
        syndesis.io/component: syndesis-db-metrics
data:
  queries.yaml: |
    pg_database:
      query: "SELECT pg_database.datname, pg_database_size(pg_database.datname) as size FROM pg_database"
      metrics:
      - datname:
          usage: "LABEL"
          description: "Name of the database"
      - size:
          usage: "GAUGE"
          description: "Disk space used by the database"
EOT
