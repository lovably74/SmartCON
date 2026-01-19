'''
# SmartCON Lite 통합 개발자 가이드

**작성자**: Manus AI
**작성일**: 2026년 1월 19일

## 1. 본사 관리자 (PC Web)

### 1.1. 현장 관리 (Site Management)

**화면 개요**: 본사 관리자가 새로운 현장을 개설하고, 기존 현장의 정보(안면인식 단말기 설정 등)를 관리하는 페이지입니다.

**UI 화면**: `[UI Image: 본사 관리자 - 현장 관리 대시보드]`

#### UI 요소별 명세

| 번호 | UI 요소 | 설명 |
| :--- | :--- | :--- |
| 1 | **현장 목록 테이블** | 전체 현장 리스트를 페이지네이션과 함께 표시합니다. 현장명, 현장소장, 상태, 개설일 등의 컬럼을 포함합니다. |
| 2 | **+ 새 현장 개설 버튼** | 현장 개설 모달(Modal)을 엽니다. |
| 3 | **현장명 검색 필드** | 입력된 키워드로 현장 목록을 실시간 필터링합니다. |
| 4 | **설정(톱니바퀴) 아이콘** | 특정 현장의 상세 설정 모달을 엽니다. |
| 5 | **상태 토글 스위치** | 현장의 활성/비활성 상태를 변경합니다. |

#### 상세 구현 명세

**[1] 현장 목록 테이블**

| 구분 | 상세 구현 내용 |
| :--- | :--- |
| **Event** | 페이지 로드 시 (`useEffect` 또는 `componentDidMount`) |
| **Frontend Logic** | - `useQuery` (React Query)를 사용하여 현장 목록 API를 호출합니다.<br>- 로딩 중에는 스켈레톤 UI를 표시하고, 에러 발생 시 에러 메시지를 표시합니다.<br>- 페이지네이션 컴포넌트와 연동하여 페이지 변경 시 `page` 파라미터를 변경하여 API를 재호출합니다. |
| **API Request** | `GET /api/v1/sites?page={page_number}&size=10&sort=createdAt,desc&name={keyword}` |
| **Backend Logic** | - `tenant_id`를 기준으로 현재 테넌트에 속한 현장 목록만 조회합니다. (Hibernate Filter 자동 적용)<br>- `Pageable` 객체를 사용하여 페이지네이션을 처리하고, `Page<Site>` 객체를 반환합니다. |
| **Initial State** | - 현재 페이지: 1<br>- 검색 키워드: '' (빈 문자열) |

**[2] + 새 현장 개설 버튼**

| 구분 | 상세 구현 내용 |
| :--- | :--- |
| **Event** | `onClick` |
| **Frontend Logic** | - 현장 개설 모달의 상태를 `true`로 변경하여 화면에 표시합니다.<br>- 모달 내부의 폼 필드를 초기화합니다. |
| **API Request** | (모달 내부의 '저장' 버튼에서 처리) `POST /api/v1/sites` |
| **Backend Logic** | - 요청된 현장 정보를 바탕으로 새로운 `Site` 엔티티를 생성하고 저장합니다.<br>- 저장 시 `@PrePersist` 리스너가 `tenant_id`를 자동으로 주입합니다. |
| **Initial State** | 모달 숨김 (`isOpen: false`) |

**[3] 현장명 검색 필드**

| 구분 | 상세 구현 내용 |
| :--- | :--- |
| **Event** | `onChange` |
| **Frontend Logic** | - 입력 이벤트를 500ms 동안 지연시키는 디바운스(Debounce) 로직을 적용합니다.<br>- 디바운스 후, 입력된 키워드를 `name` 쿼리 파라미터로 사용하여 현장 목록 API를 재호출합니다. |
| **API Request** | `GET /api/v1/sites?name={keyword}` |
| **Backend Logic** | - `Specification` 또는 `QueryDSL`을 사용하여 `name` 필드에 대한 `LIKE` 검색 조건을 동적으로 추가합니다. |
| **Initial State** | 값: '' (빈 문자열) |

**[4] 설정(톱니바퀴) 아이콘**

| 구분 | 상세 구현 내용 |
| :--- | :--- |
| **Event** | `onClick` |
| **Frontend Logic** | - 클릭된 현장의 `siteId`를 사용하여 상세 정보 API를 호출합니다.<br>- API 응답 데이터를 기반으로 상세 설정 모달의 폼 필드를 채우고, 모달을 표시합니다. |
| **API Request** | `GET /api/v1/sites/{siteId}` |
| **Backend Logic** | - `tenant_id`와 `siteId`를 모두 사용하여 정확한 현장 정보를 조회합니다. (데이터 격리 보장) |
| **Initial State** | 모달 숨김 (`isOpen: false`) |

**[5] 상태 토글 스위치**

| 구분 | 상세 구현 내용 |
| :--- | :--- |
| **Event** | `onChange` |
| **Frontend Logic** | - 낙관적 업데이트(Optimistic Update)를 적용하여 UI를 즉시 변경합니다.<br>- 상태 변경 API를 호출하고, 실패 시 UI를 원래 상태로 되돌리고 에러 메시지를 표시합니다. |
| **API Request** | `PATCH /api/v1/sites/{siteId}/status` (Payload: `{ "status": "INACTIVE" }`) |
| **Backend Logic** | - 요청된 `status` 값의 유효성을 검증하고, 해당 현장의 상태를 업데이트합니다. |
| **Initial State** | API 응답에 따른 현장 상태 (`site.status`) |

---
'''

