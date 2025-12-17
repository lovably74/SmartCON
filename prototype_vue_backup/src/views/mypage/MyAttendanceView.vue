<template>
  <v-container>
    <v-row>
        <v-col cols="12">
          <h1 class="text-h4 mb-4">월간 출역 현황</h1>
        </v-col>
      </v-row>
      
      <!-- PC: 달력 -->
      <v-card class="desktop-only">
        <v-card-title>출역 달력</v-card-title>
        <v-card-text>
          <div class="text-center pa-8">
            <p class="text-body-1">Full Calendar 영역</p>
            <p class="text-caption text-grey">프로토타입에서는 간단히 표시</p>
          </div>
        </v-card-text>
      </v-card>
      
      <!-- 모바일: 리스트 -->
      <v-card class="mobile-only">
        <v-card-title>출역 기록</v-card-title>
        <v-card-text>
          <v-list>
            <v-list-item
              v-for="log in userAttendanceLogs"
              :key="log.id"
            >
              <template v-slot:prepend>
                <v-icon :color="getStatusColor(log.status)">
                  {{ getStatusIcon(log.status) }}
                </v-icon>
              </template>
              
              <v-list-item-title>{{ formatDate(log.checkInTime) }}</v-list-item-title>
              <v-list-item-subtitle>
                {{ formatTime(log.checkInTime) }} | {{ getStatusText(log.status) }}
              </v-list-item-subtitle>
            </v-list-item>
          </v-list>
        </v-card-text>
      </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getAttendanceLogsByUser } from '@/mock-data/attendance'
import type { AttendanceLog } from '@/mock-data/attendance'

const userStore = useUserStore()

const userAttendanceLogs = computed(() => {
  if (!userStore.currentUser) return []
  return getAttendanceLogsByUser(userStore.currentUser.id)
})

function formatDate(timeString: string): string {
  const date = new Date(timeString)
  return date.toLocaleDateString('ko-KR')
}

function formatTime(timeString: string): string {
  const date = new Date(timeString)
  return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
}

function getStatusColor(status: AttendanceLog['status']): string {
  const colors: Record<string, string> = {
    NORMAL: 'success',
    LATE: 'warning',
    ABSENT: 'grey',
    MANUAL: 'info',
  }
  return colors[status] || 'grey'
}

function getStatusIcon(status: AttendanceLog['status']): string {
  const icons: Record<string, string> = {
    NORMAL: 'mdi-check-circle',
    LATE: 'mdi-clock-alert',
    ABSENT: 'mdi-close-circle',
    MANUAL: 'mdi-account-check',
  }
  return icons[status] || 'mdi-help-circle'
}

function getStatusText(status: AttendanceLog['status']): string {
  const texts: Record<string, string> = {
    NORMAL: '정상',
    LATE: '지각',
    ABSENT: '미출역',
    MANUAL: '수동승인',
  }
  return texts[status] || status
}
</script>

