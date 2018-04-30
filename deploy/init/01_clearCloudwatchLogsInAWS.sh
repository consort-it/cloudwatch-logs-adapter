#!/bin/bash
# $1 ENV_PATH               .env relative path for docker
# $2 DEPLOYMENT_TARGET      like 'dev' or 'live' | can be used to identify scopes or namespaces
# $3 MICROSERVICE_NAME      name of microservice

ENV="${1?Need to set ENV_PATH}"
TARGET="${2?Need to set DEPLOYMENT_TARGET}"
NAME="${3?Need to set MICROSERVICE_NAME}"

NAMESPACE="eu-central-1"
[ "$TARGET" == "dev" ] && NAMESPACE="${CON_IT_AWS_DEV_REGION?Need to set env CON_IT_AWS_DEV_REGION}"
[ "$TARGET" == "live" ] && NAMESPACE="${CON_IT_AWS_LIVE_REGION?Need to set env CON_IT_AWS_LIVE_REGION}"

: "${NAMESPACE?Need to set CON_IT_AWS_XXX_REGION according DEPLOYMENT_TARGET (2nd Argument)}"

LOG_GROUP="TEST-adapter-v1"
LOG_STREAM="TEST-adapter-$RANDOM"

#AWS_ACCESS_KEY_ID=${CON_IT_AWS_CLOUDWATCH_ACCESS?Need to set env CON_IT_AWS_CLOUDWATCH_ACCESS} AWS_SECRET_ACCESS_KEY=${CON_IT_AWS_CLOUDWATCH_SECRET?Need to set env CON_IT_AWS_CLOUDWATCH_SECRET} PATH=/var/jenkins_home/bin:/var/jenkins_home/.local/bin:$PATH aws --region $NAMESPACE logs delete-log-stream --log-group-name "${LOG_GROUP}" --log-stream-name "${LOG_STREAM}"
#AWS_ACCESS_KEY_ID=${CON_IT_AWS_CLOUDWATCH_ACCESS?Need to set env CON_IT_AWS_CLOUDWATCH_ACCESS} AWS_SECRET_ACCESS_KEY=${CON_IT_AWS_CLOUDWATCH_SECRET?Need to set env CON_IT_AWS_CLOUDWATCH_SECRET} PATH=/var/jenkins_home/bin:/var/jenkins_home/.local/bin:$PATH aws --region $NAMESPACE logs delete-log-group --log-group-name "${LOG_GROUP}"
#AWS_ACCESS_KEY_ID=${CON_IT_AWS_CLOUDWATCH_ACCESS?Need to set env CON_IT_AWS_CLOUDWATCH_ACCESS} AWS_SECRET_ACCESS_KEY=${CON_IT_AWS_CLOUDWATCH_SECRET?Need to set env CON_IT_AWS_CLOUDWATCH_SECRET} PATH=/var/jenkins_home/bin:/var/jenkins_home/.local/bin:$PATH aws --region $NAMESPACE logs create-log-group --log-group-name "${LOG_GROUP}"
AWS_ACCESS_KEY_ID=${CON_IT_AWS_CLOUDWATCH_ACCESS?Need to set env CON_IT_AWS_CLOUDWATCH_ACCESS} AWS_SECRET_ACCESS_KEY=${CON_IT_AWS_CLOUDWATCH_SECRET?Need to set env CON_IT_AWS_CLOUDWATCH_SECRET} PATH=/var/jenkins_home/bin:/var/jenkins_home/.local/bin:$PATH aws --region $NAMESPACE logs create-log-stream --log-group-name "${LOG_GROUP}" --log-stream-name "${LOG_STREAM}"
sleep 2
AWS_ACCESS_KEY_ID=${CON_IT_AWS_CLOUDWATCH_ACCESS?Need to set env CON_IT_AWS_CLOUDWATCH_ACCESS} AWS_SECRET_ACCESS_KEY=${CON_IT_AWS_CLOUDWATCH_SECRET?Need to set env CON_IT_AWS_CLOUDWATCH_SECRET} PATH=/var/jenkins_home/bin:/var/jenkins_home/.local/bin:$PATH aws --region $NAMESPACE logs put-log-events --log-group-name "${LOG_GROUP}" --log-stream-name "${LOG_STREAM}" --log-events "[{\"timestamp\": $(date +%s)000, \"message\": \"{ \\\"log\\\": \\\"0 [main] INFO de.consortit.none.TestAdapter - TestAdapter is being deployed!\n\\\", \\\"stream\\\": \\\"stdin\\\", \\\"docker\\\": { \\\"container_id\\\": \\\"NA\\\" }, \\\"kubernetes\\\": { \\\"container_name\\\": \\\"TEST-adapter-v1\\\", \\\"namespace_name\\\": \\\"default\\\", \\\"pod_name\\\": \\\"TEST-adapter-v1-NA\\\", \\\"pod_id\\\": \\\"NA\\\", \\\"labels\\\": { \\\"app\\\": \\\"TEST-adapter-v1\\\", \\\"pod-template-hash\\\": \\\"NA\\\" }, \\\"host\\\": \\\"NA\\\", \\\"master_url\\\": \\\"NA\\\" }, \\\"pod_name\\\": \\\"TEST-adapter-v1-NA\\\", \\\"container_name\\\": \\\"TEST-adapter-v1\\\" }\"}]"
echo "AWS_ACCESS_KEY_ID=${CON_IT_AWS_CLOUDWATCH_ACCESS?Need to set env CON_IT_AWS_CLOUDWATCH_ACCESS} AWS_SECRET_ACCESS_KEY=${CON_IT_AWS_CLOUDWATCH_SECRET?Need to set env CON_IT_AWS_CLOUDWATCH_SECRET} PATH=/var/jenkins_home/bin:/var/jenkins_home/.local/bin:$PATH aws --region $NAMESPACE logs put-log-events --log-group-name \"${LOG_GROUP}\" --log-stream-name \"${LOG_STREAM}\" --log-events \"[{\\\"timestamp\\\": $(date +%s), \\\"message\\\": '{ \\\"log\\\": \\\"0 [main] INFO de.consortit.none.TestAdapter - TestAdapter is being deployed!\n\\\", \\\"stream\\\": \\\"stdin\\\", \\\"docker\\\": { \\\"container_id\\\": \\\"NA\\\" }, \\\"kubernetes\\\": { \\\"container_name\\\": \\\"TEST-adapter-v1\\\", \\\"namespace_name\\\": \\\"default\\\", \\\"pod_name\\\": \\\"TEST-adapter-v1-NA\\\", \\\"pod_id\\\": \\\"NA\\\", \\\"labels\\\": { \\\"app\\\": \\\"TEST-adapter-v1\\\", \\\"pod-template-hash\\\": \\\"NA\\\" }, \\\"host\\\": \\\"NA\\\", \\\"master_url\\\": \\\"NA\\\" }, \\\"pod_name\\\": \\\"TEST-adapter-v1-NA\\\", \\\"container_name\\\": \\\"TEST-adapter-v1\\\" }'}]\""

echo "CON_IT_TEST_LOG_GROUP=${LOG_GROUP}">>${ENV}
export CON_IT_TEST_LOG_GROUP=$LOG_GROUP
echo "CON_IT_TEST_LOG_STREAM=${LOG_STREAM}">>${ENV}
export CON_IT_TEST_LOG_STREAM=$LOG_STREAM

