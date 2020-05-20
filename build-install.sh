#!/usr/bin/env bash

./mvnw package -Pnative -Dquarkus.native.container-build=true
mkdir -p  ~/bin
cp target/svg-font-embedding-1.0.0-runner ~/bin/svg-font-embedding