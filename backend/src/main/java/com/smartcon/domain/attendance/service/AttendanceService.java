package com.smartcon.domain.attendance.service;

import com.smartcon.domain.attendance.dto.*;
import com.smartcon.domain.attendance.entity.AttendanceRecord;
import com.smartcon.domain.user.entity.Role;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

/**
 * 출역 관리 서비스 인터페이스
 * 안면인식기 연동 및 실시간 출역 데이터 처리
 */
public interface AttendanceService {

    /**
     * 출역 기록 생성
     */
    AttendanceRecordResponse createAttendanceRecord(AttendanceRecordRequest request);

    /**
     * 출역 기록 조회 (역할별 권한 적용)
     */
    Page<AttendanceRecordResponse> getAttendanceRecords(AttendanceQueryRequest query, Role userRole, Long userId);

    /**
     * 출역 기록 상세 조회
     */
    AttendanceRecordResponse getAttendanceRecordById(Long recordId);

    /**
     * 출근/퇴근 시간 수정 (현장관리자 권한)
     */
    AttendanceRecordResponse modifyAttendanceTime(Long recordId, TimeModificationRequest request, Long modifierId);

    /**
     * 안면인식기로부터 실시간 출역 데이터 수신
     */
    AttendanceRecordResponse receiveAttendanceFromFaceNet(FaceNetAttendanceData data);

    /**
     * 특정 프로젝트의 오늘 출역 현황 조회
     */
    List<AttendanceRecordResponse> getTodayAttendance(Long projectId);

    /**
     * 특정 노무자의 출역 기록 조회
     */
    List<AttendanceRecordResponse> getWorkerAttendanceHistory(Long workerId, LocalDate startDate, LocalDate endDate);

    /**
     * 출역 기록 삭제 (관리자 권한)
     */
    void deleteAttendanceRecord(Long recordId, Role userRole);
}
