#!/bin/bash

# 색상 설정
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# .env 파일 존재 여부 확인 및 로드
if [ -f ".env" ]; then
  source .env
  echo -e "${GREEN}[✓] .env 파일을 로드했습니다.${NC}"
else
  echo -e "${YELLOW}[!] .env 파일이 없습니다. 기본값과 시스템 환경 변수를 사용합니다.${NC}"
  echo -e "${YELLOW}    참고: .env.example 파일을 복사하여 .env 파일을 생성하는 것을 권장합니다.${NC}"
fi

# 필수 환경 변수 확인
check_env_var() {
  local var_name=$1
  local var_value=${!var_name}
  local default_value=$2
  
  if [ -z "$var_value" ]; then
    if [ -n "$default_value" ]; then
      echo -e "${YELLOW}[!] $var_name 환경 변수가 설정되지 않았습니다. 기본값 '$default_value'를 사용합니다.${NC}"
      export $var_name="$default_value"
    else
      echo -e "${RED}[✗] 필수 환경 변수 $var_name가 설정되지 않았습니다.${NC}"
      return 1
    fi
  else
    echo -e "${GREEN}[✓] $var_name = $var_value${NC}"
  fi
  return 0
}

# 레지스트리 설정 - 환경 변수 우선, 없으면 기본값 사용
check_env_var "DOCKER_REGISTRY_URL" "localhost:5000"
REGISTRY=${DOCKER_REGISTRY_URL}
IMAGE_NAME="rest-server"

# 데이터베이스 설정 검증
echo -e "\n${BLUE}=== 데이터베이스 설정 검증 ===${NC}"
check_env_var "DB_URL" "jdbc:postgresql://localhost:5432/postgres"
check_env_var "DB_USERNAME" "postgres"
check_env_var "DB_PASSWORD" "postgres"

# JVM 설정 검증
echo -e "\n${BLUE}=== JVM 설정 검증 ===${NC}"
check_env_var "JVM_XMS" "512m"
check_env_var "JVM_XMX" "1g"
check_env_var "JVM_MAX_RAM_PERCENTAGE" "75"

# 기본 버전 설정
DEFAULT_VERSION="latest"

echo -e "\n${BLUE}=== 빌드 정보 ===${NC}"
echo -e "Target Registry: ${GREEN}$REGISTRY${NC}"
echo -e "Image Name: ${GREEN}$IMAGE_NAME${NC}"

# 현재 리포지토리 버전 확인 (git 사용)
if [ -d ".git" ]; then
  GIT_VERSION=$(git describe --tags --always 2>/dev/null || echo "unknown")
  echo -e "Current repository version: ${GREEN}$GIT_VERSION${NC}"
else
  GIT_VERSION="unknown"
  echo -e "Git repository not found. Cannot determine version automatically."
fi