## 2. 노무자 (Mobile App)

### 2.1. 안면인식 데이터 등록 (Face Registration)

**화면 개요**: 노무자가 본인 인증 후, 출퇴근 기록을 위해 자신의 안면 데이터를 등록하는 페이지입니다.

**UI 화면**: `[UI Image: 노무자 - 안면 등록 가이드 및 카메라 화면]`

#### UI 요소별 명세

| 번호 | UI 요소 | 설명 |
| :--- | :--- | :--- |
| 1 | **안내 가이드라인** | 안면 등록 시 주의사항(정면 응시, 안경 벗기 등)을 안내하는 텍스트 및 아이콘 |
| 2 | **카메라 프리뷰** | 실시간 카메라 영상이 표시되는 원형 영역 |
| 3 | **촬영 버튼** | 카메라 프리뷰의 현재 프레임을 캡처하여 서버로 전송합니다. |
| 4 | **품질 검사 피드백** | "얼굴을 중앙에 맞춰주세요", "조명이 너무 어둡습니다" 등 실시간 피드백 메시지 |

#### 상세 구현 명세

**[1] 안내 가이드라인**

| 구분 | 상세 구현 내용 |
| :--- | :--- |
| **Event** | 페이지 로드 시 |
| **Frontend Logic** | - 정적인 텍스트와 아이콘을 표시합니다. |
| **API Request** | 없음 |
| **Backend Logic** | 없음 |
| **Initial State** | 항상 표시 |

**[2] 카메라 프리뷰**

| 구분 | 상세 구현 내용 |
| :--- | :--- |
| **Event** | 페이지 로드 시 (`useEffect`) |
| **Frontend Logic** | - `navigator.mediaDevices.getUserMedia({ video: true })`를 사용하여 카메라 스트림을 가져옵니다.<br>- 가져온 스트림을 `<video>` 요소의 `srcObject`에 할당하여 실시간 프리뷰를 표시합니다. |
| **API Request** | 없음 |
| **Backend Logic** | 없음 |
| **Initial State** | 카메라 권한 요청 및 스트림 대기 상태 |

**[3] 촬영 버튼**

| 구분 | 상세 구현 내용 |
| :--- | :--- |
| **Event** | `onClick` |
| **Frontend Logic** | - `<video>` 요소의 현재 프레임을 `<canvas>`에 그립니다.<br>- `canvas.toBlob()`을 사용하여 이미지를 Blob 객체로 변환합니다.<br>- `FormData`에 Blob 객체를 담아 안면 등록 API로 전송합니다.<br>- API 호출 중에는 버튼을 비활성화하고 로딩 스피너를 표시합니다. |
| **API Request** | `POST /api/v1/users/face` (Content-Type: `multipart/form-data`) |
| **Backend Logic** | - 전송된 이미지에서 FaceNet 모델을 통해 512차원 임베딩 벡터를 추출합니다.<br>- 추출된 벡터를 `users` 테이블의 `face_embedding` 컬럼에 저장합니다.<br>- 원본 이미지는 S3의 격리된 경로에 저장하거나, 정책에 따라 즉시 폐기합니다. |
| **Initial State** | 활성화 상태 |

**[4] 품질 검사 피드백**

| 구분 | 상세 구현 내용 |
| :--- | :--- |
| **Event** | 카메라 스트림 분석 (실시간) |
| **Frontend Logic** | - (선택사항) TensorFlow.js나 다른 클라이언트 측 AI 라이브러리를 사용하여 실시간으로 프레임을 분석합니다.<br>- 얼굴이 중앙에 있는지, 조도가 적절한지 등을 판단하여 피드백 메시지를 동적으로 변경합니다.<br>- 서버 측 품질 검사 실패 시, API 응답 메시지를 이 영역에 표시합니다. |
| **API Request** | 없음 (클라이언트 측 분석) 또는 `POST /api/v1/users/face`의 응답 처리 |
| **Backend Logic** | - 안면 등록 API에서 이미지 품질 검사를 수행하고, 기준 미달 시 에러 코드와 메시지(예: `FACE_NOT_CENTERED`)를 반환합니다. |
| **Initial State** | "얼굴을 화면 중앙의 원에 맞춰주세요." |

