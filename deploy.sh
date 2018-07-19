#!/bin/bash

export KUBE_NAMESPACE=${KUBE_NAMESPACE}
export KUBE_SERVER=${KUBE_SERVER}

if [[ -z ${VERSION} ]] ; then
    export VERSION=${IMAGE_VERSION}
fi

if [[ ${ENVIRONMENT} == "prod" ]] ; then
    echo "deploy ${VERSION} to prod namespace, using HOCS_WORKFLOW_PR drone secret"
    export KUBE_TOKEN=${HOCS_WORKFLOW_PROD}
    export REPLICAS="2"
else
    if [[ ${ENVIRONMENT} == "qa" ]] ; then
        echo "deploying ${VERSION} to test namespace, using HOCS_WORKFLOW_QA drone secret"
        export KUBE_TOKEN=${HOCS_WORKFLOW_QA}
        export REPLICAS="2"
    else
        echo "deploy ${VERSION} to dev namespace, using HOCS_WORKFLOW_DEV drone secret"
        export KUBE_TOKEN=${HOCS_WORKFLOW_DEV}
        export REPLICAS="1"
    fi
    
fi

if [[ -z ${KUBE_TOKEN} ]] ; then
    echo "Failed to find a value for KUBE_TOKEN - exiting"
    exit -1
fi

cd kd

kd --insecure-skip-tls-verify \
   --timeout 10m \
    -f deployment.yaml \
    -f service.yaml
