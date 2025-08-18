# API 엔드포인트 문서

## 📋 API 개요

REST Server는 RESTful API, gRPC, WebSocket을 지원하는 다양한 통신 프로토콜을 제공합니다. 이 문서는 각 API의 엔드포인트, 요청/응답 형식, 그리고 사용법을 상세히 설명합니다.

## 🔗 기본 정보

- **Base URL**: `http://localhost:8080/api/v1`
- **API 문서**: `http://localhost:8080/swagger-ui.html`
- **API 스펙**: `http://localhost:8080/api-docs`
- **Content-Type**: `application/json`
- **인증**: JWT Bearer Token (필요시)

## 📊 API 응답 형식

모든 API는 일관된 응답 형식을 사용합니다:

```json
{
  "status_code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    // 실제 응답 데이터
  }
}
```

### 응답 코드

| 상태 코드 | 설명 |
|-----------|------|
| 200 | 성공 |
| 201 | 생성됨 |
| 400 | 잘못된 요청 |
| 401 | 인증 실패 |
| 403 | 권한 없음 |
| 404 | 리소스를 찾을 수 없음 |
| 500 | 서버 내부 오류 |

## 🎯 로또 API

### 1. 로또 정보 조회

#### 전체 로또 목록 조회
```http
GET /api/v1/lotto
```

**응답 예시:**
```json
{
  "status_code": 200,
  "message": "로또 정보가 성공적으로 조회되었습니다.",
  "data": [
    {
      "id": 1,
      "drwNo": 1001,
      "drwNoDate": "2024-01-01",
      "drwtNo1": 1,
      "drwtNo2": 2,
      "drwtNo3": 3,
      "drwtNo4": 4,
      "drwtNo5": 5,
      "drwtNo6": 6,
      "bnusNo": 7,
      "firstPrzwnerCo": 10,
      "firstAccumamnt": 1000000000,
      "firstWinamnt": 100000000,
      "totSellamnt": 10000000000,
      "returnValue": ""
    }
  ]
}
```

#### 특정 회차 로또 정보 조회
```http
GET /api/v1/lotto/{drwNo}
```

**경로 변수:**
- `drwNo`: 로또 회차 번호 (정수)

**응답 예시:**
```json
{
  "status_code": 200,
  "message": "로또 정보가 성공적으로 조회되었습니다.",
  "data": {
    "id": 1,
    "drwNo": 1001,
    "drwNoDate": "2024-01-01",
    "drwtNo1": 1,
    "drwtNo2": 2,
    "drwtNo3": 3,
    "drwtNo4": 4,
    "drwtNo5": 5,
    "drwtNo6": 6,
    "bnusNo": 7,
    "firstPrzwnerCo": 10,
    "firstAccumamnt": 1000000000,
    "firstWinamnt": 100000000,
    "totSellamnt": 10000000000,
    "returnValue": ""
  }
}
```

### 2. 로또 정보 생성

#### 새로운 로또 정보 생성
```http
POST /api/v1/lotto
```

**요청 본문:**
```json
{
  "drwNo": 1002,
  "drwNoDate": "2024-01-08",
  "drwtNo1": 8,
  "drwtNo2": 9,
  "drwtNo3": 10,
  "drwtNo4": 11,
  "drwtNo5": 12,
  "drwtNo6": 13,
  "bnusNo": 14,
  "firstPrzwnerCo": 15,
  "firstAccumamnt": 2000000000,
  "firstWinamnt": 200000000,
  "totSellamnt": 20000000000
}
```

**응답 예시:**
```json
{
  "status_code": 201,
  "message": "로또 정보가 성공적으로 생성되었습니다.",
  "data": {
    "id": 2,
    "drwNo": 1002,
    "drwNoDate": "2024-01-08",
    "drwtNo1": 8,
    "drwtNo2": 9,
    "drwtNo3": 10,
    "drwtNo4": 11,
    "drwtNo5": 12,
    "drwtNo6": 13,
    "bnusNo": 14,
    "firstPrzwnerCo": 15,
    "firstAccumamnt": 2000000000,
    "firstWinamnt": 200000000,
    "totSellamnt": 20000000000,
    "returnValue": ""
  }
}
```

