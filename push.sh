#!/bin/bash

# 이미지 이름
IMAGE_NAME="rest-server"

# 색상 설정
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# .env 파일 존재 여부 확인 및 로드
if [ -f "env/prod.env" ]; then
  source env/prod.env
  echo -e "${GREEN}[✓] prod.env 파일을 로드했습니다.${NC}"
else
  echo -e "${RED}[✗] prod.env 파일이 없습니다. env/prod.env 파일이 필요합니다.${NC}"
  exit 1
fi

# 필수 환경 변수 확인
check_env_var() {
  local var_name=$1
  local var_value=${!var_name}
  local default_value=$2
  
  if [ -z "$var_value" ]; then
    if [ -n "$default_value" ]; then
      echo -e "${YELLOW}[!] $var_name 환경 변수가 설정되지 않았습니다. 기본값 '$default_value'를 사용합니다.${NC}"
      export "$var_name"="$default_value"
    else
      echo -e "${RED}[✗] 필수 환경 변수 $var_name가 설정되지 않았습니다.${NC}"
      return 1
    fi
  else
    echo -e "${GREEN}[✓] $var_name = $var_value${NC}"
  fi
  return 0
}

# MicroK8s 레지스트리 설정 확인
echo -e "\n${BLUE}=== MicroK8s 레지스트리 설정 확인 ===${NC}"
check_env_var "DOCKER_REGISTRY_URL" ""

# 레지스트리 호스트와 포트 추출
REGISTRY_HOST=$(echo $DOCKER_REGISTRY_URL | cut -d ":" -f 1)
REGISTRY_PORT=$(echo $DOCKER_REGISTRY_URL | cut -d ":" -f 2)

# 원격 호스트 연결 테스트
echo -e "\n${BLUE}=== 원격 레지스트리 연결 테스트 ===${NC}"
if ! nc -z $REGISTRY_HOST $REGISTRY_PORT >/dev/null 2>&1; then
  echo -e "${RED}[✗] 원격 레지스트리($DOCKER_REGISTRY_URL)에 연결할 수 없습니다.${NC}"
  echo -e "${YELLOW}다음 사항을 확인하세요:${NC}"
  echo -e "1. 원격 호스트의 microk8s registry가 활성화되어 있는지"
  echo -e "2. 원격 호스트의 방화벽이 $REGISTRY_PORT 포트를 허용하는지"
  echo -e "3. 원격 호스트에서 다음 명령을 실행했는지:"
  echo -e "   microk8s enable registry"
  echo -e "   microk8s kubectl patch svc -n container-registry registry -p '{\"spec\":{\"type\":\"NodePort\"}}'"
  exit 1
fi
echo -e "${GREEN}[✓] 원격 레지스트리에 연결할 수 있습니다.${NC}"

# 레지스트리 설정
REGISTRY=${DOCKER_REGISTRY_URL}

