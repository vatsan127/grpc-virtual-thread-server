#!/usr/bin/env bash
set -euo pipefail

IMAGE="grpc-virtual-thread-server"
K8S_MANIFEST="k8s/deployment.yaml"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"

cd "$(dirname "$0")"

echo "==> git reset --hard"
git reset --hard

echo "==> git pull origin master"
git pull origin master

echo "==> retag existing ${IMAGE}:latest -> ${IMAGE}:${TIMESTAMP}"
if docker image inspect "${IMAGE}:latest" >/dev/null 2>&1; then
  docker tag "${IMAGE}:latest" "${IMAGE}:${TIMESTAMP}"
  echo "    preserved previous latest as ${IMAGE}:${TIMESTAMP}"
else
  echo "    no existing ${IMAGE}:latest, skipping retag"
fi

echo "==> docker build ${IMAGE}:latest"
docker build -t "${IMAGE}:latest" .

echo "==> kubectl apply -f ${K8S_MANIFEST}"
kubectl apply -f "${K8S_MANIFEST}"

echo "==> done. images:"
docker images "${IMAGE}"