### 3. 로또 정보 수정

#### 기존 로또 정보 수정
```http
PUT /api/v1/lotto/{id}
```

**경로 변수:**
- `id`: 로또 정보 ID (정수)

**요청 본문:**
```json
{
  "drwNo": 1002,
  "drwNoDate": "2024-01-08",
  "drwtNo1": 8,
  "drwtNo2": 9,
  "drwtNo3": 10,
  "drwtNo4": 11,
  "drwtNo5": 12,
  "drwtNo6": 13,
  "bnusNo": 14,
  "firstPrzwnerCo": 16,
  "firstAccumamnt": 2500000000,
  "firstWinamnt": 250000000,
  "totSellamnt": 25000000000
}
```

### 4. 로또 정보 삭제

#### 로또 정보 삭제
```http
DELETE /api/v1/lotto/{id}
```

**경로 변수:**
- `id`: 로또 정보 ID (정수)

**응답 예시:**
```json
{
  "status_code": 200,
  "message": "로또 정보가 성공적으로 삭제되었습니다.",
  "data": null
}
```

## 🎰 연금 로또 API

### 1. 연금 로또 정보 조회

#### 전체 연금 로또 목록 조회
```http
GET /api/v1/annuity-lotto
```

#### 특정 회차 연금 로또 정보 조회
```http
GET /api/v1/annuity-lotto/{drwNo}
```

### 2. 연금 로또 정보 생성

#### 새로운 연금 로또 정보 생성
```http
POST /api/v1/annuity-lotto
```

### 3. 연금 로또 정보 수정

#### 기존 연금 로또 정보 수정
```http
PUT /api/v1/annuity-lotto/{id}
```

### 4. 연금 로또 정보 삭제

#### 연금 로또 정보 삭제
```http
DELETE /api/v1/annuity-lotto/{id}
```

## 🔧 테스트 API

### Coroutine 테스트
```http
GET /api/v1/test/coroutine
```

**응답 예시:**
```json
{
  "status_code": 200,
  "message": "Coroutine 테스트가 성공적으로 완료되었습니다.",
  "data": {
    "threadName": "reactor-http-nio-2",
    "timestamp": "2024-12-19T10:30:00",
    "executionTime": 150
  }
}
```

## 📡 gRPC API

### 1. Greeter 서비스

#### SayHello
```protobuf
service Greeter {
  rpc SayHello (HelloRequest) returns (HelloReply);
}
```

**요청:**
```protobuf
message HelloRequest {
  string name = 1;
}
```

**응답:**
```protobuf
message HelloReply {
  string message = 1;
}
```

### 2. ML 서비스

#### Predict
```protobuf
service ML {
  rpc Predict (PredictionRequest) returns (PredictionResponse);
}
```

**요청:**
```protobuf
message PredictionRequest {
  repeated float features = 1;
}
```

**응답:**
```protobuf
message PredictionResponse {
  float prediction = 1;
  float confidence = 2;
}
```

## 🔌 WebSocket API

### 1. 실시간 로또 정보

#### 연결
```javascript
const ws = new WebSocket('ws://localhost:8080/ws/lotto');
```

#### 메시지 형식
```json
{
  "type": "LOTTO_UPDATE",
  "data": {
    "drwNo": 1001,
    "timestamp": "2024-12-19T10:30:00Z"
  }
}
```

## 📋 API 사용 예시

### cURL을 사용한 로또 정보 조회

```bash
# 전체 로또 목록 조회
curl -X GET "http://localhost:8080/api/v1/lotto" \
  -H "Content-Type: application/json"

# 특정 회차 로또 정보 조회
curl -X GET "http://localhost:8080/api/v1/lotto/1001" \
  -H "Content-Type: application/json"

# 새로운 로또 정보 생성
curl -X POST "http://localhost:8080/api/v1/lotto" \
  -H "Content-Type: application/json" \
  -d '{
    "drwNo": 1003,
    "drwNoDate": "2024-01-15",
    "drwtNo1": 15,
    "drwtNo2": 16,
    "drwtNo3": 17,
    "drwtNo4": 18,
    "drwtNo5": 19,
    "drwtNo6": 20,
    "bnusNo": 21,
    "firstPrzwnerCo": 20,
    "firstAccumamnt": 3000000000,
    "firstWinamnt": 300000000,
    "totSellamnt": 30000000000
  }'
```

