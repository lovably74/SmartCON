import { useState, useEffect } from 'react'

export function useMediaQuery(query: string): boolean {
  const [matches, setMatches] = useState(() => {
    if (typeof window === 'undefined') return false
    return window.matchMedia(query).matches
  })

  useEffect(() => {
    if (typeof window === 'undefined') return

    const media = window.matchMedia(query)
    
    // 초기 값 설정
    setMatches(media.matches)
    
    // 이벤트 리스너
    const listener = (event: MediaQueryListEvent) => {
      setMatches(event.matches)
    }
    
    // addEventListener 사용 (addListener는 deprecated)
    if (media.addEventListener) {
      media.addEventListener('change', listener)
      return () => media.removeEventListener('change', listener)
    } else {
      // 구형 브라우저 지원
      media.addListener(listener)
      return () => media.removeListener(listener)
    }
  }, [query])

  return matches
}
