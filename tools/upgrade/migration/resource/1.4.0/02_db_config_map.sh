#!/bin/bash
# Add the new ConfigMap for configuring syndesis-db
cat <<EOT | oc create -f -
apiVersion: v1
kind: ConfigMap
metadata:
  labels:
    app: syndesis
    syndesis.io/app: syndesis
    syndesis.io/type: infrastructure
    syndesis.io/component: syndesis-db
  name: syndesis-db-conf
data:
  syndesis-postgresql.conf: |
    log_autovacuum_min_duration = 0
    autovacuum_max_workers = 6
    autovacuum_naptime = 15s
    autovacuum_vacuum_threshold = 25
    autovacuum_vacuum_scale_factor = 0.1
    autovacuum_analyze_threshold = 10
    autovacuum_analyze_scale_factor = 0.05
    autovacuum_vacuum_cost_delay = 10ms
    autovacuum_vacuum_cost_limit = 1000
EOT
