package com.smartcon.domain.attendance.controller;

import com.smartcon.domain.attendance.dto.*;
import com.smartcon.domain.attendance.service.AttendanceService;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 출역 관리 컨트롤러
 * 안면인식기 연동 및 실시간 출역 데이터 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * 출역 기록 생성
     */
    @PostMapping("/records")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> createAttendanceRecord(
            @Valid @RequestBody AttendanceRecordRequest request) {
        log.info("[API] 출역 기록 생성 요청: 노무자 ID={}, 프로젝트 ID={}", 
                request.getWorkerId(), request.getProjectId());

        AttendanceRecordResponse response = attendanceService.createAttendanceRecord(request);
        return ResponseEntity.ok(ApiResponse.success(response, "출역 기록이 생성되었습니다"));
    }

    /**
     * 출역 기록 조회 (역할별 권한 적용)
     */
    @GetMapping("/records")
    public ResponseEntity<ApiResponse<Page<AttendanceRecordResponse>>> getAttendanceRecords(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
            @RequestParam(required = false) String teamName,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "workDate,desc") String sort,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long userId) {
        
        log.info("[API] 출역 기록 조회 요청: 프로젝트 ID={}, 노무자 ID={}, 기간={} ~ {}", 
                projectId, workerId, startDate, endDate);

        AttendanceQueryRequest query = AttendanceQueryRequest.builder()
                .projectId(projectId)
                .workerId(workerId)
                .startDate(startDate)
                .endDate(endDate)
                .workDate(workDate)
                .teamName(teamName)
                .page(page)
                .size(size)
                .sort(sort)
                .build();

        Role userRole = role != null ? Role.valueOf(role) : Role.ROLE_WORKER;
        Page<AttendanceRecordResponse> response = attendanceService.getAttendanceRecords(query, userRole, userId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "출역 기록 조회 성공"));
    }

    /**
     * 출역 기록 상세 조회
     */
    @GetMapping("/records/{recordId}")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> getAttendanceRecordById(
            @PathVariable Long recordId) {
        log.info("[API] 출역 기록 상세 조회 요청: ID={}", recordId);

        AttendanceRecordResponse response = attendanceService.getAttendanceRecordById(recordId);
        return ResponseEntity.ok(ApiResponse.success(response, "출역 기록 조회 성공"));
    }

    /**
     * 출근/퇴근 시간 수정 (현장관리자 권한)
     */
    @PutMapping("/records/{recordId}/time")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> modifyAttendanceTime(
            @PathVariable Long recordId,
            @Valid @RequestBody TimeModificationRequest request,
            @RequestParam Long modifierId) {
        log.info("[API] 출역 시간 수정 요청: ID={}, 수정자 ID={}", recordId, modifierId);

        AttendanceRecordResponse response = attendanceService.modifyAttendanceTime(recordId, request, modifierId);
        return ResponseEntity.ok(ApiResponse.success(response, "출역 시간이 수정되었습니다"));
    }

    /**
     * 안면인식기로부터 실시간 출역 데이터 수신 (Webhook)
     */
    @PostMapping("/face-recognition/webhook")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> receiveAttendanceFromFaceNet(
            @Valid @RequestBody FaceNetAttendanceData data) {
        log.info("[API] 안면인식 출역 데이터 수신: 디바이스={}, 노무자 ID={}, 이벤트={}", 
                data.getDeviceSerialNumber(), data.getWorkerId(), data.getEventType());

        AttendanceRecordResponse response = attendanceService.receiveAttendanceFromFaceNet(data);
        return ResponseEntity.ok(ApiResponse.success(response, "안면인식 출역 데이터 처리 완료"));
    }

    /**
     * 특정 프로젝트의 오늘 출역 현황 조회
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getTodayAttendance(
            @RequestParam Long projectId) {
        log.info("[API] 오늘 출역 현황 조회 요청: 프로젝트 ID={}", projectId);

        List<AttendanceRecordResponse> response = attendanceService.getTodayAttendance(projectId);
        return ResponseEntity.ok(ApiResponse.success(response, "오늘 출역 현황 조회 성공"));
    }

    /**
     * 특정 노무자의 출역 이력 조회
     */
    @GetMapping("/workers/{workerId}/history")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getWorkerAttendanceHistory(
            @PathVariable Long workerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("[API] 노무자 출역 이력 조회 요청: 노무자 ID={}, 기간={} ~ {}", 
                workerId, startDate, endDate);

        List<AttendanceRecordResponse> response = attendanceService.getWorkerAttendanceHistory(
                workerId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response, "노무자 출역 이력 조회 성공"));
    }

    /**
     * 출역 기록 삭제 (관리자 권한)
     */
    @DeleteMapping("/records/{recordId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttendanceRecord(
            @PathVariable Long recordId,
            @RequestParam String role) {
        log.info("[API] 출역 기록 삭제 요청: ID={}, 역할={}", recordId, role);

        Role userRole = Role.valueOf(role);
        attendanceService.deleteAttendanceRecord(recordId, userRole);
        return ResponseEntity.ok(ApiResponse.success(null, "출역 기록이 삭제되었습니다"));
    }
}
