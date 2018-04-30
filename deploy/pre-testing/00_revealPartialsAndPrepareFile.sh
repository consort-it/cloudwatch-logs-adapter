#!/bin/bash
# $1 ENV_PATH               .env relative path for docker
# $2 DEPLOYMENT_TARGET      like 'dev' or 'live' | can be used to identify scopes or namespaces
# $3 MICROSERVICE_NAME      name of microservice

ENV="${1?Need to set ENV_PATH}"
TARGET="${2?Need to set DEPLOYMENT_TARGET}"
NAME="${3?Need to set MICROSERVICE_NAME}"

# Following line should work if envsubset is made available
#find ./${NAME} -type f -wholename '*/resources/*.part.*' -exec bash -c "envsubst < {} > \$(echo {} | sed 's/.part//g') && cat \$(echo {} | sed 's/.part//g') && rm {}"  \;

while read -r partialFile
do
    echo "Partial: $partialFile"
    FILE=$(<"$partialFile")
    while read -r envRow
    do
        KEY=$(echo $envRow | cut -d'=' -f1)
        VALUE=${envRow#*=}
        FILE=${FILE//\$$KEY/$VALUE}
    done < <(printenv)
    echo "$FILE" > "$(echo $partialFile | sed 's/.part//g')"
    rm $partialFile
done < <(find ./${NAME} -type f -wholename '*/*.part.*')

