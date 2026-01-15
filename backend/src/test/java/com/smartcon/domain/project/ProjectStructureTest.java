package com.smartcon.domain.project;

import com.smartcon.domain.attendance.entity.AttendanceRecord;
import com.smartcon.domain.project.entity.FaceRecognitionDevice;
import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.project.entity.ProjectManager;
import com.smartcon.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 5단계 역할 기반 시스템 프로젝트 구조 검증 테스트
 * Task 1.1: 프로젝트 구조 설정 테스트
 * Requirements: 1.1
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("프로젝트 구조 검증 테스트")
class ProjectStructureTest {

    @Test
    @DisplayName("User 엔티티 5단계 역할 시스템 구조 검증")
    void testUserEntityRoleBasedStructure() {
        // Given: 5단계 역할을 가진 사용자 생성
        User user = User.builder()
                .name("테스트 사용자")
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .build();
        user.setCiValueDirect("CI_TEST_12345");

        // When: 각 역할 추가
        user.addRole(User.Role.ROLE_SUPER);
        user.addRole(User.Role.ROLE_HQ);
        user.addRole(User.Role.ROLE_SITE);

        // Then: 역할 시스템이 정상 작동하는지 검증
        assertThat(user.hasRole(User.Role.ROLE_SUPER)).isTrue();
        assertThat(user.hasRole(User.Role.ROLE_HQ)).isTrue();
        assertThat(user.hasRole(User.Role.ROLE_SITE)).isTrue();
        assertThat(user.hasRole(User.Role.ROLE_TEAM)).isFalse();
        assertThat(user.hasRole(User.Role.ROLE_WORKER)).isFalse();
        
        assertThat(user.isAdmin()).isTrue();
        assertThat(user.isPersonalUser()).isFalse();
        assertThat(user.isCiBasedUser()).isTrue();
    }

    @Test
    @DisplayName("Project 엔티티 안면인식기 디바이스 연동 구조 검증")
    void testProjectEntityFaceDeviceIntegration() {
        // Given: 프로젝트 생성
        Project project = Project.builder()
                .name("테스트 현장")
                .constructionPeriodStart(LocalDate.now())
                .constructionPeriodEnd(LocalDate.now().plusMonths(6))
                .siteManagerName("현장소장")
                .status(Project.ProjectStatus.ACTIVE)
                .build();

        // When: 안면인식기 디바이스 추가
        FaceRecognitionDevice device1 = FaceRecognitionDevice.builder()
                .serialNumber("FACE_001")
                .deviceName("정문 안면인식기")
                .apiEndpoint("http://192.168.1.100:8080/api")
                .syncStatus(FaceRecognitionDevice.DeviceSyncStatus.SYNCED)
                .build();

        FaceRecognitionDevice device2 = FaceRecognitionDevice.builder()
                .serialNumber("FACE_002")
                .deviceName("후문 안면인식기")
                .apiEndpoint("http://192.168.1.101:8080/api")
                .syncStatus(FaceRecognitionDevice.DeviceSyncStatus.PENDING)
                .build();

        project.addFaceDevice(device1);
        project.addFaceDevice(device2);

        // Then: 안면인식기 연동 구조 검증
        assertThat(project.getFaceDevices()).hasSize(2);
        assertThat(project.getFaceDevices().get(0).getProject()).isSameAs(project);
        assertThat(project.getFaceDevices().get(1).getProject()).isSameAs(project);
        assertThat(project.isActive()).isTrue();
    }

    @Test
    @DisplayName("AttendanceRecord 엔티티 안면인식 연동 및 시간 수정 구조 검증")
    void testAttendanceRecordFaceRecognitionAndTimeModification() {
        // Given: 사용자와 프로젝트 생성
        User worker = User.builder()
                .name("노무자")
                .email("worker@example.com")
                .phoneNumber("010-9876-5432")
                .build();
        worker.setCiValueDirect("CI_WORKER_12345");
        worker.addRole(User.Role.ROLE_WORKER);

        Project project = Project.builder()
                .name("테스트 현장")
                .status(Project.ProjectStatus.ACTIVE)
                .build();

        // When: 안면인식 출역 기록 생성
        AttendanceRecord record = AttendanceRecord.builder()
                .worker(worker)
                .project(project)
                .workDate(LocalDate.now())
                .checkInTime(LocalDateTime.now().withHour(8).withMinute(0))
                .checkOutTime(LocalDateTime.now().withHour(17).withMinute(0))
                .jobType(AttendanceRecord.JobType.GENERAL_LABORER)
                .unitPrice(new BigDecimal("150000"))
                .faceMatchConfidence(new BigDecimal("0.95"))
                .dataSource(AttendanceRecord.AttendanceDataSource.FACE_RECOGNITION)
                .build();

        record.calculateWorkHours();

        // Then: 안면인식 데이터 구조 검증
        assertThat(record.isFaceRecognitionData()).isTrue();
        assertThat(record.isConfidenceAboveThreshold()).isTrue();
        assertThat(record.isWorkCompleted()).isTrue();
        assertThat(record.getWorkHours()).isEqualTo(new BigDecimal("9.00"));
        assertThat(record.isTimeModified()).isFalse();

        // When: 시간 수정 (현장관리자 권한)
        LocalDateTime newCheckIn = LocalDateTime.now().withHour(7).withMinute(30);
        LocalDateTime newCheckOut = LocalDateTime.now().withHour(17).withMinute(30);
        record.modifyTime(newCheckIn, newCheckOut, "출근시간 조정", 1L);

        // Then: 시간 수정 구조 검증
        assertThat(record.isTimeModified()).isTrue();
        assertThat(record.getDataSource()).isEqualTo(AttendanceRecord.AttendanceDataSource.ADMIN_CORRECTION);
        assertThat(record.getTimeModificationReason()).isEqualTo("출근시간 조정");
        assertThat(record.getLastModifiedBy()).isEqualTo(1L);
        assertThat(record.getOriginalCheckInTime()).isNotNull();
        assertThat(record.getOriginalCheckOutTime()).isNotNull();
    }

