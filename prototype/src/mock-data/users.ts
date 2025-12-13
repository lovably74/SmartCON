export interface UserRole {
  roleCode: 'ADMIN' | 'SITE_ADMIN' | 'TEAM_LEADER' | 'WORKER'
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
  roles: UserRole[]
  currentRole?: UserRole
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
        roleCode: 'ADMIN',
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
        roleCode: 'SITE_ADMIN',
        roleName: '현장 관리자',
        siteId: 1,
        siteName: '서울 강남구 건설 현장',
      },
      {
        roleCode: 'TEAM_LEADER',
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
        roleCode: 'TEAM_LEADER',
        roleName: '팀장',
        siteId: 1,
        siteName: '서울 강남구 건설 현장',
        teamName: '콘크리트팀',
      },
      {
        roleCode: 'WORKER',
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
        roleCode: 'WORKER',
        roleName: '일반 노무자',
        siteId: 1,
        siteName: '서울 강남구 건설 현장',
        teamName: '철근팀',
      },
    ],
    isFaceRegistered: false,
  },
  {
    id: 5,
    name: '최지훈',
    email: 'worker2@smartcon.com',
    phone: '010-5678-9012',
    roles: [
      {
        roleCode: 'WORKER',
        roleName: '일반 노무자',
        siteId: 1,
        siteName: '서울 강남구 건설 현장',
        teamName: '콘크리트팀',
      },
    ],
    isFaceRegistered: true,
  },
]

