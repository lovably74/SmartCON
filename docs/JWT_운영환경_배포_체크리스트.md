# JWT 토큰 시스템 운영환경 배포 체크리스트

## 개요

SmartCON Lite JWT 토큰 시스템을 운영 환경에 안전하게 배포하기 위한 체크리스트입니다. 이 문서는 DevOps 엔지니어와 시스템 관리자를 위한 단계별 배포 가이드를 제공합니다.

## 배포 단계별 체크리스트

### 1단계: 사전 준비 (Pre-deployment)

#### 환경 설정 확인
- [ ] **Java 환경**
  - [ ] Java 17 LTS 설치 확인
  - [ ] JAVA_HOME 환경변수 설정
  - [ ] 메모리 설정 (최소 2GB, 권장 4GB)

- [ ] **데이터베이스 준비**
  - [ ] MariaDB 10.11 이상 설치
  - [ ] 데이터베이스 및 사용자 계정 생성
  - [ ] SSL 연결 설정
  - [ ] 백업 정책 수립

- [ ] **SSL 인증서**
  - [ ] SSL 인증서 발급 (Let's Encrypt 또는 상용 인증서)
  - [ ] 인증서 파일 경로 확인
  - [ ] 인증서 만료일 모니터링 설정

#### 보안 설정
- [ ] **방화벽 설정**
  - [ ] HTTPS 포트 (443, 8443) 개방
  - [ ] HTTP 포트 (80, 8080) 차단 또는 HTTPS 리다이렉트
  - [ ] SSH 포트 접근 제한
  - [ ] 데이터베이스 포트 (3306) 외부 접근 차단

- [ ] **환경변수 설정**
  ```bash
  # JWT 설정
  export JWT_SECRET="your-production-secret-key-min-256-bits"
  export JWT_RSA_PRIVATE_KEY="$(cat /path/to/jwt-private.pem)"
  export JWT_RSA_PUBLIC_KEY="$(cat /path/to/jwt-public.pem)"
  
  # 데이터베이스 설정
  export DB_USERNAME="smartcon_user"
  export DB_PASSWORD="secure-database-password"
  export DB_URL="jdbc:mariadb://localhost:3306/smartcon_lite?useSSL=true"
  
  # SSL 설정
  export SSL_KEYSTORE_PATH="/path/to/keystore.p12"
  export SSL_KEYSTORE_PASSWORD="keystore-password"
  ```

### 2단계: 애플리케이션 빌드 및 테스트

#### 빌드 프로세스
- [ ] **소스코드 준비**
  - [ ] 최신 코드 체크아웃
  - [ ] 운영 환경 설정 파일 확인
  - [ ] 민감한 정보 하드코딩 여부 검사

- [ ] **빌드 실행**
  ```bash
  cd backend
  mvn clean package -Pprod
  ```
  - [ ] 빌드 성공 확인
  - [ ] JAR 파일 생성 확인
  - [ ] 빌드 로그 오류 없음 확인

#### 테스트 실행
- [ ] **단위 테스트**
  ```bash
  mvn test
  ```
  - [ ] 모든 단위 테스트 통과
  - [ ] JWT 토큰 서비스 테스트 통과
  - [ ] 인증 서비스 테스트 통과

- [ ] **통합 테스트**
  ```bash
  mvn test -Dtest=*IntegrationTest
  ```
  - [ ] JWT 인증 통합 테스트 통과
  - [ ] 멀티테넌트 테스트 통과
  - [ ] Spring Security 통합 테스트 통과

- [ ] **속성 기반 테스트**
  ```bash
  mvn test -Dtest=*PropertyBasedTest
  ```
  - [ ] 모든 속성 기반 테스트 통과 (40개 속성)
  - [ ] 각 테스트 최소 100회 반복 실행 확인

### 3단계: 스테이징 환경 배포

#### 스테이징 환경 설정
- [ ] **애플리케이션 배포**
  ```bash
  # 애플리케이션 중지
  sudo systemctl stop smartcon-lite
  
  # 백업 생성
  cp /opt/smartcon-lite/smartcon-lite.jar /opt/smartcon-lite/backup/
  
  # 새 버전 배포
  cp target/smartcon-lite-*.jar /opt/smartcon-lite/smartcon-lite.jar
  
  # 애플리케이션 시작
  sudo systemctl start smartcon-lite
  ```

- [ ] **서비스 상태 확인**
  ```bash
  # 서비스 상태 확인
  sudo systemctl status smartcon-lite
  
  # 로그 확인
  tail -f /var/log/smartcon-lite/application.log
  
  # 헬스체크
  curl -k https://staging.smartcon.co.kr/actuator/health
  ```

#### 스테이징 테스트
- [ ] **기능 테스트**
  - [ ] 로그인 API 테스트
  - [ ] 토큰 갱신 API 테스트
  - [ ] 로그아웃 API 테스트
  - [ ] 토큰 검증 API 테스트
  - [ ] 권한 기반 접근 제어 테스트

- [ ] **보안 테스트**
  - [ ] HTTPS 강제 사용 확인
  - [ ] JWT 토큰 검증 확인
  - [ ] 블랙리스트 토큰 차단 확인
  - [ ] 멀티테넌트 데이터 격리 확인

- [ ] **성능 테스트**
  ```bash
  # 부하 테스트 (Apache Bench)
  ab -n 1000 -c 10 -H "Authorization: Bearer TOKEN" \
     https://staging.smartcon.co.kr/v1/auth/validate
  ```
  - [ ] 응답 시간 100ms 이하
  - [ ] 처리량 초당 100 요청 이상
  - [ ] 오류율 1% 이하

### 4단계: 운영 환경 배포

#### 배포 전 최종 점검
- [ ] **데이터베이스 백업**
  ```bash
  mysqldump -u root -p smartcon_lite > backup_$(date +%Y%m%d_%H%M%S).sql
  ```

- [ ] **설정 파일 검증**
  - [ ] application-prod.yml 설정 확인
  - [ ] 환경변수 설정 확인
  - [ ] 로그 설정 확인

- [ ] **모니터링 준비**
  - [ ] 로그 수집 시스템 설정
  - [ ] 메트릭 수집 설정
  - [ ] 알림 시스템 설정

#### 운영 배포 실행
- [ ] **Blue-Green 배포 (권장)**
  ```bash
  # Green 환경에 새 버전 배포
  ./deploy-green.sh
  
  # 헬스체크 확인
  ./healthcheck-green.sh
  
  # 트래픽 전환
  ./switch-to-green.sh
  
  # Blue 환경 정리
  ./cleanup-blue.sh
  ```

- [ ] **Rolling 배포 (대안)**
  ```bash
  # 인스턴스별 순차 배포
  for instance in app1 app2 app3; do
    ./deploy-instance.sh $instance
    ./healthcheck-instance.sh $instance
  done
  ```

### 5단계: 배포 후 검증

#### 즉시 검증 (배포 후 30분 이내)
- [ ] **서비스 상태 확인**
  ```bash
  # 애플리케이션 상태
  curl -k https://api.smartcon.co.kr/actuator/health
  
  # 데이터베이스 연결
  curl -k https://api.smartcon.co.kr/actuator/health/db
  
  # 메모리 사용량
  curl -k https://api.smartcon.co.kr/actuator/metrics/jvm.memory.used
  ```

- [ ] **핵심 기능 테스트**
  ```bash
  # 로그인 테스트
  curl -X POST https://api.smartcon.co.kr/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@company.com","password":"password"}'
  
  # 토큰 검증 테스트
  curl -X POST https://api.smartcon.co.kr/v1/auth/validate \
    -H "Content-Type: application/json" \
    -d '{"token":"JWT_TOKEN_HERE"}'
  ```

- [ ] **로그 확인**
  ```bash
  # 오류 로그 확인
  grep -i error /var/log/smartcon-lite/application.log
  
  # 경고 로그 확인
  grep -i warn /var/log/smartcon-lite/application.log
  
  # 인증 관련 로그 확인
  grep -i "authentication" /var/log/smartcon-lite/application.log
  ```

#### 지속적 모니터링 (배포 후 24시간)
- [ ] **성능 메트릭**
  - [ ] 응답 시간 모니터링
  - [ ] 처리량 모니터링
  - [ ] 오류율 모니터링
  - [ ] 메모리 사용량 모니터링

- [ ] **보안 메트릭**
  - [ ] 로그인 성공/실패 비율
  - [ ] 토큰 생성/검증 횟수
  - [ ] 블랙리스트 토큰 차단 횟수
  - [ ] 권한 없는 접근 시도

- [ ] **비즈니스 메트릭**
  - [ ] 활성 사용자 수
  - [ ] API 사용량
  - [ ] 테넌트별 활동 현황

### 6단계: 롤백 계획

#### 롤백 조건
다음 조건 중 하나라도 발생 시 즉시 롤백:
- [ ] 서비스 가용성 95% 미만
- [ ] 응답 시간 500ms 초과
- [ ] 오류율 5% 초과
- [ ] 보안 취약점 발견
- [ ] 데이터 무결성 문제

#### 롤백 절차
- [ ] **즉시 롤백**
  ```bash
  # 이전 버전으로 복원
  ./rollback-to-previous.sh
  
  # 서비스 재시작
  sudo systemctl restart smartcon-lite
  
  # 헬스체크 확인
  ./healthcheck.sh
  ```

- [ ] **데이터베이스 롤백** (필요시)
  ```bash
  # 데이터베이스 백업 복원
  mysql -u root -p smartcon_lite < backup_YYYYMMDD_HHMMSS.sql
  ```

- [ ] **롤백 후 검증**
  - [ ] 서비스 정상 동작 확인
  - [ ] 데이터 무결성 확인
  - [ ] 사용자 접근 가능 확인

### 7단계: 문서화 및 보고

#### 배포 완료 보고서
- [ ] **배포 정보**
  - 배포 일시: _______________
  - 배포 버전: _______________
  - 배포 담당자: _____________
  - 배포 방식: _______________

- [ ] **테스트 결과**
  - 단위 테스트: 통과/실패
  - 통합 테스트: 통과/실패
  - 성능 테스트: 통과/실패
  - 보안 테스트: 통과/실패

- [ ] **이슈 및 해결사항**
  - 발생한 이슈: _____________
  - 해결 방법: _______________
  - 향후 개선사항: ___________

#### 운영 문서 업데이트
- [ ] **운영 매뉴얼 업데이트**
  - [ ] 새로운 기능 설명 추가
  - [ ] 설정 변경사항 반영
  - [ ] 문제 해결 가이드 업데이트

- [ ] **모니터링 대시보드 설정**
  - [ ] 새로운 메트릭 추가
  - [ ] 알림 임계값 설정
  - [ ] 대시보드 레이아웃 조정

## 자동화 스크립트 예제

### 배포 스크립트 (deploy.sh)
```bash
#!/bin/bash

set -e

# 설정
APP_NAME="smartcon-lite"
APP_DIR="/opt/smartcon-lite"
BACKUP_DIR="/opt/smartcon-lite/backup"
LOG_FILE="/var/log/smartcon-lite/deploy.log"

# 로그 함수
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a $LOG_FILE
}

# 백업 생성
create_backup() {
    log "백업 생성 중..."
    mkdir -p $BACKUP_DIR
    if [ -f "$APP_DIR/$APP_NAME.jar" ]; then
        cp "$APP_DIR/$APP_NAME.jar" "$BACKUP_DIR/$APP_NAME-$(date +%Y%m%d_%H%M%S).jar"
        log "백업 완료"
    fi
}

# 애플리케이션 중지
stop_application() {
    log "애플리케이션 중지 중..."
    sudo systemctl stop $APP_NAME
    log "애플리케이션 중지 완료"
}

# 새 버전 배포
deploy_new_version() {
    log "새 버전 배포 중..."
    cp "target/$APP_NAME-*.jar" "$APP_DIR/$APP_NAME.jar"
    chown smartcon:smartcon "$APP_DIR/$APP_NAME.jar"
    chmod 755 "$APP_DIR/$APP_NAME.jar"
    log "새 버전 배포 완료"
}

# 애플리케이션 시작
start_application() {
    log "애플리케이션 시작 중..."
    sudo systemctl start $APP_NAME
    sleep 10
    log "애플리케이션 시작 완료"
}

# 헬스체크
health_check() {
    log "헬스체크 실행 중..."
    for i in {1..30}; do
        if curl -f -k https://localhost:8443/actuator/health > /dev/null 2>&1; then
            log "헬스체크 성공"
            return 0
        fi
        log "헬스체크 대기 중... ($i/30)"
        sleep 10
    done
    log "헬스체크 실패"
    return 1
}

# 메인 배포 프로세스
main() {
    log "배포 시작"
    
    create_backup
    stop_application
    deploy_new_version
    start_application
    
    if health_check; then
        log "배포 성공"
        exit 0
    else
        log "배포 실패 - 롤백 실행"
        rollback
        exit 1
    fi
}

# 롤백 함수
rollback() {
    log "롤백 시작"
    
    # 최신 백업 파일 찾기
    LATEST_BACKUP=$(ls -t $BACKUP_DIR/$APP_NAME-*.jar | head -n1)
    
    if [ -f "$LATEST_BACKUP" ]; then
        log "백업 파일로 복원: $LATEST_BACKUP"
        cp "$LATEST_BACKUP" "$APP_DIR/$APP_NAME.jar"
        start_application
        
        if health_check; then
            log "롤백 성공"
        else
            log "롤백 실패 - 수동 개입 필요"
        fi
    else
        log "백업 파일을 찾을 수 없음"
    fi
}

# 스크립트 실행
main "$@"
```

### 헬스체크 스크립트 (healthcheck.sh)
```bash
#!/bin/bash

# 설정
API_URL="https://localhost:8443"
TIMEOUT=30

# 헬스체크 함수
check_health() {
    local endpoint=$1
    local expected_status=${2:-200}
    
    echo "Checking $endpoint..."
    
    response=$(curl -s -k -w "%{http_code}" -o /dev/null --max-time $TIMEOUT "$API_URL$endpoint")
    
    if [ "$response" = "$expected_status" ]; then
        echo "✓ $endpoint - OK ($response)"
        return 0
    else
        echo "✗ $endpoint - FAIL ($response)"
        return 1
    fi
}

# 메인 헬스체크
main() {
    echo "=== SmartCON Lite 헬스체크 ==="
    echo "시간: $(date)"
    echo "서버: $API_URL"
    echo ""
    
    local failed=0
    
    # 기본 헬스체크
    check_health "/actuator/health" || ((failed++))
    
    # 데이터베이스 헬스체크
    check_health "/actuator/health/db" || ((failed++))
    
    # 메트릭 확인
    check_health "/actuator/metrics" || ((failed++))
    
    echo ""
    if [ $failed -eq 0 ]; then
        echo "✓ 모든 헬스체크 통과"
        exit 0
    else
        echo "✗ $failed 개의 헬스체크 실패"
        exit 1
    fi
}

main "$@"
```

---

**문서 버전**: 1.0  
**최종 업데이트**: 2026년 1월 12일  
**작성자**: SmartCON Lite DevOps 팀