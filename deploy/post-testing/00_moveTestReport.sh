#!/bin/bash
# $1 ENV_PATH               .env relative path for docker
# $2 DEPLOYMENT_TARGET      like 'dev' or 'live' | can be used to identify scopes or namespaces
# $3 MICROSERVICE_NAME      name of microservice

ENV="${1?Need to set ENV_PATH}"
TARGET="${2?Need to set DEPLOYMENT_TARGET}"
NAME="${3?Need to set MICROSERVICE_NAME}"

#cp -R ./cloudwatch-logs-adapter/build/reports/* ./testresult
