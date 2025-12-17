import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, UserRole } from '@/mock-data/users'
import { mockUsers } from '@/mock-data/users'

export const useUserStore = defineStore('user', () => {
  // 현재 사용자 (프로토타입: 첫 번째 사용자로 초기화)
  const currentUser = ref<User | null>(mockUsers[0])
  
  // 선택된 역할
  const selectedRole = ref<UserRole | null>(null)
  
  // 로그인 상태
  const isLoggedIn = computed(() => currentUser.value !== null)
  
  // 다중 역할 여부
  const hasMultipleRoles = computed(() => {
    return currentUser.value ? currentUser.value.roles.length > 1 : false
  })
  
  // 역할별 권한 체크
  const isAdmin = computed(() => selectedRole.value?.roleCode === 'ADMIN')
  const isSiteAdmin = computed(() => selectedRole.value?.roleCode === 'SITE_ADMIN')
  const isTeamLeader = computed(() => selectedRole.value?.roleCode === 'TEAM_LEADER')
  const isWorker = computed(() => selectedRole.value?.roleCode === 'WORKER')
  
  // 사용자 선택 (프로토타입용)
  function selectUser(userId: number) {
    const user = mockUsers.find(u => u.id === userId)
    if (user) {
      currentUser.value = user
      // 단일 역할인 경우 자동 선택
      if (user.roles.length === 1) {
        selectRole(user.roles[0])
      } else {
        selectedRole.value = null
      }
    }
  }
  
  // 역할 선택
  function selectRole(role: UserRole) {
    selectedRole.value = role
    if (currentUser.value) {
      currentUser.value.currentRole = role
    }
  }
  
  // 로그인 (프로토타입용)
  function login(userId: number) {
    selectUser(userId)
  }
  
  // 로그아웃
  function logout() {
    currentUser.value = null
    selectedRole.value = null
  }
  
  return {
    currentUser,
    selectedRole,
    isLoggedIn,
    hasMultipleRoles,
    isAdmin,
    isSiteAdmin,
    isTeamLeader,
    isWorker,
    selectUser,
    selectRole,
    login,
    logout,
  }
})

