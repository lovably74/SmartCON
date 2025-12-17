<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="10" md="8" lg="6">
        <v-card>
          <v-card-title class="text-h5 text-center pa-6">
            역할 선택
          </v-card-title>
          
          <v-card-text>
            <div class="text-center mb-6">
              <p class="text-body-1">
                {{ userStore.currentUser?.name }}님, 사용할 역할을 선택해주세요
              </p>
            </div>
            
            <v-row>
              <v-col
                v-for="role in userStore.currentUser?.roles"
                :key="role.roleCode"
                cols="12"
                md="6"
              >
                <v-card
                  :class="{ 'border-primary': selectedRole?.roleCode === role.roleCode }"
                  class="role-card"
                  @click="handleRoleSelect(role)"
                  hover
                >
                  <v-card-title>
                    <v-icon :color="getRoleColor(role.roleCode)" class="mr-2">
                      {{ getRoleIcon(role.roleCode) }}
                    </v-icon>
                    {{ role.roleName }}
                  </v-card-title>
                  
                  <v-card-text>
                    <div v-if="role.siteName" class="mb-2">
                      <v-icon size="small">mdi-map-marker</v-icon>
                      {{ role.siteName }}
                    </div>
                    <div v-if="role.teamName" class="mb-2">
                      <v-icon size="small">mdi-account-group</v-icon>
                      {{ role.teamName }}
                    </div>
                    <p class="text-caption text-grey">
                      {{ getRoleDescription(role.roleCode) }}
                    </p>
                  </v-card-text>
                  
                  <v-card-actions>
                    <v-spacer />
                    <v-btn
                      color="primary"
                      @click.stop="handleRoleSelect(role)"
                    >
                      선택
                    </v-btn>
                  </v-card-actions>
                </v-card>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { UserRole } from '@/mock-data/users'

const router = useRouter()
const userStore = useUserStore()

const selectedRole = computed(() => userStore.selectedRole)

function getRoleIcon(roleCode: string): string {
  const icons: Record<string, string> = {
    ADMIN: 'mdi-shield-crown',
    SITE_ADMIN: 'mdi-account-tie',
    TEAM_LEADER: 'mdi-account-group',
    WORKER: 'mdi-account-hard-hat',
  }
  return icons[roleCode] || 'mdi-account'
}

function getRoleColor(roleCode: string): string {
  const colors: Record<string, string> = {
    ADMIN: 'purple',
    SITE_ADMIN: 'blue',
    TEAM_LEADER: 'green',
    WORKER: 'orange',
  }
  return colors[roleCode] || 'grey'
}

function getRoleDescription(roleCode: string): string {
  const descriptions: Record<string, string> = {
    ADMIN: '전사 현장 관리 및 전체 통계 조회',
    SITE_ADMIN: '담당 현장 출역 모니터링 및 예외 승인',
    TEAM_LEADER: '소속 팀원 관리 및 작업일보 작성',
    WORKER: '출역 체크 및 출역 이력 조회',
  }
  return descriptions[roleCode] || ''
}

function handleRoleSelect(role: UserRole) {
  userStore.selectRole(role)
  router.push('/app/dashboard')
}
</script>

<style scoped>
.fill-height {
  min-height: 100vh;
}

.role-card {
  cursor: pointer;
  transition: all 0.3s;
}

.role-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.border-primary {
  border: 2px solid rgb(25, 118, 210);
}
</style>

