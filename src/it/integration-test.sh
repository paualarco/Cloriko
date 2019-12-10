#!/usr/bin/env bash

set -e

CURRENT_DIR=$(pwd)
echo "CURRENT_DIR=$CURRENT_DIR"
export APPLICATION_VERSION=$(cat $CURRENT_DIR/VERSION)

echo "Docker bind IP address: $TEST_SERVICES_HOST"

docker-compose -f ./src/integration-test/docker-compose.yml stop cloriko-service
docker-compose -f ./src/integration-test/docker-compose.yml rm -f cloriko-service

echo -e "Docker ps."
docker ps

echo -e "Building docker image.."

sbt clean docker:publishLocal

docker-compose -f ./src/integration-test/docker-compose.yml up -d cloriko-service

sleep 2

sbt clean it:test
