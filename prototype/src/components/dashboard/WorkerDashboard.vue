<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <h1 class="text-h4 mb-4">노무자 대시보드</h1>
      </v-col>
    </v-row>
    
    <!-- 안면 등록 상태 -->
    <v-row>
      <v-col cols="12">
        <v-card
          :color="userStore.currentUser?.isFaceRegistered ? 'success' : 'warning'"
          variant="tonal"
        >
          <v-card-title>
            <v-icon class="mr-2">
              {{ userStore.currentUser?.isFaceRegistered ? 'mdi-check-circle' : 'mdi-alert-circle' }}
            </v-icon>
            안면 등록 상태
          </v-card-title>
          <v-card-text>
            <p v-if="userStore.currentUser?.isFaceRegistered" class="text-h6">
              안면 등록이 완료되었습니다.
            </p>
            <div v-else>
              <p class="text-h6 mb-4">안면 등록이 필요합니다.</p>
              <v-btn
                color="primary"
                @click="goToFaceRegister"
              >
                안면 등록하기
              </v-btn>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 출역 체크 -->
    <v-row class="mt-4">
      <v-col cols="12">
        <v-card>
          <v-card-title>출역 체크</v-card-title>
          <v-card-text>
            <v-btn
              color="primary"
              size="large"
              block
              @click="goToCheckIn"
              :disabled="!userStore.currentUser?.isFaceRegistered"
            >
              <v-icon start>mdi-account-check</v-icon>
              출역 체크하기
            </v-btn>
            <p v-if="!userStore.currentUser?.isFaceRegistered" class="text-caption text-grey mt-2">
              안면 등록 후 출역 체크가 가능합니다.
            </p>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 최근 출역 기록 -->
    <v-row class="mt-4">
      <v-col cols="12">
        <v-card>
          <v-card-title>
            최근 출역 기록
            <v-spacer />
            <v-btn
              variant="text"
              size="small"
              @click="goToMyAttendance"
            >
              전체 보기
            </v-btn>
          </v-card-title>
          
          <v-card-text>
            <v-list v-if="recentAttendanceLogs.length > 0">
              <v-list-item
                v-for="log in recentAttendanceLogs"
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
            <div v-else class="text-center pa-4">
              <p class="text-grey">출역 기록이 없습니다.</p>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getAttendanceLogsByUser } from '@/mock-data/attendance'
import type { AttendanceLog } from '@/mock-data/attendance'

const router = useRouter()
const userStore = useUserStore()

const recentAttendanceLogs = computed(() => {
  if (!userStore.currentUser) return []
  const logs = getAttendanceLogsByUser(userStore.currentUser.id)
  return logs.slice(0, 5) // 최근 5개만
})

function formatDate(timeString: string): string {
  const date = new Date(timeString)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
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

function goToFaceRegister() {
  router.push('/app/face/register')
}

function goToCheckIn() {
  router.push('/app/attendance/check-in')
}

function goToMyAttendance() {
  router.push('/app/mypage/attendance')
}
</script>

