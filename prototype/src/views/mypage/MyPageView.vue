<template>
  <v-container>
    <v-row>
        <v-col cols="12">
          <h1 class="text-h4 mb-4">마이페이지</h1>
        </v-col>
      </v-row>
      
      <!-- 사용자 정보 -->
      <v-row>
        <v-col cols="12" md="6">
          <v-card>
            <v-card-title>사용자 정보</v-card-title>
            <v-card-text>
              <v-list>
                <v-list-item>
                  <v-list-item-title>이름</v-list-item-title>
                  <v-list-item-subtitle>{{ userStore.currentUser?.name }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>이메일</v-list-item-title>
                  <v-list-item-subtitle>{{ userStore.currentUser?.email }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>전화번호</v-list-item-title>
                  <v-list-item-subtitle>{{ userStore.currentUser?.phone }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>안면 등록</v-list-item-title>
                  <v-list-item-subtitle>
                    <v-chip :color="userStore.currentUser?.isFaceRegistered ? 'success' : 'warning'">
                      {{ userStore.currentUser?.isFaceRegistered ? '등록됨' : '미등록' }}
                    </v-chip>
                  </v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
      
      <!-- 메뉴 -->
      <v-row class="mt-4">
        <v-col cols="12" md="6">
          <v-card @click="goToAttendance" hover>
            <v-card-title>
              <v-icon class="mr-2">mdi-calendar</v-icon>
              출역 현황
            </v-card-title>
          </v-card>
        </v-col>
        <v-col cols="12" md="6">
          <v-card @click="goToDispute" hover>
            <v-card-title>
              <v-icon class="mr-2">mdi-alert-circle</v-icon>
              이의 제기
            </v-card-title>
          </v-card>
        </v-col>
      </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

function goToAttendance() {
  router.push('/app/mypage/attendance')
}

function goToDispute() {
  router.push('/app/mypage/dispute')
}
</script>

