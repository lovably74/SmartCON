package com.smartcon.domain.contract;

import com.smartcon.domain.attendance.entity.AttendanceRecord;
import com.smartcon.domain.contract.dto.ContractRequest;
import com.smartcon.domain.contract.dto.ContractResponse;
import com.smartcon.domain.contract.dto.SignContractRequest;
import com.smartcon.domain.contract.entity.Contract;
import com.smartcon.domain.contract.exception.ContractNotFoundException;
import com.smartcon.domain.contract.exception.UnauthorizedContractAccessException;
import com.smartcon.domain.contract.repository.ContractRepository;
import com.smartcon.domain.contract.service.ContractServiceImpl;
import com.smartcon.domain.contract.service.SignatureValidationService;
import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.project.repository.ProjectRepository;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 근로계약 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("근로계약 서비스 테스트")
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SignatureValidationService signatureValidationService;

    @InjectMocks
    private ContractServiceImpl contractService;

    private User worker;
    private User manager;
    private Project project;
    private Contract contract;

    @BeforeEach
    void setUp() {
        // 노무자 설정
        worker = User.builder()
                .name("김노무")
                .email("worker@test.com")
                .roles(Set.of(Role.ROLE_WORKER))
                .build();
        worker.setId(1L);

        // 관리자 설정
        manager = User.builder()
                .name("이관리")
                .email("manager@test.com")
                .roles(Set.of(Role.ROLE_SITE))
                .build();
        manager.setId(2L);

        // 프로젝트 설정
        project = Project.builder()
                .name("테스트 현장")
                .status(Project.ProjectStatus.ACTIVE)
                .build();
        project.setId(1L);

        // 계약 설정
        contract = Contract.builder()
                .worker(worker)
                .project(project)
                .workDate(LocalDate.now().plusDays(1))
                .jobType(AttendanceRecord.JobType.CARPENTER)
                .unitPrice(new BigDecimal("150000"))
                .status(Contract.ContractStatus.PENDING)
                .createdBy(manager)
                .build();
        contract.setId(1L);
    }

    @Test
    @DisplayName("계약 생성 성공")
    void createContract_Success() {
        // Given
        ContractRequest request = ContractRequest.builder()
                .workerId(1L)
                .projectId(1L)
                .workDate(LocalDate.now().plusDays(1))
                .jobType(AttendanceRecord.JobType.CARPENTER)
                .unitPrice(new BigDecimal("150000"))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        // When
        ContractResponse response = contractService.createContract(request, 2L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getWorkerId()).isEqualTo(1L);
        assertThat(response.getProjectId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(Contract.ContractStatus.PENDING);
        verify(contractRepository, times(1)).save(any(Contract.class));
    }

    @Test
    @DisplayName("계약 조회 성공")
    void getContract_Success() {
        // Given
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        // When
        ContractResponse response = contractService.getContract(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getWorkerName()).isEqualTo("김노무");
    }

    @Test
    @DisplayName("계약 조회 실패 - 존재하지 않는 계약")
    void getContract_NotFound() {
        // Given
        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> contractService.getContract(999L))
                .isInstanceOf(ContractNotFoundException.class)
                .hasMessageContaining("계약을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("계약서 서명 성공")
    void signContract_Success() {
        // Given
        String signatureData = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        SignContractRequest request = SignContractRequest.builder()
                .contractId(1L)
                .signatureData(signatureData)
                .build();

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(signatureValidationService.validateSignature(signatureData)).thenReturn(true);
        when(signatureValidationService.normalizeSignature(signatureData)).thenReturn(signatureData);
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        // When
        ContractResponse response = contractService.signContract(request, 1L);

        // Then
        assertThat(response).isNotNull();
        verify(signatureValidationService, times(1)).validateSignature(signatureData);
        verify(contractRepository, times(1)).save(any(Contract.class));
    }

    @Test
    @DisplayName("계약서 서명 실패 - 권한 없음")
    void signContract_Unauthorized() {
        // Given
        String signatureData = "validSignatureData";
        SignContractRequest request = SignContractRequest.builder()
                .contractId(1L)
                .signatureData(signatureData)
                .build();

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        // When & Then
        assertThatThrownBy(() -> contractService.signContract(request, 999L))
                .isInstanceOf(UnauthorizedContractAccessException.class)
                .hasMessageContaining("본인의 계약만 서명할 수 있습니다");
    }

    @Test
    @DisplayName("계약서 서명 실패 - 유효하지 않은 서명 데이터")
    void signContract_InvalidSignature() {
        // Given
        String invalidSignatureData = "invalid";
        SignContractRequest request = SignContractRequest.builder()
                .contractId(1L)
                .signatureData(invalidSignatureData)
                .build();

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(signatureValidationService.validateSignature(invalidSignatureData)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> contractService.signContract(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 서명 데이터입니다");
    }

    @Test
    @DisplayName("계약 취소 성공 - 생성자")
    void cancelContract_ByCreator_Success() {
        // Given
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        // When
        ContractResponse response = contractService.cancelContract(1L, 2L);

        // Then
        assertThat(response).isNotNull();
        verify(contractRepository, times(1)).save(any(Contract.class));
    }

    @Test
    @DisplayName("계약 취소 실패 - 권한 없음")
    void cancelContract_Unauthorized() {
        // Given
        User unauthorizedUser = User.builder()
                .name("무권한")
                .email("unauthorized@test.com")
                .roles(Set.of(Role.ROLE_WORKER))
                .build();
        unauthorizedUser.setId(999L);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(userRepository.findById(999L)).thenReturn(Optional.of(unauthorizedUser));

        // When & Then
        assertThatThrownBy(() -> contractService.cancelContract(1L, 999L))
                .isInstanceOf(UnauthorizedContractAccessException.class)
                .hasMessageContaining("계약을 취소할 권한이 없습니다");
    }

    @Test
    @DisplayName("계약서 재발송 성공")
    void resendContract_Success() {
        // Given
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        // When
        ContractResponse response = contractService.resendContract(1L, "재발송 사유", 2L);

        // Then
        assertThat(response).isNotNull();
        verify(contractRepository, times(1)).save(any(Contract.class));
    }
}