# 레지스트리에서 이미지 목록 조회
echo -e "\n${BLUE}=== 레지스트리 이미지 목록 조회 ===${NC}"
CATALOG=$(curl -s http://$REGISTRY/v2/_catalog)
if [[ $CATALOG == *"$IMAGE_NAME"* ]]; then
  echo -e "${GREEN}[✓] 이미지 '$IMAGE_NAME'이(가) 레지스트리에 존재합니다.${NC}"
  
  # 이미지 태그(버전) 조회
  echo -e "\n${BLUE}=== 이미지 버전 조회 ===${NC}"
  TAGS=$(curl -s http://$REGISTRY/v2/$IMAGE_NAME/tags/list)
  
  # JSON 파싱 (단순화를 위해 grep, sed 사용)
  if [[ $TAGS == *'"tags":'* ]]; then
    echo -e "${GREEN}현재 존재하는 버전:${NC}"
    # JSON 파싱: "tags":["tag1","tag2"] 형식에서 태그 추출
    TAGS_LIST=$(echo $TAGS | grep -o '"tags":\[[^]]*\]' | sed 's/"tags":\[//g' | sed 's/\]//g' | sed 's/"//g' | sed 's/,/\n/g')
    
    if [ -n "$TAGS_LIST" ]; then
      # 각 태그에 번호 부여하여 표시
      echo "$TAGS_LIST" | cat -n
    else
      echo -e "${YELLOW}태그가 없습니다.${NC}"
    fi
  else
    echo -e "${YELLOW}태그 정보를 가져올 수 없습니다.${NC}"
  fi
else
  echo -e "${YELLOW}[!] 이미지 '$IMAGE_NAME'이(가) 레지스트리에 존재하지 않습니다. 새로 생성됩니다.${NC}"
fi

# 데이터베이스 설정 검증
# echo -e "\n${BLUE}=== 데이터베이스 설정 검증 ===${NC}"
# check_env_var "DB_URL" "jdbc:postgresql://postgres:5432/postgres"
# check_env_var "DB_USERNAME" "postgres"
# check_env_var "DB_PASSWORD" "postgres"

# JVM 설정 검증
echo -e "\n${BLUE}=== JVM 설정 검증 ===${NC}"
check_env_var "JVM_XMS" "512m"
check_env_var "JVM_XMX" "1g"
check_env_var "JVM_MAX_RAM_PERCENTAGE" "75"

# 버전 선택
echo -e "\n${BLUE}=== 이미지 버전 선택 ===${NC}"
echo -e "옵션:"
echo -e "1. 자동 생성된 타임스탬프 버전 사용 (예: $(date +%Y%m%d-%H%M%S))"
echo -e "2. 사용자 지정 버전 입력"

read -p "옵션을 선택하세요 (1 또는 2): " VERSION_OPTION

if [ "$VERSION_OPTION" == "1" ]; then
  VERSION=$(date +%Y%m%d-%H%M%S)
  echo -e "${GREEN}자동 생성된 버전: $VERSION${NC}"
elif [ "$VERSION_OPTION" == "2" ]; then
  read -p "사용할 버전을 입력하세요: " USER_VERSION
  VERSION=$USER_VERSION
  
  # 이미 존재하는 버전인지 확인
  if [[ $TAGS_LIST == *"$VERSION"* ]]; then
    echo -e "${YELLOW}경고: 버전 '$VERSION'이(가) 이미 존재합니다.${NC}"
    read -p "덮어쓰시겠습니까? (y/n): " OVERWRITE
    if [[ "$OVERWRITE" != "y" && "$OVERWRITE" != "Y" ]]; then
      echo -e "${RED}빌드가 취소되었습니다. 다른 버전을 선택하세요.${NC}"
      exit 1
    fi
  fi
else
  echo -e "${RED}잘못된 옵션입니다. 자동 생성된 버전을 사용합니다.${NC}"
  VERSION=$(date +%Y%m%d-%H%M%S)
fi

echo -e "\n${BLUE}=== 빌드 정보 ===${NC}"
echo -e "Target Registry: ${GREEN}$REGISTRY${NC}"
echo -e "Image Name: ${GREEN}$IMAGE_NAME${NC}"
echo -e "Version: ${GREEN}$VERSION${NC}"

# 최종 확인
read -p "이 설정으로 빌드 및 푸시를 진행하시겠습니까? (y/n): " CONFIRM
if [[ "$CONFIRM" != "y" && "$CONFIRM" != "Y" ]]; then
  echo -e "${RED}빌드가 취소되었습니다.${NC}"
  exit 1
fi

# Jib을 사용하여 이미지 빌드 및 푸시
echo -e "\n${BLUE}=== 이미지 빌드 및 푸시 시작 ===${NC}"
./gradlew jib \
  -Djib.allowInsecureRegistries=true \
  -DsendCredentialsOverHttp=true \
  -Djib.to.image=$REGISTRY/$IMAGE_NAME:$VERSION \
  -Djib.container.environment.SPRING_PROFILES_ACTIVE=prod \
  -Djib.container.environment.SPRING_CONFIG_LOCATION="classpath:/,file:/app/config/" \
  -Djib.container.environment.DB_URL="$DB_URL" \
  -Djib.container.environment.DB_USERNAME="$DB_USERNAME" \
  -Djib.container.environment.DB_PASSWORD="$DB_PASSWORD" \
  -Djib.container.jvmFlags="-Xms$JVM_XMS,-Xmx$JVM_XMX,-XX:+UseContainerSupport,-XX:MaxRAMPercentage=$JVM_MAX_RAM_PERCENTAGE" \
  -x test

if [ $? -eq 0 ]; then
  echo -e "\n${GREEN}=== 빌드 및 푸시 완료 ===${NC}"
  echo -e "이미지가 성공적으로 빌드되어 레지스트리에 푸시되었습니다."
  echo -e "이미지: ${GREEN}$REGISTRY/$IMAGE_NAME:$VERSION${NC}"
  echo -e "\n${YELLOW}참고: 원격 호스트에서 이미지를 사용하려면:${NC}"
  echo -e "1. 이미지 주소를 localhost:32000/$IMAGE_NAME:$VERSION 로 변경하세요."
  echo -e "2. imagePullPolicy: IfNotPresent 를 사용하세요."
  echo -e "${GREEN}===================${NC}"
else
  echo -e "\n${RED}=== 빌드 실패 ===${NC}"
  echo -e "빌드 로그를 확인하세요."
  echo -e "${RED}===================${NC}"
  exit 1
fi 