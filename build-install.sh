#!/usr/bin/env bash

./mvnw package -Pnative -Dquarkus.native.container-build=true
mkdir -p  ~/bin
rm -f ~/bin/svg-font-embedding
cp target/svg-font-embedding-runner target/svg-font-embedding
cp target/svg-font-embedding-runner ~/bin/svg-font-embedding