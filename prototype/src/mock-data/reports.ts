export interface DailyReport {
  id: number
  siteId: number
  siteName: string
  teamLeaderId: number
  teamLeaderName: string
  workDate: string
  workTypeCode: string
  workTypeName: string
  workContent: string
  photoUrls: string[]
  isApproved: boolean
  approvedAt?: string
}

export const mockReports: DailyReport[] = [
  {
    id: 1,
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    teamLeaderId: 2,
    teamLeaderName: '김철수',
    workDate: '2025-12-17',
    workTypeCode: 'CONCRETE',
    workTypeName: '콘크리트',
    workContent: '1층 콘크리트 타설 작업 완료',
    photoUrls: ['/images/report1.jpg'],
    isApproved: false,
  },
  {
    id: 2,
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    teamLeaderId: 3,
    teamLeaderName: '이영희',
    workDate: '2025-12-17',
    workTypeCode: 'REBAR',
    workTypeName: '철근',
    workContent: '2층 철근 조립 작업 진행 중',
    photoUrls: ['/images/report2.jpg'],
    isApproved: true,
    approvedAt: '2025-12-17T14:30:00',
  },
]





