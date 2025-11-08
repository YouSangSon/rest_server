# Kubernetes 배포 가이드 (AWS EKS)

엔터프라이즈급 REST Server를 AWS EKS에 멀티 포드로 배포하는 완벽한 가이드입니다.

---

## 🎯 아키텍처 개요

### 확장성 및 고가용성

```
Internet
    ↓
AWS ALB (Application Load Balancer)
    ↓
┌────────────────────────────────────────┐
│  Kubernetes Cluster (EKS)              │
│                                        │
│  ┌──────────────────────────────────┐ │
│  │  REST Server Pods (3~50개)       │ │
│  │  - Auto-scaling (HPA)            │ │
│  │  - Virtual Threads               │ │
│  │  - Health Checks                 │ │
│  └──────────────────────────────────┘ │
│                                        │
│  ┌──────────────────────────────────┐ │
│  │  Databases & Cache               │ │
│  │  - RDS PostgreSQL (Multi-AZ)     │ │
│  │  - ElastiCache Redis (Cluster)   │ │
│  │  - DocumentDB (MongoDB)          │ │
│  │  - MSK (Kafka)                   │ │
│  └──────────────────────────────────┘ │
└────────────────────────────────────────┘
         ↓
    Monitoring
- Prometheus
- Grafana
- CloudWatch
```

---

## 📋 사전 준비

### 1. 필수 도구 설치

```bash
# AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# eksctl
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin

# Helm
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# kustomize
curl -s "https://raw.githubusercontent.com/kubernetes-sigs/kustomize/master/hack/install_kustomize.sh"  | bash
sudo mv kustomize /usr/local/bin/
```

### 2. AWS 인증 설정

```bash
aws configure
# AWS Access Key ID:
# AWS Secret Access Key:
# Default region name: ap-northeast-2
# Default output format: json
```

---

## 🚀 1단계: EKS 클러스터 생성

### Option 1: eksctl 사용 (빠름)

```bash
eksctl create cluster \
  --name rest-server-cluster \
  --region ap-northeast-2 \
  --version 1.28 \
  --nodegroup-name general-purpose \
  --node-type t3.xlarge \
  --nodes 3 \
  --nodes-min 3 \
  --nodes-max 10 \
  --managed \
  --with-oidc \
  --ssh-access \
  --ssh-public-key your-key-name
```

### Option 2: Terraform 사용 (권장 - 프로덕션)

```hcl
# terraform/main.tf
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 19.0"

  cluster_name    = "rest-server-cluster"
  cluster_version = "1.28"

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  eks_managed_node_groups = {
    general_purpose = {
      min_size     = 3
      max_size     = 10
      desired_size = 3

      instance_types = ["t3.xlarge"]
      capacity_type  = "ON_DEMAND"
    }
  }
}
```

```bash
cd terraform
terraform init
terraform plan
terraform apply
```

### kubeconfig 설정

```bash
aws eks update-kubeconfig \
  --region ap-northeast-2 \
  --name rest-server-cluster

kubectl get nodes
```

---

## 🔧 2단계: 필수 Add-ons 설치

### 1. AWS Load Balancer Controller

```bash
# IAM Policy 생성
curl -o iam_policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/main/docs/install/iam_policy.json

aws iam create-policy \
  --policy-name AWSLoadBalancerControllerIAMPolicy \
  --policy-document file://iam_policy.json

# Helm으로 설치
helm repo add eks https://aws.github.io/eks-charts
helm repo update

helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=rest-server-cluster \
  --set serviceAccount.create=true \
  --set region=ap-northeast-2 \
  --set vpcId=<VPC_ID>
```

### 2. EBS CSI Driver

```bash
eksctl create iamserviceaccount \
  --name ebs-csi-controller-sa \
  --namespace kube-system \
  --cluster rest-server-cluster \
  --attach-policy-arn arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy \
  --approve

helm repo add aws-ebs-csi-driver https://kubernetes-sigs.github.io/aws-ebs-csi-driver
helm repo update

helm install aws-ebs-csi-driver aws-ebs-csi-driver/aws-ebs-csi-driver \
  --namespace kube-system \
  --set controller.serviceAccount.create=false \
  --set controller.serviceAccount.name=ebs-csi-controller-sa
```

### 3. External Secrets Operator (선택사항)

```bash
helm repo add external-secrets https://charts.external-secrets.io
helm repo update

helm install external-secrets \
  external-secrets/external-secrets \
  -n external-secrets-system \
  --create-namespace
```

