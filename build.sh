#!/bin/bash

DockerCategory=airta
NAME=actionengine
Release=1.0
timestamp=$(date +"%Y%m%d%H.%s")
ImageGlobalTag=${Release}-${timestamp}

./mvnw clean
./mvnw package -DskipTests

docker build --build-arg BUILD_DATE=`date -u +"%Y-%m-%dT%H:%M:%SZ"` \
             --build-arg VCS_REF=`git rev-parse --short HEAD` \
             --build-arg VERSION=${ImageGlobalTag} \
             -t ${DockerCategory}/${NAME}:${ImageGlobalTag} \
             -f Dockerfile . || exit 1
#docker build -t ${DockerCategory}/${NAME}:${ImageGlobalTag} -f Dockerfile . || exit 1
docker tag ${DockerCategory}/${NAME}:${ImageGlobalTag} ${DockerCategory}/${NAME}:latest
docker push ${DockerCategory}/${NAME}:${ImageGlobalTag}
docker push ${DockerCategory}/${NAME}:latest