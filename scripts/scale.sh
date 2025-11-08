#!/bin/bash
# Kubernetes Scaling 스크립트

set -e

ENVIRONMENT=${1:-"dev"}
REPLICAS=${2:-3}
NAMESPACE="rest-server-${ENVIRONMENT}"

if [ "$ENVIRONMENT" == "prod" ]; then
    NAMESPACE="rest-server"
fi

echo "========================================="
echo "Scaling deployment"
echo "========================================="
echo "Environment: ${ENVIRONMENT}"
echo "Namespace: ${NAMESPACE}"
echo "Replicas: ${REPLICAS}"
echo "========================================="

# 현재 상태
echo "Current status:"
kubectl get deployment rest-server -n ${NAMESPACE}

# Scale
echo "Scaling to ${REPLICAS} replicas..."
kubectl scale deployment rest-server --replicas=${REPLICAS} -n ${NAMESPACE}

# 대기
echo "Waiting for scaling to complete..."
kubectl wait --for=condition=available --timeout=300s \
    deployment/rest-server -n ${NAMESPACE}

# 결과 확인
echo "New status:"
kubectl get deployment rest-server -n ${NAMESPACE}
kubectl get pods -n ${NAMESPACE} -l app=rest-server

echo "========================================="
echo "✅ Scaling completed successfully!"
echo "========================================="
