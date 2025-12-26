export interface Team {
  id: number
  name: string
  leaderId: number
  leaderName: string
  siteId: number
  siteName: string
  workType: string
  memberCount: number
  status: 'ACTIVE' | 'INACTIVE'
}

export interface TeamMember {
  id: number
  teamId: number
  workerId: number
  workerName: string
  phone: string
  workType: string
  joinedAt: string
}

export const mockTeams: Team[] = [
  {
    id: 1,
    name: '철근팀',
    leaderId: 2,
    leaderName: '김철수',
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    workType: '철근',
    memberCount: 15,
    status: 'ACTIVE',
  },
  {
    id: 2,
    name: '콘크리트팀',
    leaderId: 3,
    leaderName: '이영희',
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    workType: '콘크리트',
    memberCount: 12,
    status: 'ACTIVE',
  },
]

export const mockTeamMembers: TeamMember[] = [
  {
    id: 1,
    teamId: 1,
    workerId: 4,
    workerName: '박민수',
    phone: '010-4567-8901',
    workType: '철근',
    joinedAt: '2025-12-01',
  },
]



