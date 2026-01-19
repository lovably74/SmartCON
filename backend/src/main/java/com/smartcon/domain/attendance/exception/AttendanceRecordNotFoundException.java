package com.smartcon.domain.attendance.exception;

/**
 * 출역 기록을 찾을 수 없을 때 발생하는 예외
 */
public class AttendanceRecordNotFoundException extends RuntimeException {
    
    public AttendanceRecordNotFoundException(String message) {
        super(message);
    }
    
    public AttendanceRecordNotFoundException(Long recordId) {
        super("출역 기록을 찾을 수 없습니다: " + recordId);
    }
}
