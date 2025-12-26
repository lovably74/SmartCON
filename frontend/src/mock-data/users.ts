export interface UserRole {
  roleCode: 'ROLE_SUPER' | 'ROLE_HQ' | 'ROLE_SITE' | 'ROLE_TEAM' | 'ROLE_WORKER'
  roleName: string
  siteId?: number
  siteName?: string
  teamName?: string
}

export interface User {
  id: number
  name: string
  email: string
  phone: string
  roles: UserRole[] // 다중 역할 지원
  currentRole?: UserRole // 현재 선택된 역할
  isFaceRegistered: boolean
}

export const mockUsers: User[] = [
  {
    id: 1,
    name: '홍길동',
    email: 'admin@smartcon.com',
    phone: '010-1234-5678',
    roles: [
      {
        roleCode: 'ROLE_HQ',
        roleName: '본사 관리자',
      },
    ],
    isFaceRegistered: true,
  },
  {
    id: 2,
    name: '김철수',
    email: 'site@smartcon.com',
    phone: '010-2345-6789',
    roles: [
      {
        roleCode: 'ROLE_SITE',
        roleName: '현장 관리자',
        siteId: 1,
        siteName: '서울 강남구 건설 현장',
      },
      {
        roleCode: 'ROLE_TEAM',
        roleName: '팀장',
        siteId: 1,
        siteName: '서울 강남구 건설 현장',
        teamName: '철근팀',
      },
    ],
    isFaceRegistered: true,
  },
  {
    id: 3,
    name: '이영희',
    email: 'team@smartcon.com',
    phone: '010-3456-7890',
    roles: [
      {
        roleCode: 'ROLE_TEAM',
        roleName: '팀장',
        siteId: 1,
        siteName: '서울 강남구 건설 현장',
        teamName: '콘크리트팀',
      },
      {
        roleCode: 'ROLE_WORKER',
        roleName: '일반 노무자',
        siteId: 1,
        siteName: '서울 강남구 건설 현장',
        teamName: '콘크리트팀',
      },
    ],
    isFaceRegistered: true,
  },
  {
    id: 4,
    name: '박민수',
    email: 'worker@smartcon.com',
    phone: '010-4567-8901',
    roles: [
      {
        roleCode: 'ROLE_WORKER',
        roleName: '일반 노무자',
        siteId: 1,
        siteName: '서울 강남구 건설 현장',
        teamName: '철근팀',
      },
    ],
    isFaceRegistered: false,
  },
]



