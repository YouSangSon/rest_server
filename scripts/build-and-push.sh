#!/bin/bash
# Docker 이미지 빌드 및 푸시 스크립트

set -e  # Exit on error

# Variables
APP_NAME="rest-server"
VERSION=${1:-latest}
AWS_ACCOUNT_ID=${AWS_ACCOUNT_ID:-"123456789012"}
AWS_REGION=${AWS_REGION:-"ap-northeast-2"}
ECR_REPO="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${APP_NAME}"

echo "========================================="
echo "Building and Pushing Docker Image"
echo "========================================="
echo "App: ${APP_NAME}"
echo "Version: ${VERSION}"
echo "ECR Repo: ${ECR_REPO}"
echo "========================================="

# ECR 로그인
echo "Logging in to Amazon ECR..."
aws ecr get-login-password --region ${AWS_REGION} | \
    docker login --username AWS --password-stdin ${ECR_REPO}

# Docker 이미지 빌드
echo "Building Docker image..."
docker build \
    --platform linux/amd64 \
    -t ${APP_NAME}:${VERSION} \
    -t ${APP_NAME}:latest \
    -t ${ECR_REPO}:${VERSION} \
    -t ${ECR_REPO}:latest \
    --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
    --build-arg VCS_REF=$(git rev-parse --short HEAD) \
    .

# 이미지 푸시
echo "Pushing image to ECR..."
docker push ${ECR_REPO}:${VERSION}
docker push ${ECR_REPO}:latest

echo "========================================="
echo "✅ Build and push completed successfully!"
echo "Image: ${ECR_REPO}:${VERSION}"
echo "========================================="

# 이미지 스캔 (선택사항)
echo "Scanning image for vulnerabilities..."
aws ecr start-image-scan \
    --repository-name ${APP_NAME} \
    --image-id imageTag=${VERSION} \
    --region ${AWS_REGION} || true

echo "Done!"
