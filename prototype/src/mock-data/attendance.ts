export interface AttendanceLog {
  id: number
  userId: number
  userName: string
  siteId: number
  siteName: string
  checkInTime: string
  status: 'NORMAL' | 'LATE' | 'ABSENT' | 'MANUAL'
  authType: 'FACE' | 'MANUAL'
  dailyManDay: number
  manualReason?: string
  teamName?: string
}

export const mockAttendanceLogs: AttendanceLog[] = [
  {
    id: 1,
    userId: 3,
    userName: '이영희',
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    checkInTime: '2025-12-13T08:30:00',
    status: 'NORMAL',
    authType: 'FACE',
    dailyManDay: 1.0,
    teamName: '콘크리트팀',
  },
  {
    id: 2,
    userId: 4,
    userName: '박민수',
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    checkInTime: '2025-12-13T09:15:00',
    status: 'LATE',
    authType: 'FACE',
    dailyManDay: 1.0,
    teamName: '철근팀',
  },
  {
    id: 3,
    userId: 5,
    userName: '최지훈',
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    checkInTime: '2025-12-13T08:45:00',
    status: 'NORMAL',
    authType: 'FACE',
    dailyManDay: 1.0,
    teamName: '콘크리트팀',
  },
  {
    id: 4,
    userId: 8,
    userName: '장영수',
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    checkInTime: '2025-12-13T10:00:00',
    status: 'MANUAL',
    authType: 'MANUAL',
    dailyManDay: 1.0,
    manualReason: '안면인식 실패',
    teamName: '철근팀',
  },
  {
    id: 5,
    userId: 9,
    userName: '윤서연',
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    checkInTime: '2025-12-13T08:20:00',
    status: 'NORMAL',
    authType: 'FACE',
    dailyManDay: 1.0,
    teamName: '콘크리트팀',
  },
  {
    id: 6,
    userId: 10,
    userName: '오대현',
    siteId: 2,
    siteName: '서울 송파구 건설 현장',
    checkInTime: '2025-12-13T08:35:00',
    status: 'NORMAL',
    authType: 'FACE',
    dailyManDay: 1.0,
    teamName: '타일팀',
  },
]

// 오늘 날짜의 출역 기록만 필터링
export function getTodayAttendanceLogs(): AttendanceLog[] {
  const today = new Date().toISOString().split('T')[0]
  return mockAttendanceLogs.filter(log => log.checkInTime.startsWith(today))
}

// 특정 현장의 출역 기록
export function getAttendanceLogsBySite(siteId: number): AttendanceLog[] {
  return mockAttendanceLogs.filter(log => log.siteId === siteId)
}

// 특정 사용자의 출역 기록
export function getAttendanceLogsByUser(userId: number): AttendanceLog[] {
  return mockAttendanceLogs.filter(log => log.userId === userId)
}

