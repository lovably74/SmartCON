<template>
  <v-container>
    <v-row>
        <v-col cols="12">
          <h1 class="text-h4 mb-4">출역 현황</h1>
        </v-col>
      </v-row>
      
      <!-- 필터 -->
      <v-row>
        <v-col cols="12" md="4">
          <v-select
            v-model="selectedSite"
            :items="siteOptions"
            label="현장 선택"
            clearable
          />
        </v-col>
        <v-col cols="12" md="4">
          <v-select
            v-model="selectedStatus"
            :items="statusOptions"
            label="상태 선택"
            clearable
          />
        </v-col>
        <v-col cols="12" md="4">
          <v-text-field
            v-model="searchQuery"
            label="이름 검색"
            prepend-inner-icon="mdi-magnify"
            clearable
          />
        </v-col>
      </v-row>
      
      <!-- PC: 테이블 -->
      <v-card class="desktop-only">
        <v-card-title>출역 목록</v-card-title>
        <v-card-text>
          <v-table>
            <thead>
              <tr>
                <th>이름</th>
                <th>현장</th>
                <th>팀</th>
                <th>출역 시간</th>
                <th>상태</th>
                <th>인증 방식</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="log in filteredLogs"
                :key="log.id"
              >
                <td>{{ log.userName }}</td>
                <td>{{ log.siteName }}</td>
                <td>{{ log.teamName || '-' }}</td>
                <td>{{ formatDateTime(log.checkInTime) }}</td>
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
                    v-if="log.status === 'MANUAL' && (userStore.isAdmin || userStore.isSiteAdmin)"
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
      </v-card>
      
      <!-- 모바일: 카드 -->
      <v-card class="mobile-only">
        <v-card-title>출역 목록</v-card-title>
        <v-card-text>
          <v-card
            v-for="log in filteredLogs"
            :key="log.id"
            class="mb-3"
          >
            <v-card-title>{{ log.userName }}</v-card-title>
            <v-card-subtitle>
              {{ log.siteName }} | {{ log.teamName || '-' }}
            </v-card-subtitle>
            <v-card-text>
              <div class="mb-2">
                <v-icon size="small" class="mr-1">mdi-clock</v-icon>
                {{ formatDateTime(log.checkInTime) }}
              </div>
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
            <v-card-actions v-if="log.status === 'MANUAL' && (userStore.isAdmin || userStore.isSiteAdmin)">
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
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { mockAttendanceLogs } from '@/mock-data/attendance'
import { mockSites } from '@/mock-data/sites'
import type { AttendanceLog } from '@/mock-data/attendance'

const router = useRouter()
const userStore = useUserStore()

const selectedSite = ref<number | null>(null)
const selectedStatus = ref<AttendanceLog['status'] | null>(null)
const searchQuery = ref('')

const siteOptions = computed(() => {
  return mockSites.map(site => ({
    title: site.name,
    value: site.id,
  }))
})

const statusOptions = [
  { title: '정상', value: 'NORMAL' },
  { title: '지각', value: 'LATE' },
  { title: '미출역', value: 'ABSENT' },
  { title: '수동승인', value: 'MANUAL' },
]

const filteredLogs = computed(() => {
  let logs = [...mockAttendanceLogs]
  
  // 현장 필터
  if (selectedSite.value) {
    logs = logs.filter(log => log.siteId === selectedSite.value)
  }
  
  // 상태 필터
  if (selectedStatus.value) {
    logs = logs.filter(log => log.status === selectedStatus.value)
  }
  
  // 검색 필터
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    logs = logs.filter(log => log.userName.toLowerCase().includes(query))
  }
  
  return logs
})

function formatDateTime(timeString: string): string {
  const date = new Date(timeString)
  return date.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
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

