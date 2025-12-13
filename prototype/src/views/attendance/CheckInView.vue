<template>
  <v-container>
    <v-row>
        <v-col cols="12" md="8" offset-md="2">
          <v-card>
            <v-card-title class="text-h5">
              출역 체크
            </v-card-title>
            
            <v-card-text>
              <div class="text-center pa-8">
                <v-icon size="80" color="primary" class="mb-4">
                  mdi-camera
                </v-icon>
                <p class="text-h6 mb-4">안면인식으로 출역을 체크합니다</p>
                <p class="text-caption text-grey mb-8">
                  카메라를 정면으로 향하고 얼굴이 프레임 안에 오도록 해주세요
                </p>
                
                <v-btn
                  color="primary"
                  size="x-large"
                  @click="handleCheckIn"
                  :loading="isProcessing"
                  :disabled="!userStore.currentUser?.isFaceRegistered"
                >
                  <v-icon start>mdi-camera</v-icon>
                  출역 체크하기
                </v-btn>
                
                <p v-if="!userStore.currentUser?.isFaceRegistered" class="text-caption text-warning mt-4">
                  안면 등록이 필요합니다.
                </p>
              </div>
            </v-card-text>
          </v-card>
          
          <!-- 결과 다이얼로그 -->
          <v-dialog v-model="showResult" max-width="500" persistent>
            <v-card>
              <v-card-title>
                <v-icon :color="checkInResult.success ? 'success' : 'error'" class="mr-2">
                  {{ checkInResult.success ? 'mdi-check-circle' : 'mdi-alert-circle' }}
                </v-icon>
                {{ checkInResult.success ? '출역 체크 완료' : '출역 체크 실패' }}
              </v-card-title>
              
              <v-card-text>
                <div v-if="checkInResult.success">
                  <p class="text-h6 mb-2">출역 시간: {{ checkInResult.checkInTime }}</p>
                  <p class="mb-2">상태: {{ checkInResult.status === 'NORMAL' ? '정상 출역' : '지각' }}</p>
                  <v-chip
                    :color="checkInResult.status === 'NORMAL' ? 'success' : 'warning'"
                    class="mt-2"
                  >
                    {{ checkInResult.status === 'NORMAL' ? '정상' : '지각' }}
                  </v-chip>
                </div>
                <div v-else>
                  <p class="text-h6 mb-2">안면인식에 실패했습니다.</p>
                  <p class="text-caption text-grey">
                    관리자 승인을 요청하시겠습니까?
                  </p>
                </div>
              </v-card-text>
              
              <v-card-actions>
                <v-spacer />
                <v-btn
                  v-if="!checkInResult.success"
                  variant="text"
                  @click="showResult = false"
                >
                  취소
                </v-btn>
                <v-btn
                  v-if="!checkInResult.success"
                  color="primary"
                  @click="requestManualApproval"
                >
                  승인 요청
                </v-btn>
                <v-btn
                  v-else
                  color="primary"
                  @click="handleClose"
                >
                  확인
                </v-btn>
              </v-card-actions>
            </v-card>
          </v-dialog>
        </v-col>
      </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isProcessing = ref(false)
const showResult = ref(false)
const checkInResult = ref<{
  success: boolean
  checkInTime?: string
  status?: 'NORMAL' | 'LATE'
}>({
  success: false,
})

function handleCheckIn() {
  // 프로토타입: 실제 API 호출 없이 시뮬레이션
  isProcessing.value = true
  
  setTimeout(() => {
    const now = new Date()
    const hour = now.getHours()
    const isLate = hour >= 9
    
    checkInResult.value = {
      success: Math.random() > 0.2, // 80% 성공률
      checkInTime: now.toLocaleTimeString('ko-KR'),
      status: isLate ? 'LATE' : 'NORMAL',
    }
    
    isProcessing.value = false
    showResult.value = true
  }, 2000) // 2초 시뮬레이션
}

function requestManualApproval() {
  showResult.value = false
  router.push('/app/attendance/manual-approve')
}

function handleClose() {
  showResult.value = false
  router.push('/app/dashboard')
}
</script>

