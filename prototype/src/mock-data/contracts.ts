export interface Contract {
  id: number
  workerId: number
  workerName: string
  siteId: number
  siteName: string
  contractDate: string
  startDate: string
  endDate?: string
  workType: string
  status: 'PENDING' | 'SIGNED' | 'EXPIRED'
  signedAt?: string
}

export const mockContracts: Contract[] = [
  {
    id: 1,
    workerId: 4,
    workerName: '박민수',
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    contractDate: '2025-12-01',
    startDate: '2025-12-01',
    status: 'SIGNED',
    signedAt: '2025-12-01T10:30:00',
    workType: '철근',
  },
  {
    id: 2,
    workerId: 5,
    workerName: '최철근',
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    contractDate: '2025-12-15',
    startDate: '2025-12-15',
    status: 'PENDING',
    workType: '콘크리트',
  },
]



