# SmartCON Lite 시스템 아키텍처 설계서

**문서 버전:** 2.0  
**작성일:** 2025년 12월 17일  
**작성자:** 개발 PM (Gemini)

---

## 1. 아키텍처 개요 (Overview)

SmartCON Lite는 **SaaS(Software as a Service) 기반의 멀티 테넌트 아키텍처**를 채택하여, 단일 인스턴스로 여러 고객사(Tenant)를 효율적으로 지원합니다.  
**MSA(Microservices Architecture) 지향적인 모듈러 모놀리스(Modular Monolith)** 구조를 기반으로 초기 개발 속도를 높이고 향후 확장을 대비합니다.

### 1.1. 설계 원칙
- **Multi-tenancy**: 데이터 격리(Schema-based or Discriminator-based) 및 보안 필수.
- **Mobile First**: 현장 사용성을 최우선으로 고려한 하이브리드 앱(Capacitor) 권장.
- **AI-Native**: 안면인식(FaceNet) 파이프라인의 내재화.
- **Scalability**: Stateless 서버 구조 및 도커 컨테이너 기반 배포.

---

## 2. 전체 시스템 구성도 (System Landscape)

```mermaid
graph TD
    Client_PC[PC Client (Admin)] --> |HTTPS| LB[Load Balancer / Nginx]
    Client_Mobile[Mobile App (Hybrid)] --> |HTTPS| LB
    
    subgraph "SmartCON Cloud (AWS/On-Premise)"
        LB --> WAS[API Server (Spring Boot)]
        
        subgraph "Backend Services"
            WAS --> Auth[Auth Service (OAuth2)]
            WAS --> Core[Workforce Core Domain]
            WAS --> Batch[Batch Service (FaceSync, Billing)]
        end
        
        subgraph "Data Store"
            WAS --> DB[(MariaDB - MultiTenant)]
            WAS --> Redis[(Redis - Cache/Session)]
            WAS --> S3[Object Storage (Photos/Contracts)]
        end

        subgraph "AI Inference"
            Batch --> FaceEngine[FaceNet Serving (Python/TensorFlow)]
        end
    end

    subgraph "External Systems"
        WAS --> KMA[Weather API]
        WAS --> PG[Payment Gateway]
        WAS --> NTS[HomeTax API (Tax Invoice)]
        WAS --> FCM[FCM (Push)]
    end
```

---

## 3. 기술 스택 (Technology Stack)

| 영역 | 기술 요소 | 버전 | 비고 |
| :--- | :--- | :--- | :--- |
| **Frontend** | **React** | 18.x | Vite Build System |
| | **Language** | TypeScript | 5.x |
| | **UI Framework** | **Shadcn/UI** + Tailwind CSS | 디자인 시스템 적용 |
| | **State Mgmt** | Zustand (Client), TanStack Query (Server) | |
| | **Mobile Bridge** | **Capacitor** | 6.x | Camera, Geolocation, Push |
| **Backend** | **Java** | 17 (LTS) | |
| | **Framework** | **Spring Boot** | 3.3.x | |
| | **ORM** | Spring Data JPA + QueryDSL | |
| | **Auth** | Spring Security + JWT | OAuth2 Client (Social) |
| | **Batch** | Spring Batch | 대용량 데이터 처리 |
| **Database** | **MariaDB** | 10.11 | |
| | **Redis** | 7.x | Session, Cache, Pub/Sub |
| **DevOps** | **Docker** | | 컨테이너 가상화 |
| | **CI/CD** | Github Actions | (권장) |

---

## 4. 데이터 아키텍처 (Data Architecture)

### 4.1. 멀티 테넌트 전략
- **방식**: **Shared Database, Shared Schema** (Discriminator Column `tenant_id` 사용)
- **이유**: 중소규모 건설사의 잦은 생성/소멸 및 리소스 효율성 최적화.
- **보안**: Hibernate Filter 또는 AOP를 통해 모든 쿼리에 `WHERE tenant_id = ?` 강제 주입.

### 4.2. 주요 데이터 흐름
1.  **안면인식 데이터**:
    *   (App) 사진 촬영 -> (S3) 이미지 업로드 -> (FaceEngine) 128D Embedding 추출 -> (DB) Vector 저장 -> (Edge) 현장 단말기 동기화
2.  **공사일보 데이터**:
    *   (Team) 일일 보고 -> (DB) 임시 저장 -> (Batch) 기상청 날씨 병합 -> (Site) 확정 및 마감

---

## 5. 인터페이스 아키텍처 (Interface Architecture)

### 5.1. 내부 API (Frontend <-> Backend)
- **Protocol**: RESTful API over HTTPS
- **Payload**: JSON
- **Auth header**: `Authorization: Bearer {JWT}`

### 5.2. 외부 연동 (External)
- **기상청 API**: 공공데이터포털 활용 (단기예보, 초단기실황)
- **PG사 (Payment)**: 토스페이먼츠/아임포트 등 (구독 결제)
- **홈택스 (Tax)**: 전자세금계산서 발급 대행 서비스 (Popbill, Barobill 등) API 활용

---

**문서 끝**
