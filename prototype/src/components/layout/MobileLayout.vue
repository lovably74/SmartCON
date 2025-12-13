<template>
  <v-app>
    <v-app-bar>
      <v-app-bar-title>
        {{ pageTitle }}
      </v-app-bar-title>
      
      <template v-slot:append>
        <v-menu>
          <template v-slot:activator="{ props }">
            <v-btn icon v-bind="props">
              <v-icon>mdi-menu</v-icon>
            </v-btn>
          </template>
          <v-list>
            <v-list-item
              v-if="userStore.hasMultipleRoles"
              @click="showRoleDialog = true"
            >
              <v-list-item-title>역할 전환</v-list-item-title>
            </v-list-item>
            <v-list-item @click="handleLogout">
              <v-list-item-title>로그아웃</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </template>
    </v-app-bar>
    
    <v-main>
      <router-view />
    </v-main>
    
    <v-bottom-navigation
      v-model="bottomNav"
      fixed
      color="primary"
      class="mobile-only"
    >
      <v-btn
        v-for="item in bottomMenuItems"
        :key="item.value"
        :to="item.to"
        :value="item.value"
      >
        <v-icon>{{ item.icon }}</v-icon>
        <span>{{ item.title }}</span>
      </v-btn>
    </v-bottom-navigation>
    
    <!-- 역할 전환 다이얼로그 -->
    <v-dialog v-model="showRoleDialog" max-width="500">
      <v-card>
        <v-card-title>역할 선택</v-card-title>
        <v-card-text>
          <v-list>
            <v-list-item
              v-for="role in userStore.currentUser?.roles"
              :key="role.roleCode"
              @click="handleRoleSelect(role)"
            >
              <v-list-item-title>{{ role.roleName }}</v-list-item-title>
              <v-list-item-subtitle v-if="role.siteName">
                {{ role.siteName }}
              </v-list-item-subtitle>
            </v-list-item>
          </v-list>
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-app>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { UserRole } from '@/mock-data/users'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const bottomNav = ref('dashboard')
const showRoleDialog = ref(false)

function getCurrentRouteValue(): string {
  const path = route.path
  if (path.includes('/dashboard')) return 'dashboard'
  if (path.includes('/check-in')) return 'checkin'
  if (path.includes('/attendance/list')) return 'attendance'
  if (path.includes('/reports')) return 'reports'
  if (path.includes('/mypage')) return 'mypage'
  return 'dashboard'
}

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    Dashboard: '대시보드',
    CheckIn: '출역 체크',
    AttendanceList: '출역 현황',
    ManualApprove: '예외 승인',
    FaceRegister: '안면 등록',
    Reports: '작업일보',
    ReportCreate: '작업일보 작성',
    MyPage: '마이페이지',
    MyAttendance: '출역 현황',
    Dispute: '이의 제기',
  }
  return titles[route.name as string] || 'SmartCON Lite'
})

const bottomMenuItems = computed(() => {
  const items: Array<{ title: string; icon: string; to: string; value: string; roles?: string[] }> = [
    { title: '대시보드', icon: 'mdi-view-dashboard', to: '/app/dashboard', value: 'dashboard' },
    { title: '출역 체크', icon: 'mdi-account-check', to: '/app/attendance/check-in', value: 'checkin', roles: ['WORKER', 'TEAM_LEADER'] },
    { title: '출역 현황', icon: 'mdi-format-list-bulleted', to: '/app/attendance/list', value: 'attendance' },
    { title: '작업일보', icon: 'mdi-file-document', to: '/app/reports', value: 'reports', roles: ['TEAM_LEADER', 'SITE_ADMIN', 'ADMIN'] },
    { title: '마이페이지', icon: 'mdi-account', to: '/app/mypage', value: 'mypage' },
  ]
  
  // 역할별 필터링
  if (userStore.selectedRole) {
    return items.filter(item => {
      if (!item.roles) return true
      return item.roles.includes(userStore.selectedRole!.roleCode)
    })
  }
  return items
})

function handleRoleSelect(role: UserRole) {
  userStore.selectRole(role)
  showRoleDialog.value = false
  router.push('/app/dashboard')
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

