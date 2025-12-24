# SmartCON Lite 데이터베이스 스키마 설계

**문서 버전:** 1.0  
**작성일:** 2025년 12월 23일  
**작성자:** Kiro AI Assistant  
**기반 문서:** 상세기능명세서 v3.0, Backend-Architecture v1.0, Platform-API-Specification v1.0

---

## 1. 데이터베이스 설계 개요

### 1.1 설계 원칙
- **멀티테넌트 아키텍처**: 모든 비즈니스 테이블에 `tenant_id` 포함
- **데이터 무결성**: 외래키 제약조건 및 체크 제약조건 적용
- **성능 최적화**: 적절한 인덱스 설계 및 파티셔닝 고려
- **확장성**: 수평/수직 확장을 고려한 정규화 설계
- **보안**: 민감정보 암호화 및 접근 제어

### 1.2 기술 스택
- **DBMS**: MariaDB 10.11 (Primary)
- **캐시**: Redis 7.x (Session, Temporary Data)
- **파일 저장소**: AWS S3 (Documents, Images)
- **마이그레이션**: Flyway
- **ORM**: JPA/Hibernate 6.x

### 1.3 명명 규칙
- **테이블명**: 복수형, snake_case (예: `users`, `attendance_logs`)
- **컬럼명**: snake_case (예: `created_at`, `business_number`)
- **인덱스명**: `idx_{table}_{columns}` (예: `idx_users_tenant_id`)
- **외래키명**: `fk_{table}_{referenced_table}` (예: `fk_users_tenants`)

이 문서는 SmartCON Lite 데이터베이스 스키마 설계의 백업 버전입니다.
전체 내용은 최신 버전인 Database-Schema-Design.md 파일을 참조하시기 바랍니다.

**백업 생성일시:** 2025년 12월 23일 18:45:00
**백업 사유:** 초기 데이터베이스 스키마 설계 완료