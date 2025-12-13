<template>
  <v-container>
    <v-row>
        <v-col cols="12" md="8" offset-md="2">
          <v-card>
            <v-card-title>작업일보 상세</v-card-title>
            
            <v-card-text>
              <v-list>
                <v-list-item>
                  <v-list-item-title>작업일자</v-list-item-title>
                  <v-list-item-subtitle>{{ formatDate(report.workDate) }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>공종</v-list-item-title>
                  <v-list-item-subtitle>{{ report.workTypeName }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>작업내용</v-list-item-title>
                  <v-list-item-subtitle>{{ report.workContent }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>승인 상태</v-list-item-title>
                  <v-list-item-subtitle>
                    <v-chip :color="report.isApproved ? 'success' : 'warning'">
                      {{ report.isApproved ? '승인됨' : '대기중' }}
                    </v-chip>
                  </v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-card-text>
            
            <v-card-actions>
              <v-btn @click="router.push('/app/reports')">목록</v-btn>
              <v-spacer />
              <v-btn
                v-if="userStore.isSiteAdmin || userStore.isAdmin"
                color="primary"
                @click="handleApprove"
              >
                승인
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { mockReports } from '@/mock-data/reports'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const report = computed(() => {
  const id = parseInt(route.params.id as string)
  return mockReports.find(r => r.id === id) || mockReports[0]
})

function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

function handleApprove() {
  alert('작업일보 승인 완료')
  router.push('/app/reports')
}
</script>