# 레지스트리에서 현재 존재하는 이미지 태그 가져오기
echo -e "\n${BLUE}=== 레지스트리 이미지 확인 ===${NC}"
echo "Fetching existing versions from registry..."
TAGS=$(curl -s http://$REGISTRY/v2/$IMAGE_NAME/tags/list 2>/dev/null | grep -o '"tags":\[[^]]*\]' | sed 's/"tags":\[//g' | sed 's/\]//g' | sed 's/"//g' | sed 's/,/\n/g' | sort -V)

if [ -n "$TAGS" ]; then
  echo -e "${GREEN}Available versions in registry:${NC}"
  echo "$TAGS" | nl
else
  echo -e "${YELLOW}No versions found in registry or unable to connect to registry.${NC}"
fi

# 사용자에게 버전 입력 요청
echo -e "\n${BLUE}=== 이미지 버전 선택 ===${NC}"
read -p "Enter version (press Enter for '$DEFAULT_VERSION'): " VERSION
VERSION=${VERSION:-$DEFAULT_VERSION}

# 동일한 버전이 이미 존재하는지 확인
if echo "$TAGS" | grep -q "^$VERSION$"; then
  echo -e "${YELLOW}Warning: Version '$VERSION' already exists in the registry.${NC}"
  read -p "Do you want to delete the existing image and replace it? (y/n): " REPLACE
  if [[ "$REPLACE" == "y" || "$REPLACE" == "Y" ]]; then
    echo "Deleting existing image version..."
    
    # 이미지 매니페스트 가져오기
    DIGEST=$(curl -s -H "Accept: application/vnd.docker.distribution.manifest.v2+json" \
      http://$REGISTRY/v2/$IMAGE_NAME/manifests/$VERSION | grep -o '"digest":"[^"]*"' | head -1 | cut -d':' -f3 | tr -d '"')
    
    if [ -n "$DIGEST" ]; then
      # 이미지 삭제
      curl -s -X DELETE http://$REGISTRY/v2/$IMAGE_NAME/manifests/sha256:$DIGEST
      echo -e "${GREEN}Existing image deleted successfully.${NC}"
    else
      echo -e "${YELLOW}Failed to get image digest. Proceeding with build...${NC}"
    fi
  else
    echo -e "${RED}Build cancelled. Please choose a different version.${NC}"
    exit 1
  fi
fi

echo -e "\n${BLUE}====================================${NC}"
echo -e "${BLUE} Building REST Server Docker Image${NC}"
echo -e "${BLUE}====================================${NC}"
echo -e "Target Registry: ${GREEN}$REGISTRY${NC}"
echo -e "Image Name: ${GREEN}$IMAGE_NAME:$VERSION${NC}"

# Spring 설정
SPRING_PROFILES=${SPRING_PROFILES_ACTIVE:-"prod"}

# 빌드 환경 변수 구성
JIB_OPTS=""
add_env_var() {
  local var_name=$1
  local var_value=${!var_name}
  
  if [ -n "$var_value" ]; then
    JIB_OPTS="$JIB_OPTS -Djib.container.environment.$var_name=\"$var_value\""
  fi
}

# 모든 .env 파일의 환경 변수를 컨테이너에 전달
add_env_var "SPRING_PROFILES_ACTIVE"
add_env_var "DB_URL"
add_env_var "DB_USERNAME"
add_env_var "DB_PASSWORD"
add_env_var "CORS_ALLOWED_ORIGINS"
add_env_var "LOG_LEVEL"
add_env_var "LOG_FILE_PATH"

# Jib 빌드 실행
echo -e "\n${BLUE}=== 빌드 시작 ===${NC}"
eval "./gradlew jib \
  -Djib.allowInsecureRegistries=true \
  -DsendCredentialsOverHttp=true \
  -Djib.to.image=$REGISTRY/$IMAGE_NAME:$VERSION \
  -Djib.container.environment.SPRING_CONFIG_LOCATION=\"classpath:/,file:/app/config/\" \
  -Djib.container.jvmFlags=\"-Xms$JVM_XMS\",\"-Xmx$JVM_XMX\",\"-XX:+UseContainerSupport\",\"-XX:MaxRAMPercentage=$JVM_MAX_RAM_PERCENTAGE\" \
  $JIB_OPTS \
  -x test"

BUILD_SUCCESS=$?

if [ $BUILD_SUCCESS -eq 0 ]; then
  echo -e "\n${GREEN}====================================${NC}"
  echo -e "${GREEN} Build Completed Successfully${NC}"
  echo -e "${GREEN}====================================${NC}"
  echo -e "Image built and pushed as: ${GREEN}$REGISTRY/$IMAGE_NAME:$VERSION${NC}"
  echo -e "To run the image in Kubernetes:"
  echo -e "${BLUE}kubectl apply -f rest-server.yaml${NC}"
  echo -e "${GREEN}====================================${NC}"

  # 이미지 업데이트 후 Kubernetes에 적용
  if [[ "$REPLACE" == "y" || "$REPLACE" == "Y" ]]; then
    read -p "Do you want to restart the deployment to apply the new image? (y/n): " RESTART
    if [[ "$RESTART" == "y" || "$RESTART" == "Y" ]]; then
      echo "Restarting deployment..."
      kubectl rollout restart statefulset/rest-server
      echo -e "${GREEN}Deployment restarted. The new image will be applied.${NC}"
    fi
  fi
else
  echo -e "\n${RED}====================================${NC}"
  echo -e "${RED} Build Failed${NC}"
  echo -e "${RED}====================================${NC}"
fi 