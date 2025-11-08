#!/bin/bash
# Kubernetes 배포 스크립트

set -e

# Variables
ENVIRONMENT=${1:-"dev"}
VERSION=${2:-"latest"}
NAMESPACE="rest-server"

if [ "$ENVIRONMENT" == "prod" ]; then
    NAMESPACE="rest-server"
else
    NAMESPACE="rest-server-${ENVIRONMENT}"
fi

echo "========================================="
echo "Deploying to Kubernetes"
echo "========================================="
echo "Environment: ${ENVIRONMENT}"
echo "Version: ${VERSION}"
echo "Namespace: ${NAMESPACE}"
echo "========================================="

# kubectl 클러스터 연결 확인
echo "Checking kubectl connection..."
kubectl cluster-info

# Kustomize로 배포
echo "Deploying with Kustomize..."
kubectl apply -k k8s/overlays/${ENVIRONMENT}

# 이미지 태그 업데이트
echo "Updating image tag to ${VERSION}..."
kubectl set image deployment/rest-server \
    rest-server=${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/rest-server:${VERSION} \
    -n ${NAMESPACE}

# Rollout 상태 확인
echo "Waiting for rollout to complete..."
kubectl rollout status deployment/rest-server -n ${NAMESPACE} --timeout=5m

# Pod 상태 확인
echo "Checking pod status..."
kubectl get pods -n ${NAMESPACE} -l app=rest-server

# Service 확인
echo "Checking service..."
kubectl get svc -n ${NAMESPACE}

# Ingress 확인
echo "Checking ingress..."
kubectl get ingress -n ${NAMESPACE}

echo "========================================="
echo "✅ Deployment completed successfully!"
echo "========================================="

# 배포 후 확인
echo "Recent events:"
kubectl get events -n ${NAMESPACE} --sort-by='.lastTimestamp' | tail -10

echo "Done!"
