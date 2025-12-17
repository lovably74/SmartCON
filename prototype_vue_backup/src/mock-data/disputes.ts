export interface AttendanceDispute {
  id: number
  userId: number
  userName: string
  attendanceLogId: number
  disputeDate: string
  reason: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  submittedAt: string
  processedBy?: number
  processedByName?: string
  processedAt?: string
  processComment?: string
}

export const mockDisputes: AttendanceDispute[] = [
  {
    id: 1,
    userId: 4,
    userName: '박민수',
    attendanceLogId: 2,
    disputeDate: '2025-12-13',
    reason: '지각으로 기록되었으나 실제로는 8시 50분에 도착했습니다. 안면인식이 늦게 처리된 것 같습니다.',
    status: 'PENDING',
    submittedAt: '2025-12-13T10:00:00',
  },
  {
    id: 2,
    userId: 5,
    userName: '최지훈',
    attendanceLogId: 3,
    disputeDate: '2025-12-12',
    reason: '출역했으나 기록이 없습니다.',
    status: 'APPROVED',
    submittedAt: '2025-12-12T15:30:00',
    processedBy: 2,
    processedByName: '김철수',
    processedAt: '2025-12-12T16:00:00',
    processComment: '확인 결과 출역 사실 확인. 수동 승인 처리 완료.',
  },
  {
    id: 3,
    userId: 8,
    userName: '장영수',
    attendanceLogId: 4,
    disputeDate: '2025-12-13',
    reason: '안면인식 실패로 수동 승인되었으나 실제로는 정상 출역했습니다.',
    status: 'REJECTED',
    submittedAt: '2025-12-13T11:00:00',
    processedBy: 2,
    processedByName: '김철수',
    processedAt: '2025-12-13T11:30:00',
    processComment: '안면인식 실패 사유 확인. 수동 승인 처리 적절함.',
  },
]

