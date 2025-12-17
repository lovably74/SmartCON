export interface DailyReport {
  id: number
  siteId: number
  siteName: string
  teamLeaderId: number
  teamLeaderName: string
  teamName: string
  workDate: string
  workTypeCode: string
  workTypeName: string
  workContent: string
  photoUrls: string[]
  isApproved: boolean
  approvedBy?: number
  approvedByName?: string
  approvedAt?: string
}

export const mockReports: DailyReport[] = [
  {
    id: 1,
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    teamLeaderId: 3,
    teamLeaderName: '이영희',
    teamName: '콘크리트팀',
    workDate: '2025-12-13',
    workTypeCode: 'CONCRETE',
    workTypeName: '콘크리트 타설',
    workContent: '1층 콘크리트 타설 작업 완료. 양생 상태 양호.',
    photoUrls: ['/images/report1.jpg', '/images/report2.jpg'],
    isApproved: false,
  },
  {
    id: 2,
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    teamLeaderId: 2,
    teamLeaderName: '김철수',
    teamName: '철근팀',
    workDate: '2025-12-13',
    workTypeCode: 'REBAR',
    workTypeName: '철근 배근',
    workContent: '1층 기둥 철근 배근 완료. 검사 대기 중.',
    photoUrls: ['/images/report3.jpg'],
    isApproved: true,
    approvedBy: 2,
    approvedByName: '김철수',
    approvedAt: '2025-12-13T14:30:00',
  },
  {
    id: 3,
    siteId: 2,
    siteName: '서울 송파구 건설 현장',
    teamLeaderId: 11,
    teamLeaderName: '강민호',
    teamName: '타일팀',
    workDate: '2025-12-13',
    workTypeCode: 'TILE',
    workTypeName: '타일 시공',
    workContent: '욕실 타일 시공 진행 중. 50% 완료.',
    photoUrls: ['/images/report4.jpg'],
    isApproved: false,
  },
]

// 공종 마스터 코드
export const workTypeCodes = [
  { code: 'CONCRETE', name: '콘크리트 타설' },
  { code: 'REBAR', name: '철근 배근' },
  { code: 'FORMWORK', name: '거푸집' },
  { code: 'TILE', name: '타일 시공' },
  { code: 'PAINT', name: '도장' },
  { code: 'PLUMBING', name: '배관' },
  { code: 'ELECTRIC', name: '전기' },
  { code: 'OTHER', name: '기타' },
]

