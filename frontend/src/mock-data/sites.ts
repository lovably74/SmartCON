export interface Site {
  id: number
  name: string
  code: string
  address: string
  managerId: number
  managerName: string
  status: 'ACTIVE' | 'INACTIVE'
}

export const mockSites: Site[] = [
  {
    id: 1,
    name: '서울 강남구 건설 현장',
    code: 'SITE-001',
    address: '서울시 강남구 테헤란로 123',
    managerId: 2,
    managerName: '김철수',
    status: 'ACTIVE',
  },
  {
    id: 2,
    name: '부산 신항만 공사 현장',
    code: 'SITE-002',
    address: '부산시 강서구 성북동 456',
    managerId: 3,
    managerName: '이소장',
    status: 'ACTIVE',
  },
]