---

### 2.2. 전자 계약 서명 (Electronic Contract Signing)

**화면 개요**: 노무자가 본인에게 할당된 근로계약서의 내용을 확인하고, Canvas 기반 서명 패드에 서명하여 계약을 체결하는 페이지입니다.

**UI 화면**: `[UI Image: 노무자 - 계약서 확인 및 서명 패드 화면]`

#### UI 요소별 명세

| 번호 | UI 요소 | 설명 |
| :--- | :--- | :--- |
| 1 | **계약서 내용 뷰어** | 서버로부터 받은 계약서 본문(HTML 또는 PDF)을 표시하는 영역입니다. 스크롤이 가능해야 합니다. |
| 2 | **서명 패드 (Canvas)** | 사용자가 터치 또는 마우스로 서명을 입력할 수 있는 흰색 배경의 영역입니다. |
| 3 | **다시 서명 버튼** | 서명 패드의 내용을 모두 지우고 다시 서명할 수 있도록 합니다. |
| 4 | **동의 및 서명 완료 버튼** | 계약 내용에 동의하고, 입력된 서명을 최종 제출하여 계약을 완료합니다. |

#### 상세 구현 명세

**[1] 계약서 내용 뷰어**

| 구분 | 상세 구현 내용 |
| :--- | :--- |
| **Event** | 페이지 로드 시 (`useEffect`) |
| **Frontend Logic** | - 계약서 상세 조회 API를 호출하여 계약서 내용을 가져옵니다.<br>- 응답받은 HTML을 `dangerouslySetInnerHTML`을 사용하여 렌더링하거나, PDF.js와 같은 라이브러리를 사용하여 PDF를 렌더링합니다. |
| **API Request** | `GET /api/v1/contracts/{contractId}` |
| **Backend Logic** | - `tenant_id`와 `contractId`를 기준으로 계약서를 조회합니다.<br>- 계약서 템플릿과 사용자 정보를 조합하여 완전한 계약서 내용을 HTML 또는 PDF 형태로 생성하여 반환합니다. |
| **Initial State** | 로딩 스피너 표시 |

**[2] 서명 패드 (Canvas)**

| 구분 | 상세 구현 내용 |
| :--- | :--- |
| **Event** | `onMouseDown`, `onMouseMove`, `onMouseUp`, `onTouchStart`, `onTouchMove`, `onTouchEnd` |
| **Frontend Logic** | - `react-signature-canvas` 또는 유사 라이브러리를 사용하여 서명 패드를 구현합니다.<br>- 터치 및 마우스 이벤트에 따라 Canvas에 선을 그려 서명 궤적을 렌더링합니다. |
| **API Request** | 없음 |
| **Backend Logic** | 없음 |
| **Initial State** | 비어있는 흰색 캔버스 |

**[3] 다시 서명 버튼**

| 구분 | 상세 구현 내용 |
| :--- | :--- |
| **Event** | `onClick` |
| **Frontend Logic** | - 서명 패드 라이브러리의 `clear()` 메서드를 호출하여 Canvas의 내용을 모두 지웁니다. |
| **API Request** | 없음 |
| **Backend Logic** | 없음 |
| **Initial State** | 활성화 상태 |

**[4] 동의 및 서명 완료 버튼**

| 구분 | 상세 구현 내용 |
| :--- | :--- |
| **Event** | `onClick` |
| **Frontend Logic** | - 서명 패드가 비어있는지 확인합니다. 비어있으면 "서명을 입력해주세요"라는 알림을 표시합니다.<br>- `getTrimmedCanvas().toDataURL('image/png')`를 호출하여 서명 이미지를 Base64 인코딩된 문자열로 추출합니다.<br>- 추출된 Base64 데이터를 포함하여 계약 서명 API를 호출합니다.<br>- API 호출 성공 시, "계약이 완료되었습니다" 메시지를 표시하고 이전 페이지로 이동합니다. |
| **API Request** | `POST /api/v1/contracts/{contractId}/sign` (Payload: `{ "signatureImage": "data:image/png;base64,..." }`) |
| **Backend Logic** | - 전송받은 Base64 서명 이미지를 디코딩하여 이미지 파일로 변환합니다.<br>- iText 또는 PDFBox 라이브러리를 사용하여 기존 계약서 PDF 템플릿의 특정 좌표에 서명 이미지를 합성합니다.<br>- 합성된 PDF에 법적 효력을 위한 타임스탬프(TSA)를 추가합니다.<br>- 최종 PDF 파일을 S3에 업로드하고, 계약 상태를 `SIGNED`로 변경합니다. |
| **Initial State** | 활성화 상태 |

