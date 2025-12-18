import { useEffect, useRef } from 'react'
import { createPortal } from 'react-dom'
import { X } from 'lucide-react'
import { cn } from '@/lib/utils'

export interface DialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  children: React.ReactNode
}

export function Dialog({ open, onOpenChange, children }: DialogProps) {
  const dialogRef = useRef<HTMLDivElement>(null)

  // 디버깅: Dialog open prop 추적
  useEffect(() => {
    console.log('[Dialog] open prop 변경:', open)
  }, [open])

  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && open) {
        console.log('[Dialog] ESC 키 누름, 모달 닫기')
        onOpenChange(false)
      }
    }

    if (open) {
      console.log('[Dialog] 모달 열림 - 이벤트 리스너 및 스크롤 제어 설정')
      document.addEventListener('keydown', handleEscape)
      // 스크롤 방지 (모달이 열렸을 때)
      const originalOverflow = document.body.style.overflow
      console.log('[Dialog] body overflow 원래 값:', originalOverflow, '-> hidden으로 변경')
      document.body.style.overflow = 'hidden'
      
      return () => {
        console.log('[Dialog] 모달 닫힘 - 이벤트 리스너 제거 및 스크롤 복원')
        document.removeEventListener('keydown', handleEscape)
        document.body.style.overflow = originalOverflow
      }
    }
  }, [open, onOpenChange])

  // Portal 생성 후 DOM 확인 (조건부 return 이전에 hooks 호출)
  useEffect(() => {
    if (open) {
      const timer = setTimeout(() => {
        const portalElements = document.querySelectorAll('[role="dialog"]')
        console.log('[Dialog] DOM에서 dialog 요소 찾기:', portalElements.length, '개')
        portalElements.forEach((el, idx) => {
          console.log(`[Dialog] Dialog 요소 #${idx}:`, el)
          console.log(`[Dialog] Dialog 요소 #${idx} computed style:`, window.getComputedStyle(el as HTMLElement))
          console.log(`[Dialog] Dialog 요소 #${idx} 부모 요소:`, el.parentElement)
          console.log(`[Dialog] Dialog 요소 #${idx} 부모의 부모:`, el.parentElement?.parentElement)
        })
      }, 100)
      return () => clearTimeout(timer)
    }
  }, [open])

  console.log('[Dialog] 렌더링 체크, open:', open)

  if (!open) {
    console.log('[Dialog] open이 false이므로 null 반환')
    return null
  }

  console.log('[Dialog] Portal 생성 시작, document.body:', document.body)
  
  // Portal을 사용하여 body에 직접 렌더링
  const portalContent = (
    <div 
      className="fixed inset-0 flex items-center justify-center p-4"
      style={{ 
        zIndex: 99999,
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0
      }}
      ref={(el) => {
        if (el) {
          console.log('[Dialog] Portal 컨테이너 DOM 요소 생성됨:', el)
          console.log('[Dialog] Portal 컨테이너 computed style:', window.getComputedStyle(el))
          console.log('[Dialog] Portal 컨테이너 z-index:', window.getComputedStyle(el).zIndex)
        }
      }}
    >
      {/* Overlay - 비활성화 배경 */}
      <div
        className="fixed inset-0"
        style={{
          backgroundColor: 'rgba(75, 85, 99, 0.92)', // gray-600 with 92% opacity - 더 진한 비활성화 느낌
          backdropFilter: 'blur(12px)',
          zIndex: 99999,
          pointerEvents: 'auto' // 클릭 이벤트 차단
        }}
        onClick={() => {
          console.log('[Dialog] Overlay 클릭됨, 모달 닫기')
          onOpenChange(false)
        }}
        aria-hidden="true"
        ref={(el) => {
          if (el) {
            console.log('[Dialog] Overlay DOM 요소:', el)
            console.log('[Dialog] Overlay computed style:', window.getComputedStyle(el))
          }
        }}
      />
      
      {/* Dialog Content */}
      <div
        ref={(el) => {
          dialogRef.current = el
          if (el) {
            console.log('[Dialog] Dialog Content DOM 요소:', el)
            console.log('[Dialog] Dialog Content computed style:', window.getComputedStyle(el))
            console.log('[Dialog] Dialog Content z-index:', window.getComputedStyle(el).zIndex)
            console.log('[Dialog] Dialog Content position:', window.getComputedStyle(el).position)
            console.log('[Dialog] Dialog Content display:', window.getComputedStyle(el).display)
            console.log('[Dialog] Dialog Content visibility:', window.getComputedStyle(el).visibility)
            console.log('[Dialog] Dialog Content offsetParent:', el.offsetParent)
            console.log('[Dialog] Dialog Content offsetTop:', el.offsetTop, 'offsetLeft:', el.offsetLeft)
            console.log('[Dialog] Dialog Content clientWidth:', el.clientWidth, 'clientHeight:', el.clientHeight)
          }
        }}
        className="relative"
        style={{
          zIndex: 100000,
          position: 'relative'
        }}
        role="dialog"
        aria-modal="true"
      >
        {children}
      </div>
    </div>
  )

  console.log('[Dialog] createPortal 호출, portalContent:', portalContent, 'target:', document.body)

  const result = createPortal(portalContent, document.body)
  console.log('[Dialog] createPortal 결과:', result)
  return result
}

export interface DialogContentProps {
  className?: string
  children: React.ReactNode
}

export function DialogContent({ className, children }: DialogContentProps) {
  return (
    <div
      className={cn(
        'bg-white rounded-lg shadow-2xl w-full max-w-lg mx-4',
        'max-h-[90vh] overflow-y-auto',
        className
      )}
      onClick={(e) => e.stopPropagation()}
    >
      {children}
    </div>
  )
}

export interface DialogHeaderProps {
  className?: string
  children: React.ReactNode
}

export function DialogHeader({ className, children }: DialogHeaderProps) {
  return (
    <div className={cn('mb-4', className)}>
      {children}
    </div>
  )
}

export interface DialogTitleProps {
  className?: string
  children: React.ReactNode
}

export function DialogTitle({ className, children }: DialogTitleProps) {
  return (
    <h2 className={cn('text-xl font-semibold text-gray-900', className)}>
      {children}
    </h2>
  )
}

export interface DialogDescriptionProps {
  className?: string
  children: React.ReactNode
}

export function DialogDescription({ className, children }: DialogDescriptionProps) {
  return (
    <p className={cn('text-sm text-gray-500 mt-2', className)}>
      {children}
    </p>
  )
}

export interface DialogFooterProps {
  className?: string
  children: React.ReactNode
}

export function DialogFooter({ className, children }: DialogFooterProps) {
  return (
    <div className={cn('flex items-center justify-end gap-2 mt-6', className)}>
      {children}
    </div>
  )
}

export interface DialogCloseProps {
  onClick: () => void
  className?: string
}

export function DialogClose({ onClick, className }: DialogCloseProps) {
  return (
    <button
      onClick={onClick}
      className={cn(
        'absolute top-4 right-4 p-1 rounded-md hover:bg-gray-100 transition-colors',
        className
      )}
      aria-label="닫기"
    >
      <X size={20} className="text-gray-500" />
    </button>
  )
}

