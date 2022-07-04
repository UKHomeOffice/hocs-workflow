#!/bin/bash
set -euo

export AWS_ACCESS_KEY_ID=UNSET
export AWS_SECRET_ACCESS_KEY=UNSET
export AWS_DEFAULT_REGION=eu-west-2

## make sure that localstack is running in the pipeline
until curl http://localstack:4566/health --silent | grep -q "\"sqs\": \"available\""; do
   sleep 5
   echo "Waiting for LocalStack to be ready..."
done

awslocal sqs create-queue --queue-name search-queue-dlq
awslocal sqs create-queue --queue-name search-queue --attributes '{"RedrivePolicy": "{\"deadLetterTargetArn\":\"arn:aws:sqs:eu-west-2:000000000000:search-queue-dlq\",\"maxReceiveCount\":2}"}'
