#!/bin/bash

export KUBE_NAMESPACE=${ENVIRONMENT}
export KUBE_SERVER=${KUBE_SERVER}

if [[ -z ${VERSION} ]] ; then
    export VERSION=${IMAGE_VERSION}
fi

if [[ ${KUBE_NAMESPACE} == "cs-prod" ]] ; then
    echo "deploy ${VERSION} to PROD namespace, using HOCS_WORKFLOW_CS_PROD drone secret"
    export KUBE_TOKEN=${HOCS_WORKFLOW_CS_PROD}
    export REPLICAS="2"
elif [[ ${KUBE_NAMESPACE} == "wcs-prod" ]] ; then
    echo "deploy ${VERSION} to PROD namespace, using HOCS_WORKFLOW_WCS_PROD drone secret"
    export KUBE_TOKEN=${HOCS_WORKFLOW_WCS_PROD}
    export REPLICAS="2"
elif [[ ${KUBE_NAMESPACE} == "cs-qa" ]] ; then
    echo "deploying ${VERSION} to QA namespace, using HOCS_WORKFLOW_CS_QA drone secret"
    export KUBE_TOKEN=${HOCS_WORKFLOW_CS_QA}
    export REPLICAS="2"
elif [[ ${KUBE_NAMESPACE} == "wcs-qa" ]] ; then
    echo "deploying ${VERSION} to QA namespace, using HOCS_WORKFLOW_WCS_QA drone secret"
    export KUBE_TOKEN=${HOCS_WORKFLOW_WCS_QA}
    export REPLICAS="2"
elif [[ ${KUBE_NAMESPACE} == "cs-demo" ]] ; then
    echo "deploy ${VERSION} to DEMO namespace, using HOCS_WORKFLOW_CS_DEMO drone secret"
    export KUBE_TOKEN=${HOCS_WORKFLOW_CS_DEMO}
    export REPLICAS="1"
elif [[ ${KUBE_NAMESPACE} == "wcs-demo" ]] ; then
    echo "deploy ${VERSION} to DEMO namespace, using HOCS_WORKFLOW_WCS_DEMO drone secret"
    export KUBE_TOKEN=${HOCS_WORKFLOW_WCS_DEMO}
    export REPLICAS="1"
elif [[ ${KUBE_NAMESPACE} == "cs-dev" ]] ; then
    echo "deploy ${VERSION} to DEV namespace, using HOCS_WORKFLOW_CS_DEV drone secret"
    export KUBE_TOKEN=${HOCS_WORKFLOW_CS_DEV}
    export REPLICAS="1"
elif [[ ${KUBE_NAMESPACE} == "wcs-dev" ]] ; then
    echo "deploy ${VERSION} to DEV namespace, using HOCS_WORKFLOW_WCS_DEV drone secret"
    export KUBE_TOKEN=${HOCS_WORKFLOW_WCS_DEV}
    export REPLICAS="1"
else
    echo "Unable to find environment: ${ENVIRONMENT}"
fi

if [[ -z ${KUBE_TOKEN} ]] ; then
    echo "Failed to find a value for KUBE_TOKEN - exiting"
    exit -1
fi

cd kd

kd --insecure-skip-tls-verify \
   --timeout 15m \
    -f deployment.yaml \
    -f service.yaml \
    -f autoscale.yaml
