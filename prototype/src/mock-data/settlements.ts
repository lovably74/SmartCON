export interface Settlement {
  id: number
  siteId: number
  siteName: string
  period: string // '2025-12'
  totalAmount: number
  workerCount: number
  status: 'PENDING' | 'APPROVED' | 'PAID'
  approvedAt?: string
  paidAt?: string
}

export interface SettlementDetail {
  id: number
  settlementId: number
  workerId: number
  workerName: string
  workDays: number
  manDays: number
  unitPrice: number
  totalAmount: number
}

export const mockSettlements: Settlement[] = [
  {
    id: 1,
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    period: '2025-12',
    totalAmount: 15000000,
    workerCount: 25,
    status: 'PENDING',
  },
]

export const mockSettlementDetails: SettlementDetail[] = [
  {
    id: 1,
    settlementId: 1,
    workerId: 4,
    workerName: '박민수',
    workDays: 20,
    manDays: 20.0,
    unitPrice: 150000,
    totalAmount: 3000000,
  },
]





