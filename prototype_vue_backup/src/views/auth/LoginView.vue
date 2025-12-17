<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="4">
        <v-card>
          <v-card-title class="text-h5 text-center pa-6">
            SmartCON Lite
          </v-card-title>
          
          <v-card-text>
            <div class="text-center mb-6">
              <p class="text-body-1">프로토타입 모드</p>
              <p class="text-caption text-grey">사용자를 선택하여 로그인하세요</p>
            </div>
            
            <v-list>
              <v-list-item
                v-for="user in mockUsers"
                :key="user.id"
                @click="handleLogin(user.id)"
                class="mb-2"
              >
                <template v-slot:prepend>
                  <v-avatar color="primary">
                    <span class="text-white">{{ user.name.charAt(0) }}</span>
                  </v-avatar>
                </template>
                
                <v-list-item-title>{{ user.name }}</v-list-item-title>
                <v-list-item-subtitle>
                  {{ user.email }}
                  <br>
                  <span class="text-caption">
                    역할: {{ user.roles.map(r => r.roleName).join(', ') }}
                  </span>
                </v-list-item-subtitle>
              </v-list-item>
            </v-list>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { mockUsers } from '@/mock-data/users'

const router = useRouter()
const userStore = useUserStore()

function handleLogin(userId: number) {
  userStore.login(userId)
  
  // 단일 역할인 경우 자동 선택 후 대시보드로 이동
  const user = mockUsers.find(u => u.id === userId)
  if (user) {
    if (user.roles.length === 1) {
      userStore.selectRole(user.roles[0])
      router.push('/app/dashboard')
    } else {
      // 다중 역할인 경우 역할 선택 화면으로 이동
      router.push('/role-select')
    }
  }
}
</script>

<style scoped>
.fill-height {
  min-height: 100vh;
}
</style>

