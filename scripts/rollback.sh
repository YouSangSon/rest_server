#!/bin/bash
# Kubernetes Rollback 스크립트

set -e

ENVIRONMENT=${1:-"dev"}
NAMESPACE="rest-server-${ENVIRONMENT}"

if [ "$ENVIRONMENT" == "prod" ]; then
    NAMESPACE="rest-server"
fi

echo "========================================="
echo "Rolling back deployment"
echo "========================================="
echo "Environment: ${ENVIRONMENT}"
echo "Namespace: ${NAMESPACE}"
echo "========================================="

# Rollout history 확인
echo "Deployment history:"
kubectl rollout history deployment/rest-server -n ${NAMESPACE}

# Rollback 실행
echo "Rolling back to previous revision..."
kubectl rollout undo deployment/rest-server -n ${NAMESPACE}

# Rollout 상태 확인
echo "Waiting for rollback to complete..."
kubectl rollout status deployment/rest-server -n ${NAMESPACE} --timeout=5m

# Pod 상태 확인
echo "Checking pod status after rollback..."
kubectl get pods -n ${NAMESPACE} -l app=rest-server

echo "========================================="
echo "✅ Rollback completed successfully!"
echo "========================================="
