<template>
  <v-container>
    <v-row>
        <v-col cols="12">
          <div class="d-flex justify-space-between align-center mb-4">
            <h1 class="text-h4">이의 제기</h1>
            <v-btn color="primary" @click="goToCreate">
              <v-icon start>mdi-plus</v-icon>
              신청하기
            </v-btn>
          </div>
        </v-col>
      </v-row>
      
      <v-row>
        <v-col
          v-for="dispute in userDisputes"
          :key="dispute.id"
          cols="12"
        >
          <v-card>
            <v-card-title>
              {{ formatDate(dispute.disputeDate) }}
              <v-spacer />
              <v-chip :color="getStatusColor(dispute.status)">
                {{ getStatusText(dispute.status) }}
              </v-chip>
            </v-card-title>
            <v-card-text>
              <p>{{ dispute.reason }}</p>
              <p v-if="dispute.processComment" class="text-caption text-grey mt-2">
                처리 의견: {{ dispute.processComment }}
              </p>
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
import { mockDisputes } from '@/mock-data/disputes'
import type { AttendanceDispute } from '@/mock-data/disputes'

const router = useRouter()
const userStore = useUserStore()

const userDisputes = computed(() => {
  if (!userStore.currentUser) return []
  return mockDisputes.filter(d => d.userId === userStore.currentUser!.id)
})

function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

function getStatusColor(status: AttendanceDispute['status']): string {
  const colors: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'error',
  }
  return colors[status] || 'grey'
}

function getStatusText(status: AttendanceDispute['status']): string {
  const texts: Record<string, string> = {
    PENDING: '처리중',
    APPROVED: '승인',
    REJECTED: '반려',
  }
  return texts[status] || status
}

function goToCreate() {
  router.push('/app/mypage/dispute/create')
}
</script>

