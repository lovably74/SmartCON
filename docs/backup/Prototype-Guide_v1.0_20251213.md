# 프로토타입 개발 가이드

**문서 버전:** 1.0  
**작성일:** 2025년 12월 13일  
**작성자:** 개발 PM (Gemini)

---

## 1. 프로토타입 개발 목적

### 1.1. 목적
- 실제 데이터 없이 화면 위주로 구현
- 모든 기능을 링크/클릭으로 동작하도록 구현
- 실제 데이터가 있는 것처럼 보이게 목업 데이터 사용
- 사용자 피드백 수집 및 요구사항 검증

### 1.2. 개발 원칙
- **빠른 개발**: 실제 API 연동 없이 화면만 구현
- **실제처럼**: 목업 데이터로 실제 데이터처럼 보이게
- **모든 기능**: PRD의 모든 화면 구현
- **반응형**: PC/모바일 반응형 디자인 적용

---

## 2. 프로젝트 설정

### 2.1. 프로젝트 초기화

```bash
cd prototype
npm create vite@latest . -- --template vue-ts
npm install
```

### 2.2. 필수 패키지 설치

```bash
# UI 라이브러리
npm install vuetify @mdi/font

# 라우팅
npm install vue-router@4

# 상태 관리 (선택사항)
npm install pinia

# 날짜 처리
npm install date-fns

# 달력 라이브러리 (출역 현황용)
npm install @fullcalendar/vue3 @fullcalendar/daygrid
```

### 2.3. Vuetify 설정

`src/plugins/vuetify.ts` 생성:

```typescript
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'

export default createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          primary: '#1976D2',
          secondary: '#424242',
          accent: '#82B1FF',
          error: '#FF5252',
          info: '#2196F3',
          success: '#4CAF50',
          warning: '#FFC107',
        },
      },
    },
  },
})
```

---

## 3. 목업 데이터 구조

### 3.1. 사용자 데이터

`src/mock-data/users.ts`:

```typescript
export interface User {
  id: number
  name: string
  role: 'ADMIN' | 'SITE_ADMIN' | 'TEAM_LEADER' | 'WORKER'
  email: string
  phone: string
  siteId?: number
  siteName?: string
  teamName?: string
  isFaceRegistered: boolean
}

export const mockUsers: User[] = [
  {
    id: 1,
    name: '홍길동',
    role: 'ADMIN',
    email: 'admin@smartcon.com',
    phone: '010-1234-5678',
    isFaceRegistered: true,
  },
  {
    id: 2,
    name: '김철수',
    role: 'SITE_ADMIN',
    email: 'site@smartcon.com',
    phone: '010-2345-6789',
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    isFaceRegistered: true,
  },
  // ... 더 많은 사용자 데이터
]
```

### 3.2. 현장 데이터

`src/mock-data/sites.ts`:

```typescript
export interface Site {
  id: number
  name: string
  code: string
  address: string
  managerId: number
  managerName: string
}

export const mockSites: Site[] = [
  {
    id: 1,
    name: '서울 강남구 건설 현장',
    code: 'SITE-001',
    address: '서울시 강남구 테헤란로 123',
    managerId: 2,
    managerName: '김철수',
  },
  // ... 더 많은 현장 데이터
]
```

### 3.3. 출역 기록 데이터

`src/mock-data/attendance.ts`:

```typescript
export interface AttendanceLog {
  id: number
  userId: number
  userName: string
  siteId: number
  siteName: string
  checkInTime: string
  status: 'NORMAL' | 'LATE'
  authType: 'FACE' | 'MANUAL'
  dailyManDay: number
  manualReason?: string
}

export const mockAttendanceLogs: AttendanceLog[] = [
  {
    id: 1,
    userId: 3,
    userName: '이영희',
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    checkInTime: '2025-12-13T08:30:00',
    status: 'NORMAL',
    authType: 'FACE',
    dailyManDay: 1.0,
  },
  // ... 더 많은 출역 기록
]
```

### 3.4. 작업일보 데이터

`src/mock-data/reports.ts`:

```typescript
export interface DailyReport {
  id: number
  siteId: number
  siteName: string
  teamLeaderId: number
  teamLeaderName: string
  workDate: string
  workTypeCode: string
  workContent: string
  photoUrls: string[]
  isApproved: boolean
}

export const mockReports: DailyReport[] = [
  {
    id: 1,
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    teamLeaderId: 4,
    teamLeaderName: '박민수',
    workDate: '2025-12-13',
    workTypeCode: 'CONCRETE',
    workContent: '1층 콘크리트 타설 작업 완료',
    photoUrls: ['/images/report1.jpg'],
    isApproved: false,
  },
  // ... 더 많은 작업일보
]
```

