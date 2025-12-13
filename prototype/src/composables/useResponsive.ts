import { computed } from 'vue'
import { useDisplay } from 'vuetify'

export function useResponsive() {
  const { mdAndUp, lgAndUp } = useDisplay()
  
  const isMobile = computed(() => !mdAndUp.value)
  const isTablet = computed(() => mdAndUp.value && !lgAndUp.value)
  const isDesktop = computed(() => lgAndUp.value)
  
  return {
    isMobile,
    isTablet,
    isDesktop,
  }
}

