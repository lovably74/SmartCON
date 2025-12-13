<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <h1 class="text-h4 mb-4">본사 관리자 대시보드</h1>
      </v-col>
    </v-row>
    
    <!-- 통계 카드 -->
    <v-row>
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-title>전체 현장 수</v-card-title>
          <v-card-text>
            <div class="text-h2">{{ mockSites.length }}</div>
            <div class="text-caption text-grey">개 현장</div>
          </v-card-text>
        </v-card>
      </v-col>
      
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-title>금일 총 출역 인원</v-card-title>
          <v-card-text>
            <div class="text-h2">{{ todayAttendanceCount }}</div>
            <div class="text-caption text-grey">명</div>
          </v-card-text>
        </v-card>
      </v-col>
      
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-title>전자계약 체결률</v-card-title>
          <v-card-text>
            <div class="text-h2">85%</div>
            <div class="text-caption text-grey">체결 완료</div>
          </v-card-text>
        </v-card>
      </v-col>
      
      <v-col cols="12" sm="6" md="3">
        <v-card>
          <v-card-title>안면 미등록자</v-card-title>
          <v-card-text>
            <div class="text-h2">{{ faceNotRegisteredCount }}</div>
            <div class="text-caption text-grey">명</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 현장별 출역 현황 -->
    <v-row class="mt-4">
      <v-col cols="12">
        <v-card>
          <v-card-title>
            현장별 출역 현황
            <v-spacer />
            <v-btn color="primary" variant="outlined" size="small">
              <v-icon start>mdi-download</v-icon>
              엑셀 다운로드
            </v-btn>
          </v-card-title>
          
          <v-card-text>
            <v-table>
              <thead>
                <tr>
                  <th>현장명</th>
                  <th>현장 코드</th>
                  <th>총 인원</th>
                  <th>금일 출역</th>
                  <th>출역률</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="site in mockSites"
                  :key="site.id"
                  @click="goToSiteDetail(site.id)"
                  style="cursor: pointer"
                >
                  <td>{{ site.name }}</td>
                  <td>{{ site.code }}</td>
                  <td>{{ site.totalWorkers }}명</td>
                  <td>{{ site.todayAttendance }}명</td>
                  <td>
                    <v-progress-linear
                      :model-value="(site.todayAttendance / site.totalWorkers) * 100"
                      color="primary"
                      height="20"
                    >
                      {{ Math.round((site.todayAttendance / site.totalWorkers) * 100) }}%
                    </v-progress-linear>
                  </td>
                  <td>
                    <v-btn size="small" variant="text">상세</v-btn>
                  </td>
                </tr>
              </tbody>
            </v-table>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- 안면 미등록자 Top 5 -->
    <v-row class="mt-4">
      <v-col cols="12" md="6">
        <v-card>
          <v-card-title>안면 미등록자 Top 5</v-card-title>
          <v-card-text>
            <v-list>
              <v-list-item
                v-for="(user, index) in faceNotRegisteredUsers"
                :key="user.id"
              >
                <v-list-item-title>
                  {{ index + 1 }}. {{ user.name }}
                </v-list-item-title>
                <v-list-item-subtitle>
                  {{ user.email }} | {{ user.phone }}
                </v-list-item-subtitle>
              </v-list-item>
            </v-list>
          </v-card-text>
        </v-card>
      </v-col>
      
      <!-- 월별 출역 통계 (간단한 표시) -->
      <v-col cols="12" md="6">
        <v-card>
          <v-card-title>월별 출역 통계</v-card-title>
          <v-card-text>
            <div class="text-center pa-8">
              <p class="text-body-1">그래프 영역</p>
              <p class="text-caption text-grey">프로토타입에서는 간단히 표시</p>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { mockSites } from '@/mock-data/sites'
import { mockUsers } from '@/mock-data/users'
import { getTodayAttendanceLogs } from '@/mock-data/attendance'

const router = useRouter()

const todayAttendanceCount = computed(() => {
  return getTodayAttendanceLogs().length
})

const faceNotRegisteredCount = computed(() => {
  return mockUsers.filter(u => !u.isFaceRegistered).length
})

const faceNotRegisteredUsers = computed(() => {
  return mockUsers.filter(u => !u.isFaceRegistered).slice(0, 5)
})

function goToSiteDetail(siteId: number) {
  // 프로토타입: 현장 상세 화면으로 이동 (구현 예정)
  console.log('현장 상세:', siteId)
}
</script>

