<template>
  <v-container>
    <v-row>
        <v-col cols="12" md="8" offset-md="2">
          <v-stepper v-model="step" :items="steps">
            <!-- Step 1: 안내 -->
            <template v-slot:item.1>
              <v-card>
                <v-card-title>안면 등록 가이드</v-card-title>
                <v-card-text>
                  <v-list>
                    <v-list-item>
                      <v-list-item-title>안경과 마스크를 제거해주세요</v-list-item-title>
                    </v-list-item>
                    <v-list-item>
                      <v-list-item-title>밝은 곳에서 촬영해주세요</v-list-item-title>
                    </v-list-item>
                    <v-list-item>
                      <v-list-item-title>얼굴이 프레임 안에 오도록 해주세요</v-list-item-title>
                    </v-list-item>
                  </v-list>
                </v-card-text>
                <v-card-actions>
                  <v-spacer />
                  <v-btn color="primary" @click="step = 2">다음</v-btn>
                </v-card-actions>
              </v-card>
            </template>
            
            <!-- Step 2: 권한 요청 -->
            <template v-slot:item.2>
              <v-card>
                <v-card-title>카메라 권한 요청</v-card-title>
                <v-card-text>
                  <p>안면 등록을 위해 카메라 접근 권한이 필요합니다.</p>
                </v-card-text>
                <v-card-actions>
                  <v-btn @click="step = 1">이전</v-btn>
                  <v-spacer />
                  <v-btn color="primary" @click="step = 3">권한 허용</v-btn>
                </v-card-actions>
              </v-card>
            </template>
            
            <!-- Step 3: 촬영 -->
            <template v-slot:item.3>
              <v-card>
                <v-card-title>안면 촬영</v-card-title>
                <v-card-text>
                  <div class="text-center pa-8">
                    <v-icon size="80" color="primary">mdi-camera</v-icon>
                    <p class="mt-4">카메라를 정면으로 향하고 얼굴이 프레임 안에 오도록 해주세요</p>
                    <div v-if="countdown > 0" class="text-h1 mt-4">{{ countdown }}</div>
                  </div>
                </v-card-text>
                <v-card-actions>
                  <v-btn @click="step = 2">이전</v-btn>
                  <v-spacer />
                  <v-btn color="primary" @click="handleCapture">촬영</v-btn>
                </v-card-actions>
              </v-card>
            </template>
            
            <!-- Step 4: 전송/분석 -->
            <template v-slot:item.4>
              <v-card>
                <v-card-title>전송 및 분석 중</v-card-title>
                <v-card-text>
                  <div class="text-center pa-8">
                    <v-progress-circular
                      indeterminate
                      color="primary"
                      size="64"
                      class="mb-4"
                    />
                    <p>안면 데이터를 분석하고 있습니다...</p>
                  </div>
                </v-card-text>
              </v-card>
            </template>
            
            <!-- Step 5: 결과 -->
            <template v-slot:item.5>
              <v-card>
                <v-card-title>
                  <v-icon :color="registrationResult.success ? 'success' : 'error'" class="mr-2">
                    {{ registrationResult.success ? 'mdi-check-circle' : 'mdi-alert-circle' }}
                  </v-icon>
                  {{ registrationResult.success ? '안면 등록 완료' : '안면 등록 실패' }}
                </v-card-title>
                <v-card-text>
                  <p v-if="registrationResult.success">
                    안면 등록이 완료되었습니다. 이제 출역 체크를 사용할 수 있습니다.
                  </p>
                  <p v-else>
                    안면 등록에 실패했습니다. 다시 시도해주세요.
                  </p>
                </v-card-text>
                <v-card-actions>
                  <v-spacer />
                  <v-btn
                    v-if="!registrationResult.success"
                    @click="step = 3"
                  >
                    다시 시도
                  </v-btn>
                  <v-btn
                    color="primary"
                    @click="handleComplete"
                  >
                    {{ registrationResult.success ? '완료' : '취소' }}
                  </v-btn>
                </v-card-actions>
              </v-card>
            </template>
          </v-stepper>
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

const step = ref(1)
const countdown = ref(0)
const registrationResult = ref<{ success: boolean }>({ success: false })

const steps = [
  '안내',
  '권한 요청',
  '촬영',
  '전송/분석',
  '결과',
]

function handleCapture() {
  // 프로토타입: 카운트다운 시뮬레이션
  countdown.value = 3
  const interval = setInterval(() => {
    countdown.value--
    if (countdown.value === 0) {
      clearInterval(interval)
      step.value = 4
      
      // 전송/분석 시뮬레이션
      setTimeout(() => {
        registrationResult.value = { success: Math.random() > 0.2 } // 80% 성공률
        step.value = 5
      }, 2000)
    }
  }, 1000)
}

function handleComplete() {
  if (registrationResult.value.success && userStore.currentUser) {
    userStore.currentUser.isFaceRegistered = true
  }
  router.push('/app/dashboard')
}
</script>

