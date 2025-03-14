#!/bin/bash

# 환경 설정 디렉토리 초기화 스크립트

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 설정 가능한 변수
ENV_REPO_URL="${1:-}"  # 첫 번째 명령줄 인수 또는 빈 문자열
ENV_DIR="env"

echo -e "${YELLOW}Rest Server 환경 설정 초기화${NC}"
echo "=============================================="

# env 디렉토리가 이미 존재하는지 확인
if [ -d "$ENV_DIR" ]; then
    echo -e "${YELLOW}경고: $ENV_DIR 디렉토리가 이미 존재합니다.${NC}"
    read -p "기존 디렉토리를 삭제하고 계속하시겠습니까? (y/n): " confirm
    if [ "$confirm" != "y" ]; then
        echo "초기화를 취소합니다."
        exit 1
    fi
    rm -rf "$ENV_DIR"
    echo "$ENV_DIR 디렉토리를 삭제했습니다."
fi

# 저장소 URL 확인
if [ -z "$ENV_REPO_URL" ]; then
    echo -e "${YELLOW}환경 설정 저장소 URL이 제공되지 않았습니다.${NC}"
    read -p "프라이빗 환경 설정 저장소 URL을 입력하세요 (비워두면 로컬로 초기화): " ENV_REPO_URL
fi

# 환경 설정 저장소에서 클론 또는 로컬로 초기화
mkdir -p "$ENV_DIR"

if [ -n "$ENV_REPO_URL" ]; then
    echo -e "${GREEN}저장소에서 환경 설정 클론 중: $ENV_REPO_URL${NC}"
    git clone "$ENV_REPO_URL" "$ENV_DIR"
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}저장소 클론 실패. 로컬로 초기화합니다.${NC}"
        # 클론 실패 시 로컬로 초기화
        setup_local=true
    else
        echo -e "${GREEN}환경 설정 저장소를 성공적으로 클론했습니다.${NC}"
    fi
else
    echo -e "${YELLOW}저장소 URL이 제공되지 않았습니다. 로컬로 초기화합니다.${NC}"
    setup_local=true
fi

# 로컬로 초기화 진행
if [ "$setup_local" = true ]; then
    # 예제 환경 파일 확인
    if [ -f "src/main/resources/application.yml.example" ]; then
        # 예제 파일로부터 env 디렉토리 초기화
        echo -e "${GREEN}로컬 환경 설정 초기화 중...${NC}"
        
        # .env.example이 있으면 복사
        if [ -f ".env.example" ]; then
            cp .env.example "$ENV_DIR/example.env"
            cp .env.example "$ENV_DIR/dev.env"
            echo "example.env 및 dev.env 파일이 생성되었습니다."
        else
            echo -e "${YELLOW}경고: .env.example 파일을 찾을 수 없습니다.${NC}"
            echo "# 개발 환경 설정" > "$ENV_DIR/dev.env"
            echo "SPRING_PROFILES_ACTIVE=dev" >> "$ENV_DIR/dev.env"
            echo "# 예제 환경 설정" > "$ENV_DIR/example.env"
            echo "SPRING_PROFILES_ACTIVE=dev" >> "$ENV_DIR/example.env"
            echo "기본 dev.env 및 example.env 파일이 생성되었습니다."
        fi
        
        # prod.env 파일 생성
        echo "# 운영 환경 설정" > "$ENV_DIR/prod.env"
        echo "SPRING_PROFILES_ACTIVE=prod" >> "$ENV_DIR/prod.env"
        echo "prod.env 파일이 생성되었습니다."
        
        # README.md 생성
        cat > "$ENV_DIR/README.md" << 'EOF'
# Rest Server 환경 설정

이 디렉토리는 Rest Server 애플리케이션의 환경 설정 파일을 관리합니다.

## 파일 구조

- `dev.env`: 개발 환경 설정
- `prod.env`: 운영 환경 설정
- `example.env`: 환경 설정 예시 파일

## 주의사항

- 이 디렉토리는 별도의 프라이빗 Git 저장소로 관리하는 것을 권장합니다.
- 민감한 정보(DB 비밀번호, API 키 등)를 포함하고 있으므로 접근 권한을 엄격하게 관리하세요.
EOF
        echo "README.md 파일이 생성되었습니다."
        
        # Git 저장소 초기화
        (cd "$ENV_DIR" && git init && git add . && git commit -m "Initial commit")
        echo -e "${GREEN}$ENV_DIR 디렉토리에 Git 저장소가 초기화되었습니다.${NC}"
    else
        echo -e "${RED}오류: 예제 환경 설정 파일이 없습니다.${NC}"
        exit 1
    fi
fi

echo ""
echo -e "${GREEN}환경 설정 초기화가 완료되었습니다!${NC}"
echo "=============================================="
echo -e "다음 단계:"
echo -e "1. ${YELLOW}$ENV_DIR/dev.env${NC} 파일에서 개발 환경 설정을 확인하고 필요에 따라 수정하세요."
echo -e "2. ${YELLOW}$ENV_DIR/prod.env${NC} 파일에 운영 환경에 필요한 설정을 추가하세요."
echo -e "3. 변경사항을 저장소에 커밋하고 푸시하는 것을 잊지 마세요."
echo ""
echo -e "이 디렉토리를 별도의 프라이빗 Git 저장소로 관리하려면 다음 명령어를 실행하세요:"
echo -e "${YELLOW}cd $ENV_DIR${NC}"
echo -e "${YELLOW}git remote add origin YOUR_PRIVATE_REPO_URL${NC}"
echo -e "${YELLOW}git push -u origin master${NC}" 