### 4. Prometheus + Grafana (모니터링)

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm install prometheus prometheus-community/kube-prometheus-stack \
  -n monitoring \
  --create-namespace \
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false
```

---

## 📦 3단계: AWS 리소스 생성

### 1. ECR Repository

```bash
aws ecr create-repository \
  --repository-name rest-server \
  --region ap-northeast-2

# 출력에서 repositoryUri 확인
# 123456789012.dkr.ecr.ap-northeast-2.amazonaws.com/rest-server
```

### 2. RDS PostgreSQL

```bash
# Terraform 또는 AWS Console 사용
# - Instance: db.t3.large (2 vCPU, 8GB RAM)
# - Storage: GP3 SSD, 100GB
# - Multi-AZ: Yes
# - Backup: 7 days
# - IAM Authentication: Yes
```

### 3. ElastiCache Redis

```bash
# - Node Type: cache.r6g.large
# - Replicas: 2 (Multi-AZ)
# - Cluster Mode: Enabled
# - Encryption: In-transit + At-rest
```

### 4. MSK (Managed Kafka)

```bash
# - Type: Serverless 또는 Provisioned
# - Kafka Version: 3.5.1
# - Brokers: 3 (Multi-AZ)
# - Encryption: TLS
```

### 5. AWS Secrets Manager (자격증명 저장)

```bash
# Database credentials
aws secretsmanager create-secret \
  --name rest-server/db \
  --description "Database credentials" \
  --secret-string '{"username":"postgres","password":"your-secure-password"}' \
  --region ap-northeast-2

# OAuth2 credentials
aws secretsmanager create-secret \
  --name rest-server/oauth2 \
  --secret-string '{
    "google_client_id":"xxx",
    "google_client_secret":"xxx",
    "naver_client_id":"xxx",
    "naver_client_secret":"xxx",
    "kakao_client_id":"xxx",
    "kakao_client_secret":"xxx"
  }'

# JWT secret
aws secretsmanager create-secret \
  --name rest-server/jwt \
  --secret-string '{"secret_key":"your-256-bit-secret-key-here"}'
```

---

## 🐳 4단계: Docker 이미지 빌드 및 푸시

### 이미지 빌드

```bash
# 환경 변수 설정
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export AWS_REGION=ap-northeast-2
export VERSION=v1.0.0

# 빌드 스크립트 실행
./scripts/build-and-push.sh $VERSION
```

또는 수동으로:

```bash
# ECR 로그인
aws ecr get-login-password --region ap-northeast-2 | \
  docker login --username AWS --password-stdin \
  ${AWS_ACCOUNT_ID}.dkr.ecr.ap-northeast-2.amazonaws.com

# 빌드
docker build -t rest-server:${VERSION} .

# 태그
docker tag rest-server:${VERSION} \
  ${AWS_ACCOUNT_ID}.dkr.ecr.ap-northeast-2.amazonaws.com/rest-server:${VERSION}

# 푸시
docker push ${AWS_ACCOUNT_ID}.dkr.ecr.ap-northeast-2.amazonaws.com/rest-server:${VERSION}
```

---

## ☸️ 5단계: Kubernetes 배포

### 1. ConfigMap 및 Secret 업데이트

```bash
cd k8s/base

# ConfigMap 수정 (DB 엔드포인트 등)
vim configmap.yaml

# Secret 수정 (External Secrets 사용하지 않는 경우)
vim secret.yaml
```

### 2. 배포 (Kustomize)

#### Development 환경

```bash
kubectl apply -k k8s/overlays/dev

# 또는 스크립트 사용
./scripts/deploy.sh dev v1.0.0
```

#### Production 환경

```bash
kubectl apply -k k8s/overlays/prod

# 또는 스크립트 사용
./scripts/deploy.sh prod v1.0.0
```

### 3. 배포 확인

```bash
# 네임스페이스 확인
kubectl get ns

# Pods 확인
kubectl get pods -n rest-server

# Deployment 확인
kubectl get deployment -n rest-server

# Service 확인
kubectl get svc -n rest-server

# Ingress 확인
kubectl get ingress -n rest-server

# HPA 확인
kubectl get hpa -n rest-server

# Logs 확인
kubectl logs -f deployment/rest-server -n rest-server
```

---

## 📊 6단계: 모니터링 설정

### 1. ServiceMonitor 배포

```bash
kubectl apply -f k8s/monitoring/servicemonitor.yaml
```

### 2. Grafana 대시보드 접속

```bash
# Grafana 포트 포워딩
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80

# 브라우저에서 접속: http://localhost:3000
# 기본 계정: admin / prom-operator
```

### 3. 대시보드 Import

- Spring Boot 2.x Dashboard: ID `11378`
- JVM Dashboard: ID `4701`
- Kubernetes Cluster Dashboard: ID `7249`

---

## 🔄 7단계: Auto-Scaling 동작 확인

### HPA 테스트

```bash
# 부하 생성 (별도 터미널)
kubectl run -i --tty load-generator --rm --image=busybox --restart=Never -- /bin/sh -c "while sleep 0.01; do wget -q -O- http://rest-server:8080/actuator/health; done"

