import { create } from 'zustand'
import type { User, UserRole } from '@/mock-data/users'

interface UserState {
  currentUser: User | null
  selectedRole: UserRole | null
  setCurrentUser: (user: User | null) => void
  selectRole: (role: UserRole) => void
  hasMultipleRoles: () => boolean
  logout: () => void
}

export const useUserStore = create<UserState>((set, get) => ({
  currentUser: null,
  selectedRole: null,
  setCurrentUser: (user) => {
    set({ currentUser: user, selectedRole: null })
    if (user) {
      localStorage.setItem('currentUser', JSON.stringify(user))
    } else {
      localStorage.removeItem('currentUser')
    }
  },
  selectRole: (role) => {
    set({ selectedRole: role })
    const { currentUser } = get()
    if (currentUser) {
      currentUser.currentRole = role
      localStorage.setItem('currentUser', JSON.stringify(currentUser))
    }
  },
  hasMultipleRoles: () => {
    const { currentUser } = get()
    return currentUser ? currentUser.roles.length > 1 : false
  },
  logout: () => {
    set({ currentUser: null, selectedRole: null })
    localStorage.removeItem('currentUser')
  },
}))

// 초기화: localStorage에서 사용자 정보 복원
if (typeof window !== 'undefined') {
  const stored = localStorage.getItem('currentUser')
  if (stored) {
    try {
      const user = JSON.parse(stored)
      useUserStore.getState().setCurrentUser(user)
      if (user.currentRole) {
        useUserStore.getState().selectRole(user.currentRole)
      }
    } catch (e) {
      console.error('Failed to restore user from localStorage', e)
    }
  }
}

