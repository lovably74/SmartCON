<template>
  <v-container>
    <v-row>
        <v-col cols="12" md="8" offset-md="2">
          <v-card>
            <v-card-title>이의 제기 신청</v-card-title>
            
            <v-card-text>
              <v-form>
                <v-text-field
                  v-model="form.disputeDate"
                  label="이의 제기 날짜"
                  type="date"
                  required
                />
                
                <v-textarea
                  v-model="form.reason"
                  label="이의 제기 사유"
                  rows="5"
                  required
                />
              </v-form>
            </v-card-text>
            
            <v-card-actions>
              <v-btn @click="router.push('/app/mypage/dispute')">취소</v-btn>
              <v-spacer />
              <v-btn color="primary" @click="handleSubmit">제출</v-btn>
            </v-card-actions>
          </v-card>
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

const form = ref({
  disputeDate: new Date().toISOString().split('T')[0],
  reason: '',
})

function handleSubmit() {
  // 프로토타입: 제출 시뮬레이션
  alert('이의 제기 신청 완료')
  router.push('/mypage/dispute')
}
</script>

