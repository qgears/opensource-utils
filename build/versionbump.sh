#!/bin/bash

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &> /dev/null && pwd)"
PARENT_DIR="$(dirname -- "$SCRIPT_DIR")"

#-Dartifacts=rtemplate,hu.rtemplate,rtemplate-parent \
function error {
    echo $@
    exit 1
}


mvn -f ${PARENT_DIR}  \
-Dartifacts=opensource-utils,opensource-utils-parent \
org.eclipse.tycho:tycho-versions-plugin:5.0.3:set-version \
-DnewVersion=$1 \
-Dversionbump || error "Failed bumping pom versions"

mvn -f ${PARENT_DIR} process-classes -Dregenmanifest  || error "Failed update Manifests"

