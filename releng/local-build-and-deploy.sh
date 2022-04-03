#!/bin/bash
set -euo pipefail

# Script meant to run from repo root (not from the releng dir)

export JAVA_HOME=/home/kdvolder/Applications/jdk1.8.0_271
./mvnw clean package
mvn --settings releng/settings.xml -Dmaven.test.skip=true deploy