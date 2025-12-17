# 모바일 네이티브앱 개발 최적화 전략 (v2.0)

**문서 버전:** 2.0  
**작성일:** 2025년 12월 17일  
**작성자:** 개발 PM (Gemini)  
**기반 문서:** `docs/PRD.txt`

---

## 1. 개요

PC웹과 모바일 웹앱은 React 기반 반응형으로 통합 개발하고, 모바일 네이티브앱은 **Capacitor**를 사용하여 하이브리드 형태로 구축합니다.

---

## 2. 모바일 네이티브앱 개발 방안

### 2.1. 최종 추천: **Capacitor**

#### 선정 이유
1.  **React 생태계 호환**: React 웹 애플리케이션을 그대로 패키징 가능
2.  **단일 코드베이스**: 웹/Webapp/Android/iOS를 하나의 소스(`frontend/web`)로 관리
3.  **네이티브 기능**: 카메라, 위치, 푸시 알림 등을 Capacitor Plugin으로 쉽게 연동

---

## 3. 아키텍처 설계

### 3.1. 통합 개발 구조
```
┌─────────────────────────────────────────┐
│         React (공통 코드)                │
│  - Features (Components/Hooks)          │
│  - Stores (Zustand)                     │
│  - Query (TanStack Query)               │
└─────────────────────────────────────────┘
           │                    │
    ┌──────▼──────┐    ┌────────▼────────┐
    │  PC/Web App │    │  Mobile Web App │
    └─────────────┘    └─────────────────┘
                   │
            ┌──────▼──────────┐
            │  Capacitor      │
            └──────┬──────────┘
                   │
        ┌──────────┴─────────┐
        │                    │
┌───────▼──────┐    ┌────────▼──────┐
│  Android App │    │   iOS App     │
└──────────────┘    └───────────────┘
```

### 3.2. 코드 공유 전략
- **Responsive Design**: Tailwind CSS의 Breakpoint(`md:`)를 활용하여 PC/Mobile 뷰 분기
- **Routing**: `react-router-dom` 사용, 모바일 전용 라우트 필요 시 별도 설정
- **Native Check**: `Capacitor.isNativePlatform()` 등을 사용하여 네이티브 환경 감지

---

## 4. Capacitor 설정 가이드

### 4.1. 설치 및 초기화
```bash
cd frontend/web
npm install @capacitor/core @capacitor/cli
npx cap init SmartCON com.smartcon.app

# 플랫폼 추가
npm install @capacitor/android @capacitor/ios
npx cap add android
npx cap add ios
```

### 4.2. 주요 플러그인
```bash
# 카메라 (안면인식)
npm install @capacitor/camera

# 위치 정보 (GPS)
npm install @capacitor/geolocation

# 네트워크 상태
npm install @capacitor/network

# 로컬 알림
npm install @capacitor/local-notifications
```

---

## 5. 네이티브 기능 구현 예시 (React Hook)

```tsx
import { Camera, CameraResultType } from '@capacitor/camera';

export const useCamera = () => {
  const takePhoto = async () => {
    const image = await Camera.getPhoto({
      quality: 90,
      allowEditing: false,
      resultType: CameraResultType.Uri
    });
    return image.webPath; // 이미지 경로 반환
  };

  return { takePhoto };
};
```

---

**문서 끝**