# HPA 상태 확인
kubectl get hpa -n rest-server -w

# Pod 자동 증가 확인
kubectl get pods -n rest-server -w
```

### Cluster Autoscaler 테스트

```bash
# Pod 수를 노드 용량 초과로 증가
kubectl scale deployment rest-server --replicas=50 -n rest-server

# 노드 자동 추가 확인
kubectl get nodes -w
```

---

## 🛠️ 운영 명령어

### 스케일링

```bash
# 수동 스케일링
./scripts/scale.sh prod 10  # 10개 포드로 스케일

# 또는
kubectl scale deployment rest-server --replicas=10 -n rest-server
```

### 롤백

```bash
# 이전 버전으로 롤백
./scripts/rollback.sh prod

# 특정 리비전으로 롤백
kubectl rollout undo deployment/rest-server --to-revision=3 -n rest-server
```

### 재시작

```bash
# 롤링 재시작
kubectl rollout restart deployment/rest-server -n rest-server
```

### 로그 확인

```bash
# 실시간 로그
kubectl logs -f deployment/rest-server -n rest-server

# 최근 100줄
kubectl logs --tail=100 deployment/rest-server -n rest-server

# 특정 Pod 로그
kubectl logs -f <pod-name> -n rest-server
```

### Shell 접속

```bash
kubectl exec -it deployment/rest-server -n rest-server -- /bin/sh
```

---

## 🔒 보안 Best Practices

### 1. Network Policy 적용

```bash
kubectl apply -f k8s/base/networkpolicy.yaml
```

### 2. Pod Security Standards

```bash
kubectl label namespace rest-server \
  pod-security.kubernetes.io/enforce=restricted
```

### 3. Secrets 암호화

- External Secrets Operator 사용
- AWS Secrets Manager 통합
- KMS로 암호화

### 4. RBAC 설정

```bash
kubectl apply -f k8s/base/rbac.yaml
```

---

## 📈 성능 최적화

### 1. Virtual Threads (Java 21)
- 이미 활성화됨 (`spring.threads.virtual.enabled=true`)
- 수천 개 동시 연결 처리 가능

### 2. Resource Requests/Limits
```yaml
resources:
  requests:
    cpu: "500m"
    memory: "1Gi"
  limits:
    cpu: "2000m"
    memory: "2Gi"
```

### 3. Connection Pooling
- HikariCP: 50 connections
- Redis Pool: 20 connections
- Kafka: 다중 브로커 연결

---

## 🚨 트러블슈팅

### Pod가 시작하지 않을 때

```bash
# 이벤트 확인
kubectl describe pod <pod-name> -n rest-server

# 로그 확인
kubectl logs <pod-name> -n rest-server --previous

# Init Container 로그
kubectl logs <pod-name> -c wait-for-postgres -n rest-server
```

### Readiness Probe 실패

```bash
# Health 엔드포인트 직접 확인
kubectl exec -it <pod-name> -n rest-server -- curl http://localhost:8080/actuator/health
```

### DB 연결 실패

```bash
# DNS 확인
kubectl exec -it <pod-name> -n rest-server -- nslookup <rds-endpoint>

# 네트워크 확인
kubectl exec -it <pod-name> -n rest-server -- nc -zv <rds-endpoint> 5432
```

---

## 📚 참고 자료

- [AWS EKS Best Practices](https://aws.github.io/aws-eks-best-practices/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Spring Boot on Kubernetes](https://spring.io/guides/gs/spring-boot-kubernetes/)
- [Java 21 Virtual Threads](https://openjdk.org/jeps/444)

---

## 🎯 배포 체크리스트

- [ ] EKS 클러스터 생성
- [ ] Add-ons 설치 (ALB Controller, EBS CSI, etc.)
- [ ] AWS 리소스 생성 (RDS, ElastiCache, MSK)
- [ ] ECR에 이미지 푸시
- [ ] ConfigMap/Secret 설정
- [ ] Kubernetes 배포
- [ ] Health Check 확인
- [ ] Ingress 설정 및 도메인 연결
- [ ] SSL/TLS 인증서 설정
- [ ] HPA 동작 확인
- [ ] 모니터링 대시보드 설정
- [ ] 알람 설정
- [ ] 백업 전략 수립

---

**배포 완료!** 🎉

멀티 포드 환경에서 엔터프라이즈급 REST API가 실행 중입니다.