---

## 4. 화면 구현 가이드

### 4.1. 레이아웃 구조

#### PC 레이아웃 (`src/components/layout/PcLayout.vue`)

```vue
<template>
  <v-app>
    <v-navigation-drawer
      v-model="drawer"
      permanent
      location="left"
      width="250"
    >
      <!-- 좌측 메뉴 -->
    </v-navigation-drawer>
    
    <v-main>
      <router-view />
    </v-main>
  </v-app>
</template>
```

#### 모바일 레이아웃 (`src/components/layout/MobileLayout.vue`)

```vue
<template>
  <v-app>
    <v-main>
      <router-view />
    </v-main>
    
    <v-bottom-navigation
      v-model="value"
      fixed
      color="primary"
    >
      <!-- 하단 탭 메뉴 -->
    </v-bottom-navigation>
  </v-app>
</template>
```

### 4.2. 반응형 처리

`src/composables/useResponsive.ts`:

```typescript
import { computed } from 'vue'
import { useDisplay } from 'vuetify'

export function useResponsive() {
  const { mdAndUp } = useDisplay()
  
  const isMobile = computed(() => !mdAndUp.value)
  const isTablet = computed(() => mdAndUp.value && !lgAndUp.value)
  const isDesktop = computed(() => lgAndUp.value)
  
  return {
    isMobile,
    isTablet,
    isDesktop,
  }
}
```

### 4.3. 라우터 설정

`src/router/index.ts`:

```typescript
import { createRouter, createWebHistory } from 'vue-router'
import { mockUsers } from '@/mock-data/users'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/LoginView.vue'),
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/DashboardView.vue'),
      meta: { requiresAuth: true },
    },
    // ... 더 많은 라우트
  ],
})

// 프로토타입용: 항상 첫 번째 사용자로 로그인 처리
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    // 프로토타입에서는 인증 체크 생략
    next()
  } else {
    next()
  }
})

export default router
```

---

## 5. 주요 화면 구현 예시

### 5.1. 대시보드 화면

`src/views/dashboard/DashboardView.vue`:

```vue
<template>
  <v-container>
    <v-row>
      <v-col cols="12" md="3">
        <v-card>
          <v-card-title>전체 현장 수</v-card-title>
          <v-card-text>
            <div class="text-h2">{{ mockSites.length }}</div>
          </v-card-text>
        </v-card>
      </v-col>
      
      <v-col cols="12" md="3">
        <v-card>
          <v-card-title>금일 출역 인원</v-card-title>
          <v-card-text>
            <div class="text-h2">{{ todayAttendanceCount }}</div>
          </v-card-text>
        </v-card>
      </v-col>
      
      <!-- 더 많은 통계 카드 -->
    </v-row>
    
    <!-- 출역 현황 테이블/리스트 -->
    <v-row>
      <v-col cols="12">
        <AttendanceList :items="mockAttendanceLogs" />
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { mockSites } from '@/mock-data/sites'
import { mockAttendanceLogs } from '@/mock-data/attendance'
import AttendanceList from '@/components/features/AttendanceList.vue'

const todayAttendanceCount = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  return mockAttendanceLogs.filter(
    log => log.checkInTime.startsWith(today)
  ).length
})
</script>
```

### 5.2. 출역 체크 화면

`src/views/attendance/CheckInView.vue`:

```vue
<template>
  <v-container>
    <v-card>
      <v-card-title>출역 체크</v-card-title>
      <v-card-text>
        <v-btn
          color="primary"
          size="large"
          block
          @click="handleCheckIn"
        >
          출역 체크하기
        </v-btn>
        
        <!-- 프로토타입: 클릭 시 결과 화면으로 이동 -->
        <v-dialog v-model="showResult" max-width="500">
          <v-card>
            <v-card-title>출역 체크 완료</v-card-title>
            <v-card-text>
              <p>출역 시간: {{ checkInTime }}</p>
              <p>상태: 정상 출역</p>
            </v-card-text>
            <v-card-actions>
              <v-spacer />
              <v-btn @click="showResult = false">확인</v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const showResult = ref(false)
const checkInTime = ref('')

const handleCheckIn = () => {
  // 프로토타입: 실제 API 호출 없이 결과만 표시
  checkInTime.value = new Date().toLocaleTimeString()
  showResult.value = true
}
</script>
```

