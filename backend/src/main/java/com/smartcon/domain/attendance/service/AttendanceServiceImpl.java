package com.smartcon.domain.attendance.service;

import com.smartcon.domain.attendance.dto.*;
import com.smartcon.domain.attendance.entity.AttendanceRecord;
import com.smartcon.domain.attendance.repository.AttendanceRecordRepository;
import com.smartcon.domain.project.entity.FaceRecognitionDevice;
import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.project.repository.FaceRecognitionDeviceRepository;
import com.smartcon.domain.project.repository.ProjectRepository;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.global.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 출역 관리 서비스 구현체
 * 안면인식기 연동 및 실시간 출역 데이터 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final FaceRecognitionDeviceRepository faceRecognitionDeviceRepository;

    @Override
    @Transactional
    public AttendanceRecordResponse createAttendanceRecord(AttendanceRecordRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[출역 기록 생성] 테넌트: {}, 노무자 ID: {}, 프로젝트 ID: {}, 근무일자: {}", 
                tenantId, request.getWorkerId(), request.getProjectId(), request.getWorkDate());

        // 노무자 조회
        User worker = userRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new IllegalArgumentException("노무자를 찾을 수 없습니다: " + request.getWorkerId()));

        // 프로젝트 조회
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + request.getProjectId()));

        // 출역 기록 생성
        AttendanceRecord record = AttendanceRecord.builder()
                .worker(worker)
                .project(project)
                .workDate(request.getWorkDate())
                .checkInTime(request.getCheckInTime())
                .checkOutTime(request.getCheckOutTime())
                .unitPrice(request.getUnitPrice())
                .jobType(request.getJobType())
                .teamName(request.getTeamName())
                .faceMatchConfidence(request.getFaceMatchConfidence())
                .dataSource(request.getDataSource() != null ? request.getDataSource() : AttendanceRecord.AttendanceDataSource.MANUAL_ENTRY)
                .checkInLatitude(request.getCheckInLatitude())
                .checkInLongitude(request.getCheckInLongitude())
                .checkOutLatitude(request.getCheckOutLatitude())
                .checkOutLongitude(request.getCheckOutLongitude())
                .build();

        // 근무시간 계산
        if (record.isWorkCompleted()) {
            record.calculateWorkHours();
        }

        AttendanceRecord savedRecord = attendanceRecordRepository.save(record);
        log.info("[출역 기록 생성 완료] ID: {}, 노무자: {}, 프로젝트: {}", 
                savedRecord.getId(), worker.getName(), project.getName());

        return AttendanceRecordResponse.from(savedRecord);
    }

    @Override
    public Page<AttendanceRecordResponse> getAttendanceRecords(AttendanceQueryRequest query, Role userRole, Long userId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[출역 기록 조회] 테넌트: {}, 역할: {}, 사용자 ID: {}", tenantId, userRole, userId);

        List<AttendanceRecord> records = new ArrayList<>();

        // 역할별 권한 적용
        if (query.getProjectId() != null) {
            // 프로젝트별 조회
            if (query.getStartDate() != null && query.getEndDate() != null) {
                records = attendanceRecordRepository.findByTenantIdAndWorkDateBetween(
                        tenantId, query.getStartDate(), query.getEndDate())
                        .stream()
                        .filter(r -> r.getProject().getId().equals(query.getProjectId()))
                        .collect(Collectors.toList());
            } else if (query.getWorkDate() != null) {
                records = attendanceRecordRepository.findByTenantIdAndWorkDate(tenantId, query.getWorkDate())
                        .stream()
                        .filter(r -> r.getProject().getId().equals(query.getProjectId()))
                        .collect(Collectors.toList());
            } else {
                records = attendanceRecordRepository.findByTenantIdAndProjectId(tenantId, query.getProjectId());
            }
        } else if (query.getWorkerId() != null) {
            // 노무자별 조회
            if (query.getStartDate() != null && query.getEndDate() != null) {
                records = attendanceRecordRepository.findByTenantIdAndWorkDateBetween(
                        tenantId, query.getStartDate(), query.getEndDate())
                        .stream()
                        .filter(r -> r.getWorker().getId().equals(query.getWorkerId()))
                        .collect(Collectors.toList());
            } else {
                records = attendanceRecordRepository.findByTenantIdAndWorkerId(tenantId, query.getWorkerId());
            }
        } else if (query.getWorkDate() != null) {
            // 날짜별 조회
            records = attendanceRecordRepository.findByTenantIdAndWorkDate(tenantId, query.getWorkDate());
        } else if (query.getStartDate() != null && query.getEndDate() != null) {
            // 기간별 조회
            records = attendanceRecordRepository.findByTenantIdAndWorkDateBetween(
                    tenantId, query.getStartDate(), query.getEndDate());
        } else {
            // 전체 조회
            records = attendanceRecordRepository.findByTenantId(tenantId);
        }

        // 팀명 필터링
        if (query.getTeamName() != null && !query.getTeamName().trim().isEmpty()) {
            records = records.stream()
                    .filter(r -> r.getTeamName() != null && r.getTeamName().contains(query.getTeamName()))
                    .collect(Collectors.toList());
        }

        // 정렬
        records.sort((r1, r2) -> {
            if (query.getSort().contains("desc")) {
                return r2.getWorkDate().compareTo(r1.getWorkDate());
            } else {
                return r1.getWorkDate().compareTo(r2.getWorkDate());
            }
        });

        // 페이징 처리
        int start = query.getPage() * query.getSize();
        int end = Math.min(start + query.getSize(), records.size());
        List<AttendanceRecord> pagedRecords = records.subList(start, end);

        List<AttendanceRecordResponse> responses = pagedRecords.stream()
                .map(AttendanceRecordResponse::from)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, PageRequest.of(query.getPage(), query.getSize()), records.size());
    }

    @Override
    public AttendanceRecordResponse getAttendanceRecordById(Long recordId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[출역 기록 상세 조회] 테넌트: {}, 기록 ID: {}", tenantId, recordId);

        AttendanceRecord record = attendanceRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("출역 기록을 찾을 수 없습니다: " + recordId));

        // 테넌트 검증
        if (!tenantId.equals(record.getTenantId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        return AttendanceRecordResponse.from(record);
    }

    @Override
    @Transactional
    public AttendanceRecordResponse modifyAttendanceTime(Long recordId, TimeModificationRequest request, Long modifierId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[출역 시간 수정] 테넌트: {}, 기록 ID: {}, 수정자 ID: {}", tenantId, recordId, modifierId);

        // 출역 기록 조회
        AttendanceRecord record = attendanceRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("출역 기록을 찾을 수 없습니다: " + recordId));

        // 테넌트 검증
        if (!tenantId.equals(record.getTenantId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        // 시간 수정
        record.modifyTime(request.getCheckInTime(), request.getCheckOutTime(), request.getReason(), modifierId);

        AttendanceRecord savedRecord = attendanceRecordRepository.save(record);
        log.info("[출역 시간 수정 완료] ID: {}, 수정자: {}, 사유: {}", 
                savedRecord.getId(), modifierId, request.getReason());

        return AttendanceRecordResponse.from(savedRecord);
    }

    @Override
    @Transactional
    public AttendanceRecordResponse receiveAttendanceFromFaceNet(FaceNetAttendanceData data) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[안면인식 출역 데이터 수신] 테넌트: {}, 디바이스: {}, 노무자 ID: {}, 이벤트: {}", 
                tenantId, data.getDeviceSerialNumber(), data.getWorkerId(), data.getEventType());

        // 신뢰도 검증
        if (!data.isConfidenceAboveThreshold()) {
            log.warn("[안면인식 신뢰도 부족] 신뢰도: {}, 임계값: 0.85", data.getConfidence());
            throw new IllegalArgumentException("안면인식 신뢰도가 임계값(0.85) 미만입니다: " + data.getConfidence());
        }

        // 디바이스 조회
        FaceRecognitionDevice device = faceRecognitionDeviceRepository.findBySerialNumber(data.getDeviceSerialNumber())
                .orElseThrow(() -> new IllegalArgumentException("안면인식기를 찾을 수 없습니다: " + data.getDeviceSerialNumber()));

        // 노무자 조회
        User worker = userRepository.findById(data.getWorkerId())
                .orElseThrow(() -> new IllegalArgumentException("노무자를 찾을 수 없습니다: " + data.getWorkerId()));

        Project project = device.getProject();
        LocalDate workDate = data.getRecognitionTime().toLocalDate();

        // 기존 출역 기록 조회 또는 생성
        AttendanceRecord record = attendanceRecordRepository
                .findByTenantIdAndWorkerIdAndWorkDate(tenantId, data.getWorkerId(), workDate)
                .orElseGet(() -> {
                    AttendanceRecord newRecord = AttendanceRecord.builder()
                            .worker(worker)
                            .project(project)
                            .workDate(workDate)
                            .dataSource(AttendanceRecord.AttendanceDataSource.FACE_RECOGNITION)
                            .faceMatchConfidence(data.getConfidence())
                            .build();
                    return newRecord;
                });

        // 출근/퇴근 처리
        if (data.isCheckInEvent()) {
            record.setCheckInTime(data.getRecognitionTime());
            record.setCheckInLatitude(data.getLatitude());
            record.setCheckInLongitude(data.getLongitude());
            log.info("[출근 처리] 노무자: {}, 시간: {}", worker.getName(), data.getRecognitionTime());
        } else if (data.isCheckOutEvent()) {
            record.setCheckOutTime(data.getRecognitionTime());
            record.setCheckOutLatitude(data.getLatitude());
            record.setCheckOutLongitude(data.getLongitude());
            log.info("[퇴근 처리] 노무자: {}, 시간: {}", worker.getName(), data.getRecognitionTime());
        }

        // 근무시간 계산
        if (record.isWorkCompleted()) {
            record.calculateWorkHours();
        }

        AttendanceRecord savedRecord = attendanceRecordRepository.save(record);
        log.info("[안면인식 출역 데이터 처리 완료] ID: {}, 노무자: {}, 프로젝트: {}", 
                savedRecord.getId(), worker.getName(), project.getName());

        return AttendanceRecordResponse.from(savedRecord);
    }

    @Override
    public List<AttendanceRecordResponse> getTodayAttendance(Long projectId) {
        String tenantId = TenantContext.getCurrentTenant();
        LocalDate today = LocalDate.now();
        log.info("[오늘 출역 현황 조회] 테넌트: {}, 프로젝트 ID: {}, 날짜: {}", tenantId, projectId, today);

        List<AttendanceRecord> records = attendanceRecordRepository.findTodayAttendanceByProject(tenantId, projectId, today);

        return records.stream()
                .map(AttendanceRecordResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceRecordResponse> getWorkerAttendanceHistory(Long workerId, LocalDate startDate, LocalDate endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[노무자 출역 이력 조회] 테넌트: {}, 노무자 ID: {}, 기간: {} ~ {}", 
                tenantId, workerId, startDate, endDate);

        List<AttendanceRecord> records = attendanceRecordRepository.findByTenantIdAndWorkDateBetween(
                        tenantId, startDate, endDate)
                .stream()
                .filter(r -> r.getWorker().getId().equals(workerId))
                .collect(Collectors.toList());

        return records.stream()
                .map(AttendanceRecordResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAttendanceRecord(Long recordId, Role userRole) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("[출역 기록 삭제] 테넌트: {}, 기록 ID: {}, 역할: {}", tenantId, recordId, userRole);

        // 관리자 권한 확인
        if (!isAdminRole(userRole)) {
            throw new IllegalArgumentException("출역 기록 삭제 권한이 없습니다");
        }

        // 출역 기록 조회
        AttendanceRecord record = attendanceRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("출역 기록을 찾을 수 없습니다: " + recordId));

        // 테넌트 검증
        if (!tenantId.equals(record.getTenantId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        attendanceRecordRepository.delete(record);
        log.info("[출역 기록 삭제 완료] ID: {}", recordId);
    }

    /**
     * 관리자 역할인지 확인
     */
    private boolean isAdminRole(Role role) {
        return role == Role.ROLE_SUPER || role == Role.ROLE_HQ || role == Role.ROLE_SITE;
    }
}
