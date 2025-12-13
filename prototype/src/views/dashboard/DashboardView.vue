<template>
  <div>
    <!-- 본사 관리자 대시보드 -->
    <AdminDashboard v-if="userStore.isAdmin" />
    
    <!-- 현장 관리자 대시보드 -->
    <SiteAdminDashboard v-else-if="userStore.isSiteAdmin" />
    
    <!-- 팀장 대시보드 -->
    <TeamLeaderDashboard v-else-if="userStore.isTeamLeader" />
    
    <!-- 노무자 대시보드 -->
    <WorkerDashboard v-else-if="userStore.isWorker" />
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import AdminDashboard from '@/components/dashboard/AdminDashboard.vue'
import SiteAdminDashboard from '@/components/dashboard/SiteAdminDashboard.vue'
import TeamLeaderDashboard from '@/components/dashboard/TeamLeaderDashboard.vue'
import WorkerDashboard from '@/components/dashboard/WorkerDashboard.vue'

const userStore = useUserStore()
</script>

