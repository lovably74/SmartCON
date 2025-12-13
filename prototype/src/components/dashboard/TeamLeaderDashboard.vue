<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <h1 class="text-h4 mb-4">
          팀장 대시보드
          <span v-if="userStore.selectedRole?.teamName" class="text-h6 text-grey">
            - {{ userStore.selectedRole.teamName }}
          </span>
        </h1>
      </v-col>
    </v-row>
    
    <!-- 통계 카드 -->
    <v-row>
      <v-col cols="12" sm="6" md="4">
        <v-card>
          <v-card-title>소속 팀원 수</v-card-title>
          <v-card-text>
            <div class="text-h2">{{ teamMemberCount }}</div>
            <div class="text-caption text-grey">명</div>
          </v-card-text>
        </v-card>
      </v-col>
      
      <v-col cols="12" sm="6" md="4">
        <v-card>
          <v-card-title>금일 출역 인원</v-card-title>
          <v-card-text>
            <div class="text-h2">{{ todayAttendanceCount }}</div>
            <div class="text-caption text-grey">명</div>
          </v-card-text>
        </v-card>
      </v-col>
      
      <v-col cols="12" sm="6" md="4">
        <v-card>
          <v-card-title>미등록 안면</v-card-title>
          <v-card-text>
            <div class="text-h2 text-orange">{{ faceNotRegisteredCount }}</div>
            <div class="text-caption text-grey">명</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 팀원 출역 현황 -->
    <v-row class="mt-4">
      <v-col cols="12">
        <v-card>
          <v-card-title>팀원 출역 현황</v-card-title>
          
          <!-- PC: 테이블 -->
          <v-card-text class="desktop-only">
            <v-table>
              <thead>
                <tr>
                  <th>이름</th>
                  <th>출역 시간</th>
                  <th>상태</th>
                  <th>인증 방식</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="log in teamAttendanceLogs"
                  :key="log.id"
                >
                  <td>{{ log.userName }}</td>
                  <td>{{ formatTime(log.checkInTime) }}</td>
                  <td>
                    <v-chip
                      :color="getStatusColor(log.status)"
                      size="small"
                    >
                      {{ getStatusText(log.status) }}
                    </v-chip>
                  </td>
                  <td>
                    <v-chip
                      :color="log.authType === 'FACE' ? 'success' : 'info'"
                      size="small"
                    >
                      {{ log.authType === 'FACE' ? '안면인식' : '수동승인' }}
                    </v-chip>
                  </td>
                </tr>
              </tbody>
            </v-table>
          </v-card-text>
          
          <!-- 모바일: 카드 -->
          <v-card-text class="mobile-only">
            <v-card
              v-for="log in teamAttendanceLogs"
              :key="log.id"
              class="mb-3"
            >
              <v-card-title>{{ log.userName }}</v-card-title>
              <v-card-subtitle>{{ formatTime(log.checkInTime) }}</v-card-subtitle>
              <v-card-text>
                <v-chip
                  :color="getStatusColor(log.status)"
                  size="small"
                  class="mr-2"
                >
                  {{ getStatusText(log.status) }}
                </v-chip>
                <v-chip
                  :color="log.authType === 'FACE' ? 'success' : 'info'"
                  size="small"
                >
                  {{ log.authType === 'FACE' ? '안면인식' : '수동승인' }}
                </v-chip>
              </v-card-text>
            </v-card>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getTodayAttendanceLogs } from '@/mock-data/attendance'
import { mockUsers } from '@/mock-data/users'
import type { AttendanceLog } from '@/mock-data/attendance'

const userStore = useUserStore()

const teamName = computed(() => userStore.selectedRole?.teamName || '')

const teamAttendanceLogs = computed(() => {
  const todayLogs = getTodayAttendanceLogs()
  if (!teamName.value) return []
  return todayLogs.filter(log => log.teamName === teamName.value)
})

const teamMemberCount = computed(() => {
  if (!teamName.value) return 0
  return mockUsers.filter(u => {
    return u.roles.some(r => r.teamName === teamName.value)
  }).length
})

const todayAttendanceCount = computed(() => {
  return teamAttendanceLogs.value.length
})

const faceNotRegisteredCount = computed(() => {
  if (!teamName.value) return 0
  return mockUsers.filter(u => {
    const hasTeamRole = u.roles.some(r => r.teamName === teamName.value)
    return hasTeamRole && !u.isFaceRegistered
  }).length
})

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