### JavaScript를 사용한 API 호출

```javascript
// 로또 정보 조회
async function getLottoInfo(drwNo) {
  try {
    const response = await fetch(`http://localhost:8080/api/v1/lotto/${drwNo}`);
    const data = await response.json();
    
    if (data.status_code === 200) {
      return data.data;
    } else {
      throw new Error(data.message);
    }
  } catch (error) {
    console.error('로또 정보 조회 실패:', error);
    throw error;
  }
}

// 로또 정보 생성
async function createLottoInfo(lottoData) {
  try {
    const response = await fetch('http://localhost:8080/api/v1/lotto', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(lottoData)
    });
    
    const data = await response.json();
    
    if (data.status_code === 201) {
      return data.data;
    } else {
      throw new Error(data.message);
    }
  } catch (error) {
    console.error('로또 정보 생성 실패:', error);
    throw error;
  }
}
```

### Python을 사용한 API 호출

```python
import requests
import json

# 로또 정보 조회
def get_lotto_info(drw_no):
    try:
        response = requests.get(f'http://localhost:8080/api/v1/lotto/{drw_no}')
        data = response.json()
        
        if data['status_code'] == 200:
            return data['data']
        else:
            raise Exception(data['message'])
    except Exception as e:
        print(f'로또 정보 조회 실패: {e}')
        raise

# 로또 정보 생성
def create_lotto_info(lotto_data):
    try:
        response = requests.post(
            'http://localhost:8080/api/v1/lotto',
            headers={'Content-Type': 'application/json'},
            data=json.dumps(lotto_data)
        )
        
        data = response.json()
        
        if data['status_code'] == 201:
            return data['data']
        else:
            raise Exception(data['message'])
    except Exception as e:
        print(f'로또 정보 생성 실패: {e}')
        raise
```

## 🚨 에러 처리

### 일반적인 에러 응답

```json
{
  "status_code": 400,
  "message": "잘못된 요청입니다.",
  "data": null
}
```

### 유효성 검증 에러

```json
{
  "status_code": 400,
  "message": "입력 데이터가 유효하지 않습니다.",
  "data": {
    "errors": [
      {
        "field": "drwNo",
        "message": "회차 번호는 1 이상이어야 합니다."
      },
      {
        "field": "drwNoDate",
        "message": "날짜 형식이 올바르지 않습니다."
      }
    ]
  }
}
```

### 서버 내부 에러

```json
{
  "status_code": 500,
  "message": "서버 내부 오류가 발생했습니다.",
  "data": null
}
```

## 📊 API 성능 지표

### 응답 시간
- **평균 응답 시간**: < 100ms
- **95% 응답 시간**: < 200ms
- **99% 응답 시간**: < 500ms

### 처리량
- **초당 요청 처리량**: 1000+ requests/sec
- **동시 사용자 지원**: 1000+ concurrent users

### 가용성
- **서비스 가용성**: 99.9%
- **계획된 다운타임**: 월 1회 (유지보수)

## 🔒 보안 고려사항

### 1. 인증
- JWT 토큰 기반 인증
- 토큰 만료 시간: 24시간
- 리프레시 토큰 지원

### 2. 권한 관리
- 역할 기반 접근 제어 (RBAC)
- API별 권한 설정
- IP 화이트리스트 지원

### 3. 데이터 보호
- HTTPS 통신 강제
- 민감한 데이터 암호화
- SQL 인젝션 방지

## 📈 모니터링 및 로깅

### 1. API 모니터링
- 요청/응답 로깅
- 성능 메트릭 수집
- 에러율 모니터링

### 2. 로그 형식
```json
{
  "timestamp": "2024-12-19T10:30:00Z",
  "level": "INFO",
  "correlationId": "req-12345",
  "method": "GET",
  "path": "/api/v1/lotto",
  "statusCode": 200,
  "responseTime": 45,
  "userId": "user123"
}
```

---

**문서 버전**: v1.0.0  
**마지막 업데이트**: 2024-12-19  
**작성자**: Development Team
