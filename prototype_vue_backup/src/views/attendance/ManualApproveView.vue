<template>
  <v-container>
    <v-row>
        <v-col cols="12" md="8" offset-md="2">
          <v-card>
            <v-card-title class="text-h5">예외 승인 처리</v-card-title>
            
            <v-card-text>
              <v-list>
                <v-list-item
                  v-for="log in pendingLogs"
                  :key="log.id"
                  class="mb-4"
                >
                  <v-card>
                    <v-card-title>{{ log.userName }}</v-card-title>
                    <v-card-subtitle>
                      {{ log.siteName }} | {{ formatDateTime(log.checkInTime) }}
                    </v-card-subtitle>
                    <v-card-text>
                      <p>출역 시도 시간: {{ formatDateTime(log.checkInTime) }}</p>
                      <p>인증 방식: {{ log.authType === 'FACE' ? '안면인식' : '수동' }}</p>
                      <p v-if="log.manualReason">사유: {{ log.manualReason }}</p>
                    </v-card-text>
                    <v-card-actions>
                      <v-select
                        v-model="approvalReasons[log.id]"
                        :items="reasonOptions"
                        label="승인 사유"
                        class="mr-4"
                      />
                      <v-spacer />
                      <v-btn color="error" @click="handleReject(log.id)">반려</v-btn>
                      <v-btn color="primary" @click="handleApprove(log.id)">승인</v-btn>
                    </v-card-actions>
                  </v-card>
                </v-list-item>
              </v-list>
              
              <div v-if="pendingLogs.length === 0" class="text-center pa-8">
                <p class="text-grey">대기 중인 예외 승인 요청이 없습니다.</p>
              </div>
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
import { mockAttendanceLogs } from '@/mock-data/attendance'

const router = useRouter()
const userStore = useUserStore()

const approvalReasons = ref<Record<number, string>>({})

const reasonOptions = [
  { title: '기기 오류', value: 'DEVICE_ERROR' },
  { title: '안면 손상/변형', value: 'FACE_DAMAGE' },
  { title: '조명 부족', value: 'LOW_LIGHT' },
  { title: '기타', value: 'OTHER' },
]

const pendingLogs = computed(() => {
  return mockAttendanceLogs.filter(log => log.status === 'MANUAL')
})

function formatDateTime(timeString: string): string {
  const date = new Date(timeString)
  return date.toLocaleString('ko-KR')
}

function handleApprove(logId: number) {
  // 프로토타입: 승인 처리 시뮬레이션
  alert(`출역 기록 ${logId} 승인 완료`)
  router.push('/app/dashboard')
}

function handleReject(logId: number) {
  // 프로토타입: 반려 처리 시뮬레이션
  alert(`출역 기록 ${logId} 반려 처리`)
  router.push('/app/dashboard')
}
</script>

