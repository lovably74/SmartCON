export interface Site {
  id: number
  name: string
  code: string
  address: string
  managerId: number
  managerName: string
  totalWorkers: number
  todayAttendance: number
}

export const mockSites: Site[] = [
  {
    id: 1,
    name: '서울 강남구 건설 현장',
    code: 'SITE-001',
    address: '서울시 강남구 테헤란로 123',
    managerId: 2,
    managerName: '김철수',
    totalWorkers: 50,
    todayAttendance: 45,
  },
  {
    id: 2,
    name: '서울 송파구 건설 현장',
    code: 'SITE-002',
    address: '서울시 송파구 올림픽로 456',
    managerId: 6,
    managerName: '정수진',
    totalWorkers: 30,
    todayAttendance: 28,
  },
  {
    id: 3,
    name: '경기 성남시 건설 현장',
    code: 'SITE-003',
    address: '경기도 성남시 분당구 정자동 789',
    managerId: 7,
    managerName: '한지우',
    totalWorkers: 40,
    todayAttendance: 38,
  },
]

