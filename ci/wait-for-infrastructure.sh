#!/bin/bash
set -euo

until curl 'localhost.localstack.cloud:4566/es/eu-west-2/decs/_cat/indices?v' --silent | grep -q "local-case"; do
   sleep 5
   echo "Waiting for Elasticsearch Index to be present..."
done
