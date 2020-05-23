#!/usr/bin/env bash

./mvnw clean
./mvnw package -Dquarkus.package.uber-jar=true
./mvnw package -Pnative -Dquarkus.native.container-build=true

mv target/svg-buddy-runner target/svg-buddy
mv target/svg-buddy-runner.jar target/svg-buddy.jar