package com.smartcon.domain.attendance;

import com.smartcon.domain.attendance.service.FaceRecognitionIntegrationServiceImpl;
import com.smartcon.domain.project.dto.DeviceSyncStatusResponse;
import com.smartcon.domain.project.entity.FaceRecognitionDevice;
import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.project.repository.FaceRecognitionDeviceRepository;
import com.smartcon.domain.project.repository.ProjectRepository;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 안면인식기 연동 신뢰성 속성 테스트
 * Feature: smartcon-lite-role-based-system, Property 17
 * 
 * Property 17: Face Recognition Integration Reliability
 * For any worker approval and face recognition device registration, 
 * the system should successfully sync worker data to all associated devices 
 * and maintain accurate sync status tracking
 * Validates: Requirements 31.1, 31.3, 31.4
 */
class FaceRecognitionIntegrationPropertyTest {

    private TestContext createTestContext() {
        FaceRecognitionDeviceRepository deviceRepository = Mockito.mock(FaceRecognitionDeviceRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        ProjectRepository projectRepository = Mockito.mock(ProjectRepository.class);
        
        FaceRecognitionIntegrationServiceImpl faceRecognitionService = new FaceRecognitionIntegrationServiceImpl(
            deviceRepository,
            userRepository,
            projectRepository
        );
        
        return new TestContext(deviceRepository, userRepository, projectRepository, faceRecognitionService);
    }
    
    private static class TestContext {
        final FaceRecognitionDeviceRepository deviceRepository;
        final UserRepository userRepository;
        final ProjectRepository projectRepository;
        final FaceRecognitionIntegrationServiceImpl faceRecognitionService;
        
        TestContext(FaceRecognitionDeviceRepository deviceRepository,
                   UserRepository userRepository,
                   ProjectRepository projectRepository,
                   FaceRecognitionIntegrationServiceImpl faceRecognitionService) {
            this.deviceRepository = deviceRepository;
            this.userRepository = userRepository;
            this.projectRepository = projectRepository;
            this.faceRecognitionService = faceRecognitionService;
        }
    }

    /**
     * Property 17.1: 안면인식 신뢰도 검증
     * 
     * 모든 안면인식 신뢰도 값에 대해:
     * 0.85 이상의 신뢰도는 유효하고, 0.85 미만의 신뢰도는 무효해야 함
     */
    @Property(tries = 100)
    @Label("Property 17.1: 안면인식 신뢰도 검증이 올바르게 동작한다")
    void faceMatchConfidenceValidationWorks(
            @ForAll("confidenceValues") BigDecimal confidence) {

        TestContext ctx = createTestContext();

        // When: 신뢰도 검증
        boolean isValid = ctx.faceRecognitionService.validateFaceMatchConfidence(confidence);

        // Then: 0.85 이상이면 유효, 미만이면 무효
        BigDecimal threshold = new BigDecimal("0.85");
        boolean expectedValid = confidence != null && confidence.compareTo(threshold) >= 0;
        
        assertThat(isValid)
                .as("신뢰도 %s의 검증 결과가 예상과 일치해야 함 (임계값: 0.85)", confidence)
                .isEqualTo(expectedValid);
    }

    /**
     * Property 17.2: 디바이스 동기화 상태 추적
     * 
     * 모든 디바이스에 대해:
     * 동기화 상태 업데이트 후 조회하면 업데이트된 상태가 반환되어야 함
     */
    @Property(tries = 100)
    @Label("Property 17.2: 디바이스 동기화 상태 추적이 올바르게 동작한다")
    void deviceSyncStatusTrackingWorks(
            @ForAll("deviceWithStatus") DeviceWithStatus deviceWithStatus) {

        TestContext ctx = createTestContext();

        // Given: 디바이스와 새로운 상태
        FaceRecognitionDevice device = deviceWithStatus.device;
        FaceRecognitionDevice.DeviceSyncStatus newStatus = deviceWithStatus.newStatus;

        // Mock 설정
        when(ctx.deviceRepository.findBySerialNumber(device.getSerialNumber()))
                .thenReturn(Optional.of(device));
        when(ctx.deviceRepository.save(any(FaceRecognitionDevice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When: 동기화 상태 업데이트
        ctx.faceRecognitionService.updateDeviceSyncStatus(device.getSerialNumber(), newStatus);

        // Then: 디바이스의 동기화 상태가 업데이트되어야 함
        assertThat(device.getSyncStatus())
                .as("디바이스 %s의 동기화 상태가 %s로 업데이트되어야 함", 
                        device.getSerialNumber(), newStatus)
                .isEqualTo(newStatus);
    }

    /**
     * Property 17.3: 프로젝트 디바이스 상태 조회
     * 
     * 모든 프로젝트에 대해:
     * 프로젝트의 모든 디바이스 상태를 조회하면 해당 프로젝트에 속한 디바이스만 반환되어야 함
     */
    @Property(tries = 100)
    @Label("Property 17.3: 프로젝트 디바이스 상태 조회가 올바르게 동작한다")
    void projectDeviceStatusRetrievalWorks(
            @ForAll("projectWithDevices") ProjectWithDevices projectWithDevices) {

        TestContext ctx = createTestContext();

        // Given: 프로젝트와 디바이스 목록
        Project project = projectWithDevices.project;
        List<FaceRecognitionDevice> devices = projectWithDevices.devices;

        // Mock 설정
        when(ctx.deviceRepository.findByProjectId(project.getId()))
                .thenReturn(devices);

        // When: 프로젝트 디바이스 상태 조회
        List<DeviceSyncStatusResponse> statusList = ctx.faceRecognitionService
                .getProjectDeviceStatuses(project.getId());

        // Then: 모든 디바이스의 상태가 반환되어야 함
        assertThat(statusList)
                .as("프로젝트 %d의 디바이스 상태 목록 크기가 일치해야 함", project.getId())
                .hasSize(devices.size());

        // 각 디바이스의 상태가 올바르게 매핑되어야 함
        for (int i = 0; i < devices.size(); i++) {
            FaceRecognitionDevice device = devices.get(i);
            DeviceSyncStatusResponse status = statusList.get(i);
            
            assertThat(status.getSerialNumber())
                    .as("디바이스 시리얼 번호가 일치해야 함")
                    .isEqualTo(device.getSerialNumber());
            // enum 타입으로 반환되는 상태를 직접 비교
            assertThat(status.getSyncStatus())
                    .as("디바이스 동기화 상태가 일치해야 함")
                    .isEqualTo(device.getSyncStatus());
        }
    }

    /**
     * Property 17.4: 노무자 등록 후 디바이스 상태 업데이트
     * 
     * 모든 노무자와 디바이스에 대해:
     * 노무자 등록 성공 시 디바이스 상태가 SYNCED로 업데이트되어야 함
     */
    @Property(tries = 100)
    @Label("Property 17.4: 노무자 등록 후 디바이스 상태가 SYNCED로 업데이트된다")
    void workerRegistrationUpdatesSyncStatus(
            @ForAll("workerAndDevice") WorkerAndDevice workerAndDevice) {

        TestContext ctx = createTestContext();

        // Given: 노무자와 디바이스
        User worker = workerAndDevice.worker;
        FaceRecognitionDevice device = workerAndDevice.device;

        // Mock 설정
        when(ctx.userRepository.findById(worker.getId()))
                .thenReturn(Optional.of(worker));
        when(ctx.deviceRepository.findBySerialNumber(device.getSerialNumber()))
                .thenReturn(Optional.of(device));
        when(ctx.deviceRepository.save(any(FaceRecognitionDevice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When: 노무자 등록
        ctx.faceRecognitionService.registerWorkerToDevice(worker.getId(), device.getSerialNumber());

        // Then: 디바이스 상태가 SYNCED로 업데이트되어야 함
        assertThat(device.getSyncStatus())
                .as("노무자 등록 후 디바이스 상태가 SYNCED여야 함")
                .isEqualTo(FaceRecognitionDevice.DeviceSyncStatus.SYNCED);
    }

    // ========== Arbitraries (데이터 생성기) ==========

    @Provide
    Arbitrary<BigDecimal> confidenceValues() {
        return Arbitraries.doubles().between(0.0, 1.0)
                .map(d -> BigDecimal.valueOf(d).setScale(4, java.math.RoundingMode.HALF_UP));
    }

    @Provide
    Arbitrary<DeviceWithStatus> deviceWithStatus() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofLength(10),
            Arbitraries.of(FaceRecognitionDevice.DeviceSyncStatus.values())
        ).as((serialNumber, status) -> {
            FaceRecognitionDevice device = createDevice(serialNumber, 
                    FaceRecognitionDevice.DeviceSyncStatus.PENDING);
            return new DeviceWithStatus(device, status);
        });
    }

    @Provide
    Arbitrary<ProjectWithDevices> projectWithDevices() {
        return Arbitraries.integers().between(1, 5).flatMap(deviceCount -> {
            Long projectId = 1L;
            Project project = createProject(projectId);
            
            List<FaceRecognitionDevice> devices = new ArrayList<>();
            for (int i = 0; i < deviceCount; i++) {
                devices.add(createDevice("DEVICE-" + i, 
                        FaceRecognitionDevice.DeviceSyncStatus.SYNCED));
            }
            
            return Arbitraries.just(new ProjectWithDevices(project, devices));
        });
    }

    @Provide
    Arbitrary<WorkerAndDevice> workerAndDevice() {
        return Combinators.combine(
            Arbitraries.longs().between(1L, 100L),
            Arbitraries.strings().alpha().ofLength(10)
        ).as((workerId, serialNumber) -> {
            User worker = createWorker(workerId);
            FaceRecognitionDevice device = createDevice(serialNumber, 
                    FaceRecognitionDevice.DeviceSyncStatus.PENDING);
            return new WorkerAndDevice(worker, device);
        });
    }

    // ========== Helper Methods ==========

    private User createWorker(Long id) {
        User worker = User.builder()
                .name("Worker" + id)
                .email("worker" + id + "@test.com")
                .roles(Set.of(Role.ROLE_WORKER))
                .build();
        worker.setId(id);
        return worker;
    }

    private FaceRecognitionDevice createDevice(String serialNumber, 
                                              FaceRecognitionDevice.DeviceSyncStatus status) {
        FaceRecognitionDevice device = FaceRecognitionDevice.builder()
                .serialNumber(serialNumber)
                .deviceName("Device " + serialNumber)
                .apiEndpoint("https://api.facenet.com/" + serialNumber)
                .apiKey("API_KEY_" + serialNumber)
                .isActive(true)
                .syncStatus(status)
                .build();
        device.setId(1L);
        device.setTenantId(1L);
        return device;
    }

    private Project createProject(Long id) {
        Project project = Project.builder()
                .name("Project" + id)
                .constructionPeriodStart(LocalDate.now())
                .constructionPeriodEnd(LocalDate.now().plusMonths(6))
                .status(Project.ProjectStatus.ACTIVE)
                .projectManagers(new ArrayList<>())
                .build();
        project.setId(id);
        project.setTenantId(1L);
        return project;
    }

    // ========== Data Classes ==========

    private static class DeviceWithStatus {
        final FaceRecognitionDevice device;
        final FaceRecognitionDevice.DeviceSyncStatus newStatus;

        DeviceWithStatus(FaceRecognitionDevice device, FaceRecognitionDevice.DeviceSyncStatus newStatus) {
            this.device = device;
            this.newStatus = newStatus;
        }
    }

    private static class ProjectWithDevices {
        final Project project;
        final List<FaceRecognitionDevice> devices;

        ProjectWithDevices(Project project, List<FaceRecognitionDevice> devices) {
            this.project = project;
            this.devices = devices;
        }
    }

    private static class WorkerAndDevice {
        final User worker;
        final FaceRecognitionDevice device;

        WorkerAndDevice(User worker, FaceRecognitionDevice device) {
            this.worker = worker;
            this.device = device;
        }
    }
}