    @Test
    @DisplayName("ProjectManager 엔티티 역할 매핑 구조 검증")
    void testProjectManagerRoleMapping() {
        // Given: 프로젝트와 관리자 생성
        Project project = Project.builder()
                .name("테스트 현장")
                .status(Project.ProjectStatus.ACTIVE)
                .build();

        User siteManager = User.builder()
                .name("현장관리자")
                .email("site@example.com")
                .businessNumber("123-45-67890")
                .build();
        siteManager.addRole(User.Role.ROLE_SITE);

        // When: 프로젝트 관리자 매핑
        ProjectManager projectManager = ProjectManager.builder()
                .user(siteManager)
                .role(ProjectManager.ManagerRole.SITE_MANAGER)
                .assignedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        project.addProjectManager(projectManager);

        // Then: 역할 매핑 구조 검증
        assertThat(project.getProjectManagers()).hasSize(1);
        assertThat(project.getProjectManagers().get(0).getUser()).isSameAs(siteManager);
        assertThat(project.getProjectManagers().get(0).getRole()).isEqualTo(ProjectManager.ManagerRole.SITE_MANAGER);
        assertThat(project.getProjectManagers().get(0).isActive()).isTrue();
        assertThat(siteManager.isBusinessUser()).isTrue();
        assertThat(siteManager.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("멀티테넌트 구조 기본 검증")
    void testMultiTenantStructure() {
        // Given: 테넌트 ID를 가진 엔티티들 생성
        User user = User.builder()
                .name("테스트 사용자")
                .email("test@example.com")
                .build();
        user.setTenantId(1L); // BaseTenantEntity에서 상속

        Project project = Project.builder()
                .name("테스트 프로젝트")
                .status(Project.ProjectStatus.ACTIVE)
                .build();
        project.setTenantId(1L);

        AttendanceRecord record = AttendanceRecord.builder()
                .worker(user)
                .project(project)
                .workDate(LocalDate.now())
                .dataSource(AttendanceRecord.AttendanceDataSource.MANUAL_ENTRY)
                .build();
        record.setTenantId(1L);

        // Then: 멀티테넌트 구조 검증
        assertThat(user.getTenantId()).isEqualTo(1L);
        assertThat(project.getTenantId()).isEqualTo(1L);
        assertThat(record.getTenantId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("5단계 역할별 권한 레벨 검증")
    void testRoleHierarchyLevels() {
        // Given: 각 역할의 표시명과 권한 레벨 검증
        
        // Then: 역할별 표시명 검증
        assertThat(User.Role.ROLE_SUPER.getDisplayName()).isEqualTo("슈퍼관리자");
        assertThat(User.Role.ROLE_HQ.getDisplayName()).isEqualTo("본사관리자");
        assertThat(User.Role.ROLE_SITE.getDisplayName()).isEqualTo("현장관리자");
        assertThat(User.Role.ROLE_TEAM.getDisplayName()).isEqualTo("노무팀장");
        assertThat(User.Role.ROLE_WORKER.getDisplayName()).isEqualTo("일반노무자");

        // 프로젝트 상태 표시명 검증
        assertThat(Project.ProjectStatus.ACTIVE.getDisplayName()).isEqualTo("진행중");
        assertThat(Project.ProjectStatus.PAUSED.getDisplayName()).isEqualTo("일시중지");
        assertThat(Project.ProjectStatus.COMPLETED.getDisplayName()).isEqualTo("완료");

        // 출역 데이터 소스 표시명 검증
        assertThat(AttendanceRecord.AttendanceDataSource.FACE_RECOGNITION.getDisplayName()).isEqualTo("안면인식기");
        assertThat(AttendanceRecord.AttendanceDataSource.MANUAL_ENTRY.getDisplayName()).isEqualTo("수동입력");
        assertThat(AttendanceRecord.AttendanceDataSource.ADMIN_CORRECTION.getDisplayName()).isEqualTo("관리자수정");
    }
}