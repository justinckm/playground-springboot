#!/usr/bin/env bash
set -euo pipefail

function cleanup() {
    docker-compose -f docker/common.yml down
}
trap cleanup EXIT

docker-compose -f docker/common.yml up