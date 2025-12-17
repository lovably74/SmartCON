<template>
  <PcLayout v-if="isDesktop" />
  <MobileLayout v-else />
</template>

<script setup lang="ts">
import { useResponsive } from '@/composables/useResponsive'
import PcLayout from '@/components/layout/PcLayout.vue'
import MobileLayout from '@/components/layout/MobileLayout.vue'

const { isDesktop } = useResponsive()
</script>