### 5.3. 안면 등록 화면

`src/views/face/FaceRegisterView.vue`:

```vue
<template>
  <v-container>
    <v-stepper v-model="step" :items="steps">
      <template v-slot:item.1>
        <v-card>
          <v-card-title>안면 등록 가이드</v-card-title>
          <v-card-text>
            <ul>
              <li>안경과 마스크를 제거해주세요</li>
              <li>밝은 곳에서 촬영해주세요</li>
              <li>얼굴이 프레임 안에 오도록 해주세요</li>
            </ul>
          </v-card-text>
          <v-card-actions>
            <v-spacer />
            <v-btn @click="step = 2">다음</v-btn>
          </v-card-actions>
        </v-card>
      </template>
      
      <template v-slot:item.2>
        <v-card>
          <v-card-title>카메라 권한 요청</v-card-title>
          <v-card-text>
            <p>카메라 접근 권한이 필요합니다.</p>
          </v-card-text>
          <v-card-actions>
            <v-btn @click="step = 1">이전</v-btn>
            <v-spacer />
            <v-btn color="primary" @click="step = 3">권한 허용</v-btn>
          </v-card-actions>
        </v-card>
      </template>
      
      <!-- 더 많은 스텝 -->
    </v-stepper>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const step = ref(1)
const steps = [
  '안내',
  '권한 요청',
  '촬영',
  '전송/분석',
  '결과',
]
</script>
```

---

## 6. 라우팅 및 네비게이션

### 6.1. 단순 링크 처리

모든 화면 전환은 Vue Router의 `router-link` 또는 `router.push()`를 사용:

```vue
<template>
  <v-btn @click="goToDashboard">대시보드로 이동</v-btn>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goToDashboard = () => {
  router.push('/dashboard')
}
</script>
```

### 6.2. 역할별 메뉴

`src/composables/useUser.ts`:

```typescript
import { ref } from 'vue'
import { mockUsers } from '@/mock-data/users'

export function useUser() {
  // 프로토타입: 첫 번째 사용자를 현재 사용자로 설정
  const currentUser = ref(mockUsers[0])
  
  const isAdmin = computed(() => currentUser.value.role === 'ADMIN')
  const isSiteAdmin = computed(() => currentUser.value.role === 'SITE_ADMIN')
  const isTeamLeader = computed(() => currentUser.value.role === 'TEAM_LEADER')
  const isWorker = computed(() => currentUser.value.role === 'WORKER')
  
  return {
    currentUser,
    isAdmin,
    isSiteAdmin,
    isTeamLeader,
    isWorker,
  }
}
```

---

## 7. 스타일링 가이드

### 7.1. 색상 시스템

- **정상 출역**: Green (#4CAF50)
- **지각**: Orange (#FF9800)
- **미출역**: Gray (#9E9E9E)
- **수동 승인**: Blue (#2196F3)

### 7.2. 반응형 브레이크포인트

- **Mobile**: ~767px
- **Tablet**: 768px ~ 1023px
- **Desktop**: 1024px ~

### 7.3. 터치 친화적 UI

- 모바일 버튼 최소 크기: 44x44px
- 터치 영역 충분히 확보
- 스크롤 가능한 영역 명확히 표시

---

## 8. 완료 검증 체크리스트

### 8.1. 화면 구현
- [ ] 모든 화면 구현 완료
- [ ] PC/모바일 반응형 적용
- [ ] 모든 링크/클릭 동작 확인

### 8.2. 데이터 표시
- [ ] 목업 데이터로 실제처럼 표시
- [ ] 날짜/시간 포맷팅 적용
- [ ] 상태별 색상 구분 적용

### 8.3. 사용자 경험
- [ ] 화면 전환 부드러움
- [ ] 로딩 상태 표시 (선택사항)
- [ ] 에러 메시지 표시 (선택사항)

### 8.4. 문서화
- [ ] 화면 캡처 수집
- [ ] 시연 영상 제작
- [ ] 사용자 피드백 수집 계획

---

## 9. 다음 단계

프로토타입 완료 후:

1. **사용자 피드백 수집**
   - 실제 사용자에게 시연
   - 피드백 수집 및 정리

2. **요구사항 검증**
   - PRD와 프로토타입 비교
   - 누락된 화면/기능 확인

3. **실제 개발 시작**
   - Phase 1부터 순차적으로 개발
   - 프로토타입 코드는 참고용으로 활용

---

**문서 끝**

