export interface AttendanceLog {
  id: number
  userId: number
  userName: string
  siteId: number
  siteName: string
  checkInTime: string
  checkOutTime?: string
  status: 'NORMAL' | 'LATE' | 'ABSENT'
  authType: 'FACE' | 'MANUAL'
  dailyManDay: number
  manualReason?: string
}

export const mockAttendanceLogs: AttendanceLog[] = [
  {
    id: 1,
    userId: 3,
    userName: '이영희',
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    checkInTime: '2025-12-17T08:30:00',
    checkOutTime: '2025-12-17T18:00:00',
    status: 'NORMAL',
    authType: 'FACE',
    dailyManDay: 1.0,
  },
  {
    id: 2,
    userId: 4,
    userName: '박민수',
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    checkInTime: '2025-12-17T07:15:00',
    checkOutTime: '2025-12-17T17:45:00',
    status: 'NORMAL',
    authType: 'FACE',
    dailyManDay: 1.0,
  },
  {
    id: 3,
    userId: 5,
    userName: '최철근',
    siteId: 1,
    siteName: '서울 강남구 건설 현장',
    checkInTime: '2025-12-17T07:05:00',
    status: 'LATE',
    authType: 'FACE',
    dailyManDay: 1.0,
  },
]





