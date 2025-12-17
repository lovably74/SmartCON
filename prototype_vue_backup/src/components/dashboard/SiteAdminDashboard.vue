<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <h1 class="text-h4 mb-4">
          현장 관리자 대시보드
          <span v-if="userStore.selectedRole?.siteName" class="text-h6 text-grey">
            - {{ userStore.selectedRole.siteName }}
          </span>
        </h1>
      </v-col>
    </v-row>
    
    <!-- 통계 카드 -->
    <v-row>
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-title>금일 출역 인원</v-card-title>
          <v-card-text>
            <div class="text-h2">{{ todayAttendanceCount }}</div>
            <div class="text-caption text-grey">명</div>
          </v-card-text>
        </v-card>
      </v-col>
      
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-title>지각 인원</v-card-title>
          <v-card-text>
            <div class="text-h2 text-orange">{{ lateCount }}</div>
            <div class="text-caption text-grey">명</div>
          </v-card-text>
        </v-card>
      </v-col>
      
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-title>예외 승인 대기</v-card-title>
          <v-card-text>
            <div class="text-h2 text-blue">{{ pendingManualApprovalCount }}</div>
            <div class="text-caption text-grey">건</div>
          </v-card-text>
        </v-card>
      </v-col>
      
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-title>출역률</v-card-title>
          <v-card-text>
            <div class="text-h2">{{ attendanceRate }}%</div>
            <div class="text-caption text-grey">정상 출역</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 출역 현황 테이블 (PC) / 카드 (모바일) -->
    <v-row class="mt-4">
      <v-col cols="12">
        <v-card>
          <v-card-title>
            출역 현황
            <v-spacer />
            <v-btn color="primary" variant="outlined" size="small">
              <v-icon start>mdi-download</v-icon>
              엑셀 다운로드
            </v-btn>
          </v-card-title>
          
          <!-- PC: 테이블 -->
          <v-card-text class="desktop-only">
            <v-table>
              <thead>
                <tr>
                  <th>이름</th>
                  <th>팀</th>
                  <th>출역 시간</th>
                  <th>상태</th>
                  <th>인증 방식</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="log in siteAttendanceLogs"
                  :key="log.id"
                >
                  <td>{{ log.userName }}</td>
                  <td>{{ log.teamName || '-' }}</td>
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
                  <td>
                    <v-btn
                      v-if="log.status === 'MANUAL'"
                      size="small"
                      color="primary"
                      @click="goToManualApprove(log.id)"
                    >
                      승인
                    </v-btn>
                  </td>
                </tr>
              </tbody>
            </v-table>
          </v-card-text>
          
          <!-- 모바일: 카드 -->
          <v-card-text class="mobile-only">
            <v-card
              v-for="log in siteAttendanceLogs"
              :key="log.id"
              class="mb-3"
            >
              <v-card-title>{{ log.userName }}</v-card-title>
              <v-card-subtitle>
                {{ log.teamName || '-' }} | {{ formatTime(log.checkInTime) }}
              </v-card-subtitle>
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
              <v-card-actions v-if="log.status === 'MANUAL'">
                <v-spacer />
                <v-btn
                  color="primary"
                  @click="goToManualApprove(log.id)"
                >
                  승인 처리
                </v-btn>
              </v-card-actions>
            </v-card>
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
import { getTodayAttendanceLogs, getAttendanceLogsBySite } from '@/mock-data/attendance'
import type { AttendanceLog } from '@/mock-data/attendance'

const router = useRouter()
const userStore = useUserStore()

const siteId = computed(() => userStore.selectedRole?.siteId || 1)

const siteAttendanceLogs = computed(() => {
  return getAttendanceLogsBySite(siteId.value)
})

const todayAttendanceCount = computed(() => {
  return siteAttendanceLogs.value.length
})

const lateCount = computed(() => {
  return siteAttendanceLogs.value.filter(log => log.status === 'LATE').length
})

const pendingManualApprovalCount = computed(() => {
  return siteAttendanceLogs.value.filter(log => log.status === 'MANUAL').length
})

const attendanceRate = computed(() => {
  if (siteAttendanceLogs.value.length === 0) return 0
  const normalCount = siteAttendanceLogs.value.filter(log => log.status === 'NORMAL').length
  return Math.round((normalCount / siteAttendanceLogs.value.length) * 100)
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

function goToManualApprove(logId: number) {
  router.push({ path: '/app/attendance/manual-approve', query: { logId: logId.toString() } })
}
</script>

