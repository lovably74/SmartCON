<template>
  <v-container>
    <v-row>
        <v-col cols="12">
          <div class="d-flex justify-space-between align-center mb-4">
            <h1 class="text-h4">작업일보</h1>
            <v-btn
              v-if="userStore.isTeamLeader || userStore.isSiteAdmin || userStore.isAdmin"
              color="primary"
              @click="goToCreate"
            >
              <v-icon start>mdi-plus</v-icon>
              작성하기
            </v-btn>
          </div>
        </v-col>
      </v-row>
      
      <v-row>
        <v-col
          v-for="report in mockReports"
          :key="report.id"
          cols="12"
          md="6"
        >
          <v-card @click="goToDetail(report.id)" hover>
            <v-card-title>{{ report.workTypeName }}</v-card-title>
            <v-card-subtitle>
              {{ report.siteName }} | {{ formatDate(report.workDate) }}
            </v-card-subtitle>
            <v-card-text>
              <p>{{ report.workContent }}</p>
              <v-chip
                :color="report.isApproved ? 'success' : 'warning'"
                size="small"
                class="mt-2"
              >
                {{ report.isApproved ? '승인됨' : '대기중' }}
              </v-chip>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { mockReports } from '@/mock-data/reports'

const router = useRouter()
const userStore = useUserStore()

function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

function goToDetail(id: number) {
  router.push(`/app/reports/${id}`)
}

function goToCreate() {
  router.push('/app/reports/create')
}
</script>

