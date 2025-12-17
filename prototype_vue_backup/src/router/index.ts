import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

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
      meta: { requiresAuth: false },
    },
    {
      path: '/role-select',
      name: 'RoleSelect',
      component: () => import('@/views/auth/RoleSelectView.vue'),
      meta: { requiresAuth: true, requiresRole: false },
    },
    {
      path: '/app',
      component: () => import('@/views/LayoutView.vue'),
      meta: { requiresAuth: true, requiresRole: true },
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
        },
        {
          path: 'attendance/check-in',
          name: 'CheckIn',
          component: () => import('@/views/attendance/CheckInView.vue'),
        },
        {
          path: 'attendance/list',
          name: 'AttendanceList',
          component: () => import('@/views/attendance/AttendanceListView.vue'),
        },
        {
          path: 'attendance/manual-approve',
          name: 'ManualApprove',
          component: () => import('@/views/attendance/ManualApproveView.vue'),
        },
        {
          path: 'face/register',
          name: 'FaceRegister',
          component: () => import('@/views/face/FaceRegisterView.vue'),
        },
        {
          path: 'reports',
          name: 'Reports',
          component: () => import('@/views/report/ReportListView.vue'),
        },
        {
          path: 'reports/create',
          name: 'ReportCreate',
          component: () => import('@/views/report/ReportCreateView.vue'),
        },
        {
          path: 'reports/:id',
          name: 'ReportDetail',
          component: () => import('@/views/report/ReportDetailView.vue'),
        },
        {
          path: 'mypage',
          name: 'MyPage',
          component: () => import('@/views/mypage/MyPageView.vue'),
        },
        {
          path: 'mypage/attendance',
          name: 'MyAttendance',
          component: () => import('@/views/mypage/MyAttendanceView.vue'),
        },
        {
          path: 'mypage/dispute',
          name: 'Dispute',
          component: () => import('@/views/mypage/DisputeView.vue'),
        },
        {
          path: 'mypage/dispute/create',
          name: 'DisputeCreate',
          component: () => import('@/views/mypage/DisputeCreateView.vue'),
        },
      ],
    },
  ],
})

// 프로토타입용: 역할 선택 체크
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth) {
    // 프로토타입에서는 인증 체크 생략 (항상 로그인된 것으로 간주)
    // 역할 선택이 필요한 경우 체크
    if (to.meta.requiresRole && !userStore.selectedRole) {
      // 다중 역할을 가진 경우 역할 선택 화면으로 이동
      if (userStore.hasMultipleRoles) {
        next('/role-select')
      } else if (userStore.currentUser && userStore.currentUser.roles.length === 1) {
        // 단일 역할인 경우 자동 선택
        userStore.selectRole(userStore.currentUser.roles[0])
        next()
      } else {
        next('/role-select')
      }
    } else {
      next()
    }
  } else {
    // 로그인 화면에서 로그인 후 대시보드로 리다이렉트
    if (to.name === 'Login' && userStore.isLoggedIn && userStore.selectedRole) {
      next('/app/dashboard')
    } else {
      next()
    }
  }
})

export default router
