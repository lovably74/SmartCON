# SmartCON Lite 모바일 네이티브 앱 아키텍처

**문서 버전:** 1.0  
**작성일:** 2025년 12월 23일  
**작성자:** Kiro AI Assistant  
**기반 문서:** PRD v2.8, 상세기능명세서 v3.0, Frontend-Architecture v1.0

---

## 1. 모바일 네이티브 앱 개요

### 1.1 아키텍처 원칙
- **Hybrid App Strategy**: React Native 기반 크로스 플랫폼 개발
- **Code Sharing**: 웹과 모바일 간 비즈니스 로직 공유
- **Native Performance**: 네이티브 기능 최적화 (카메라, GPS, 푸시알림)
- **Offline-First**: 오프라인 우선 설계로 현장 환경 대응
- **Progressive Enhancement**: 웹앱에서 네이티브 앱으로 점진적 향상

### 1.2 기술 스택 선택
- **Framework**: React Native 0.73+ (New Architecture)
- **Navigation**: React Navigation 6.x
- **State Management**: Zustand + React Query (웹과 동일)
- **UI Library**: NativeBase + Custom Design System
- **Database**: SQLite (로컬) + Realm (동기화)
- **Push Notifications**: Firebase Cloud Messaging
- **Analytics**: Firebase Analytics + Crashlytics
- **Build**: Expo Application Services (EAS)

### 1.3 웹앱과의 차별화 포인트
- **현장 특화 기능**: 오프라인 출역 체크, GPS 기반 현장 인증
- **네이티브 성능**: 카메라 최적화, 백그라운드 동기화
- **사용자 경험**: 네이티브 UI/UX, 제스처, 햅틱 피드백
- **푸시 알림**: 실시간 작업 요청, 출역 알림, 긴급 공지

---

## 2. 프로젝트 구조

### 2.1 전체 디렉토리 구조
```
mobile-app/
├── src/
│   ├── components/                 # 재사용 가능한 컴포넌트
│   │   ├── ui/                     # 기본 UI 컴포넌트
│   │   ├── forms/                  # 폼 컴포넌트
│   │   ├── camera/                 # 카메라 관련 컴포넌트
│   │   └── common/                 # 공통 컴포넌트
│   ├── screens/                    # 화면 컴포넌트 (역할별)
│   │   ├── auth/                   # 인증 화면
│   │   ├── onboarding/             # 온보딩 화면
│   │   ├── site/                   # 현장 관리자 화면
│   │   ├── team/                   # 팀장 화면
│   │   └── worker/                 # 노무자 화면
│   ├── navigation/                 # 네비게이션 설정
│   │   ├── AppNavigator.tsx        # 메인 네비게이터
│   │   ├── AuthNavigator.tsx       # 인증 네비게이터
│   │   └── TabNavigator.tsx        # 탭 네비게이터
│   ├── services/                   # 서비스 계층
│   │   ├── api/                    # API 클라이언트 (웹과 공유)
│   │   ├── storage/                # 로컬 저장소
│   │   ├── sync/                   # 데이터 동기화
│   │   ├── camera/                 # 카메라 서비스
│   │   ├── location/               # 위치 서비스
│   │   └── notification/           # 푸시 알림 서비스
│   ├── stores/                     # 상태 관리 (웹과 공유)
│   │   ├── authStore.ts
│   │   ├── attendanceStore.ts
│   │   └── syncStore.ts
│   ├── hooks/                      # 커스텀 훅
│   │   ├── useCamera.ts
│   │   ├── useLocation.ts
│   │   ├── useOfflineSync.ts
│   │   └── usePushNotification.ts
│   ├── utils/                      # 유틸리티 (웹과 공유)
│   │   ├── format/
│   │   ├── validation/
│   │   └── constants/
│   ├── types/                      # TypeScript 타입 (웹과 공유)
│   └── assets/                     # 정적 자산
├── android/                        # Android 네이티브 코드
├── ios/                           # iOS 네이티브 코드
├── shared/                        # 웹과 공유하는 코드
│   ├── api/                       # API 클라이언트
│   ├── types/                     # 타입 정의
│   ├── utils/                     # 유틸리티 함수
│   └── stores/                    # 상태 관리
├── app.json                       # Expo 설정
├── package.json
└── tsconfig.json
```