---

## 3. 공통 기술 명세

### 3.1. 멀티테넌트 데이터 격리 (Shared Database, Shared Schema)

**구현 목표**: 모든 API 요청에서 현재 사용자가 속한 테넌트(회사)의 데이터만 접근 가능하도록 강제하여, 다른 테넌트의 데이터가 노출되거나 수정되는 것을 원천적으로 차단합니다.

**핵심 기술**: **Hibernate Filter**

**동작 원리**:
1.  **컨텍스트 설정**: 사용자가 로그인하면 JWT 토큰에 `tenant_id`가 포함됩니다. API 요청 시 `TenantIdInterceptor`가 이 `tenant_id`를 `ThreadLocal` 변수인 `TenantContext`에 저장합니다.
2.  **필터 자동 활성화**: 서비스 로직이 `@Transactional`과 함께 호출되면, AOP로 구현된 `HibernateFilterAspect`가 동작하여 현재 세션에 `tenantFilter`를 활성화하고, `TenantContext`의 `tenant_id`를 필터 파라미터로 바인딩합니다.
3.  **쿼리 자동 변경**: 개발자가 `siteRepository.findAll()`과 같은 일반적인 JPA 메서드를 호출하면, Hibernate는 `@Filter`가 적용된 엔티티에 대해 `WHERE tenant_id = ?` 조건을 자동으로 추가하여 SQL을 생성합니다.

**구현 코드 예시**:

-   **엔티티 설정** (`Site.java`)

    ```java
    @Entity
    @FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = "long"), defaultCondition = "tenant_id = :tenantId")
    @Filter(name = "tenantFilter")
    public class Site { ... }
    ```

-   **인터셉터** (`TenantIdInterceptor.java`)

    ```java
    // preHandle: JWT에서 tenant_id 추출 -> TenantContext.setTenantId(tenantId)
    // afterCompletion: TenantContext.clear()
    ```

**개발자 유의사항**:
-   데이터 격리가 필요한 모든 엔티티에 `@Filter(name = "tenantFilter")` 어노테이션을 반드시 추가해야 합니다.
-   네이티브 쿼리(Native Query) 사용은 필터가 적용되지 않으므로, 사용을 지양하고 JPA `Specification`이나 `QueryDSL` 사용을 권장합니다.

### 3.2. API 보안 및 인증 (Spring Security)

**구현 목표**: 역할 기반 접근 제어(RBAC)를 통해 각 API 엔드포인트에 대한 접근 권한을 엄격히 통제하고, 인증된 사용자만 시스템을 이용할 수 있도록 합니다.

**핵심 기술**: **Spring Security, JWT (JSON Web Token)**

**동작 원리**:
1.  **로그인 및 토큰 발급**: 사용자가 로그인에 성공하면, 서버는 사용자의 ID, 역할(`Role`), `tenant_id` 등의 정보를 담은 JWT를 생성하여 클라이언트에 전달합니다.
2.  **요청 시 토큰 검증**: 클라이언트는 API 요청 시마다 `Authorization` 헤더에 `Bearer {JWT}` 형태로 토큰을 포함하여 보냅니다.
3.  **보안 필터 체인**: Spring Security의 `JwtAuthenticationFilter`가 요청을 가로채 토큰의 유효성을 검증하고, `SecurityContextHolder`에 인증된 사용자 정보를 설정합니다.
4.  **권한 부여**: `@PreAuthorize` 어노테이션을 사용하여 각 API 엔드포인트에 필요한 역할을 명시합니다. 예를 들어, 현장 개설 API는 `"hasRole('HQ_ADMIN')"`과 같이 설정하여 본사 관리자만 호출할 수 있도록 제한합니다.

**구현 코드 예시**:

-   **API 권한 설정** (`SiteController.java`)

    ```java
    @PostMapping("/sites")
    @PreAuthorize("hasRole('HQ_ADMIN')")
    public ResponseEntity<Site> createSite(@RequestBody CreateSiteRequest request) { ... }
    ```

-   **보안 설정** (`SecurityConfig.java`)

    ```java
    // http.authorizeRequests().antMatchers("/api/v1/**").authenticated() ...
    // http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
    ```

**개발자 유의사항**:
-   새로운 API를 추가할 때는 반드시 `@PreAuthorize`를 사용하여 적절한 역할 기반 접근 제어를 설정해야 합니다.
-   사용자 비밀번호와 같은 민감 정보는 반드시 `BCryptPasswordEncoder`를 사용하여 해시 처리 후 저장해야 합니다.
