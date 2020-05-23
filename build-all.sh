#!/usr/bin/env bash

./mvnw clean
./mvnw package -Dquarkus.package.uber-jar=true
./mvnw package -Pnative -Dquarkus.native.container-build=true