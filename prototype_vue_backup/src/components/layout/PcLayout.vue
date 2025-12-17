<template>
  <v-app>
    <v-navigation-drawer
      v-model="drawer"
      permanent
      location="left"
      width="250"
      class="desktop-only"
    >
      <v-list>
        <v-list-item>
          <v-list-item-title class="text-h6">
            SmartCON Lite
          </v-list-item-title>
        </v-list-item>
        
        <v-divider />
        
        <!-- 역할 전환 -->
        <v-list-item v-if="userStore.hasMultipleRoles" @click="showRoleDialog = true">
          <template v-slot:prepend>
            <v-icon>mdi-account-switch</v-icon>
          </template>
          <v-list-item-title>역할 전환</v-list-item-title>
          <v-list-item-subtitle v-if="userStore.selectedRole">
            {{ userStore.selectedRole.roleName }}
          </v-list-item-subtitle>
        </v-list-item>
        
        <v-divider v-if="userStore.hasMultipleRoles" />
        
        <!-- 메뉴 항목 -->
        <v-list-item
          v-for="item in menuItems"
          :key="item.title"
          :to="item.to"
          :prepend-icon="item.icon"
        >
          <v-list-item-title>{{ item.title }}</v-list-item-title>
        </v-list-item>
      </v-list>
      
      <template v-slot:append>
        <v-list>
          <v-list-item @click="handleLogout">
            <template v-slot:prepend>
              <v-icon>mdi-logout</v-icon>
            </template>
            <v-list-item-title>로그아웃</v-list-item-title>
          </v-list-item>
        </v-list>
      </template>
    </v-navigation-drawer>
    
    <v-app-bar class="desktop-only">
      <v-app-bar-title>
        {{ pageTitle }}
      </v-app-bar-title>
    </v-app-bar>
    
    <v-main>
      <router-view />
    </v-main>
    
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

const drawer = ref(true)
const showRoleDialog = ref(false)

function isActiveRoute(path: string): boolean {
  return route.path === path
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

const menuItems = computed(() => {
  const items: Array<{ title: string; icon: string; to: string; roles?: string[] }> = [
    { title: '대시보드', icon: 'mdi-view-dashboard', to: '/app/dashboard' },
    { title: '출역 체크', icon: 'mdi-account-check', to: '/app/attendance/check-in', roles: ['WORKER', 'TEAM_LEADER'] },
    { title: '출역 현황', icon: 'mdi-format-list-bulleted', to: '/app/attendance/list' },
    { title: '예외 승인', icon: 'mdi-check-circle', to: '/app/attendance/manual-approve', roles: ['ADMIN', 'SITE_ADMIN'] },
    { title: '안면 등록', icon: 'mdi-face-recognition', to: '/app/face/register' },
    { title: '작업일보', icon: 'mdi-file-document', to: '/app/reports', roles: ['TEAM_LEADER', 'SITE_ADMIN', 'ADMIN'] },
    { title: '마이페이지', icon: 'mdi-account', to: '/app/mypage' },
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

