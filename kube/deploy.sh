#!/bin/bash
set -euo pipefail

export KUBE_NAMESPACE=${ENVIRONMENT}
export KUBE_SERVER=${KUBE_SERVER}
export KUBE_TOKEN=${KUBE_TOKEN}
export VERSION=${VERSION}

if [[ ${KUBE_NAMESPACE} == *prod ]]
then
    export MIN_REPLICAS="2"
    export MAX_REPLICAS="3"
    export CLUSTER_NAME="acp-prod"

    export UPTIME_PERIOD="Mon-Sun 05:00-23:00 Europe/London"
else
    export MIN_REPLICAS="1"
    export MAX_REPLICAS="2"
    export CLUSTER_NAME="acp-notprod"

    export UPTIME_PERIOD="Mon-Fri 08:00-18:00 Europe/London"
fi

export KUBE_CERTIFICATE_AUTHORITY="https://raw.githubusercontent.com/UKHomeOffice/acp-ca/master/${CLUSTER_NAME}.crt"

cd kd

kd \
   --timeout 10m \
    -f deployment.yaml \
    -f service.yaml \
    -f autoscale.yaml
