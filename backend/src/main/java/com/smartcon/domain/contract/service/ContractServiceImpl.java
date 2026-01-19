package com.smartcon.domain.contract.service;

import com.smartcon.domain.contract.dto.*;
import com.smartcon.domain.contract.entity.Contract;
import com.smartcon.domain.contract.exception.ContractNotFoundException;
import com.smartcon.domain.contract.exception.UnauthorizedContractAccessException;
import com.smartcon.domain.contract.repository.ContractRepository;
import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.project.exception.ProjectNotFoundException;
import com.smartcon.domain.project.repository.ProjectRepository;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.exception.UserNotFoundException;
import com.smartcon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 근로계약 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final SignatureValidationService signatureValidationService;

    @Override
    @Transactional
    public ContractResponse createContract(ContractRequest request, Long createdByUserId) {
        log.info("계약 생성 시작 - 노무자 ID: {}, 프로젝트 ID: {}, 근무일자: {}", 
                request.getWorkerId(), request.getProjectId(), request.getWorkDate());

        // 노무자 조회
        User worker = userRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new UserNotFoundException("노무자를 찾을 수 없습니다: " + request.getWorkerId()));

        // 프로젝트 조회
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + request.getProjectId()));

        // 생성자 조회
        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new UserNotFoundException("생성자를 찾을 수 없습니다: " + createdByUserId));

        // 계약 생성
        Contract contract = Contract.builder()
                .worker(worker)
                .project(project)
                .workDate(request.getWorkDate())
                .jobType(request.getJobType())
                .unitPrice(request.getUnitPrice())
                .expiryDate(request.getExpiryDate())
                .createdBy(createdBy)
                .status(Contract.ContractStatus.PENDING)
                .build();

        Contract savedContract = contractRepository.save(contract);
        log.info("계약 생성 완료 - 계약 ID: {}", savedContract.getId());

        return ContractResponse.from(savedContract);
    }

    @Override
    public ContractResponse getContract(Long contractId) {
        log.debug("계약 조회 - 계약 ID: {}", contractId);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException("계약을 찾을 수 없습니다: " + contractId));

        return ContractResponse.from(contract);
    }

    @Override
    public List<ContractResponse> getContractsByWorker(Long workerId) {
        log.debug("노무자별 계약 목록 조회 - 노무자 ID: {}", workerId);

        // 노무자 존재 확인
        if (!userRepository.existsById(workerId)) {
            throw new UserNotFoundException("노무자를 찾을 수 없습니다: " + workerId);
        }

        List<Contract> contracts = contractRepository.findByWorkerId(workerId);
        return contracts.stream()
                .map(ContractResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<ContractResponse> getContractsByProject(Long projectId) {
        log.debug("프로젝트별 계약 목록 조회 - 프로젝트 ID: {}", projectId);

        // 프로젝트 존재 확인
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + projectId);
        }

        List<Contract> contracts = contractRepository.findByProjectId(projectId);
        return contracts.stream()
                .map(ContractResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ContractResponse> getContractsByStatus(Contract.ContractStatus status, Pageable pageable) {
        log.debug("상태별 계약 목록 조회 - 상태: {}", status);

        Page<Contract> contracts = contractRepository.findByStatus(status, pageable);
        return contracts.map(ContractResponse::from);
    }

    @Override
    public Page<ContractResponse> getContractsByProjectAndStatus(Long projectId, Contract.ContractStatus status, Pageable pageable) {
        log.debug("프로젝트별 상태별 계약 목록 조회 - 프로젝트 ID: {}, 상태: {}", projectId, status);

        // 프로젝트 존재 확인
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + projectId);
        }

        Page<Contract> contracts = contractRepository.findByProjectIdAndStatus(projectId, status, pageable);
        return contracts.map(ContractResponse::from);
    }

    @Override
    @Transactional
    public ContractResponse signContract(SignContractRequest request, Long workerId) {
        log.info("계약서 서명 시작 - 계약 ID: {}, 노무자 ID: {}", request.getContractId(), workerId);

        // 계약 조회
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ContractNotFoundException("계약을 찾을 수 없습니다: " + request.getContractId()));

        // 권한 확인 (본인의 계약만 서명 가능)
        if (!contract.getWorker().getId().equals(workerId)) {
            throw new UnauthorizedContractAccessException("본인의 계약만 서명할 수 있습니다");
        }

        // 서명 데이터 검증
        if (!signatureValidationService.validateSignature(request.getSignatureData())) {
            throw new IllegalArgumentException("유효하지 않은 서명 데이터입니다");
        }

        // 서명 데이터 정규화
        String normalizedSignature = signatureValidationService.normalizeSignature(request.getSignatureData());

        // 서명 처리
        contract.sign(normalizedSignature);
        Contract signedContract = contractRepository.save(contract);

        log.info("계약서 서명 완료 - 계약 ID: {}, 서명 크기: {} bytes", 
                signedContract.getId(), 
                signatureValidationService.getSignatureSize(normalizedSignature));
        
        return ContractResponse.from(signedContract);
    }

    @Override
    @Transactional
    public ContractResponse requestModification(ModificationRequest request, Long workerId) {
        log.info("계약 수정 요청 시작 - 계약 ID: {}, 노무자 ID: {}", request.getContractId(), workerId);

        // 계약 조회
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ContractNotFoundException("계약을 찾을 수 없습니다: " + request.getContractId()));

        // 권한 확인 (본인의 계약만 수정 요청 가능)
        if (!contract.getWorker().getId().equals(workerId)) {
            throw new UnauthorizedContractAccessException("본인의 계약만 수정 요청할 수 있습니다");
        }

        // 수정 요청 처리
        contract.requestModification(request.getModificationRequest());
        Contract modifiedContract = contractRepository.save(contract);

        log.info("계약 수정 요청 완료 - 계약 ID: {}", modifiedContract.getId());
        return ContractResponse.from(modifiedContract);
    }

    @Override
    @Transactional
    public ContractResponse cancelContract(Long contractId, Long userId) {
        log.info("계약 취소 시작 - 계약 ID: {}, 사용자 ID: {}", contractId, userId);

        // 계약 조회
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException("계약을 찾을 수 없습니다: " + contractId));

        // 권한 확인 (계약 생성자 또는 관리자만 취소 가능)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        boolean isCreator = contract.getCreatedBy() != null && contract.getCreatedBy().getId().equals(userId);
        boolean isAdmin = user.isAdmin();

        if (!isCreator && !isAdmin) {
            throw new UnauthorizedContractAccessException("계약을 취소할 권한이 없습니다");
        }

        // 취소 처리
        contract.cancel();
        Contract cancelledContract = contractRepository.save(contract);

        log.info("계약 취소 완료 - 계약 ID: {}", cancelledContract.getId());
        return ContractResponse.from(cancelledContract);
    }

    @Override
    @Transactional
    public int processExpiredContracts() {
        log.info("만료된 계약 자동 처리 시작");

        LocalDate currentDate = LocalDate.now();
        List<Contract> expiredContracts = contractRepository.findExpiredContracts(currentDate);

        int processedCount = 0;
        for (Contract contract : expiredContracts) {
            try {
                contract.expire();
                contractRepository.save(contract);
                processedCount++;
            } catch (Exception e) {
                log.error("계약 만료 처리 실패 - 계약 ID: {}", contract.getId(), e);
            }
        }

        log.info("만료된 계약 자동 처리 완료 - 처리 건수: {}", processedCount);
        return processedCount;
    }

    @Override
    public List<ContractResponse> getContractsWithModificationRequests() {
        log.debug("수정 요청이 있는 계약 목록 조회");

        List<Contract> contracts = contractRepository.findContractsWithModificationRequests();
        return contracts.stream()
                .map(ContractResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<ContractResponse> getContractsWithModificationRequestsByProject(Long projectId) {
        log.debug("프로젝트별 수정 요청이 있는 계약 목록 조회 - 프로젝트 ID: {}", projectId);

        // 프로젝트 존재 확인
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + projectId);
        }

        List<Contract> contracts = contractRepository.findContractsWithModificationRequestsByProjectId(projectId);
        return contracts.stream()
                .map(ContractResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public long countContractsByProjectAndStatus(Long projectId, Contract.ContractStatus status) {
        log.debug("프로젝트별 상태별 계약 수 조회 - 프로젝트 ID: {}, 상태: {}", projectId, status);

        // 프로젝트 존재 확인
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + projectId);
        }

        return contractRepository.countByProjectIdAndStatus(projectId, status);
    }

    @Override
    @Transactional
    public ContractResponse resendContract(Long contractId, String reason, Long userId) {
        log.info("계약서 재발송 시작 - 계약 ID: {}, 사용자 ID: {}, 사유: {}", contractId, userId, reason);

        // 계약 조회
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException("계약을 찾을 수 없습니다: " + contractId));

        // 권한 확인 (계약 생성자 또는 관리자만 재발송 가능)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        boolean isCreator = contract.getCreatedBy() != null && contract.getCreatedBy().getId().equals(userId);
        boolean isAdmin = user.isAdmin();

        if (!isCreator && !isAdmin) {
            throw new UnauthorizedContractAccessException("계약서를 재발송할 권한이 없습니다");
        }

        // 재발송 가능한 상태 확인 (PENDING 또는 EXPIRED 상태만 재발송 가능)
        if (contract.getStatus() != Contract.ContractStatus.PENDING && 
            contract.getStatus() != Contract.ContractStatus.EXPIRED) {
            throw new IllegalStateException(
                String.format("재발송 불가능한 상태입니다: %s", contract.getStatus().getDisplayName())
            );
        }

        // 상태를 PENDING으로 변경 (만료된 경우)
        if (contract.getStatus() == Contract.ContractStatus.EXPIRED) {
            contract.setStatus(Contract.ContractStatus.PENDING);
        }

        // 기존 서명 데이터 및 수정 요청 초기화
        contract.setSignatureData(null);
        contract.setSignedAt(null);
        contract.setModificationRequest(null);

        Contract resentContract = contractRepository.save(contract);

        log.info("계약서 재발송 완료 - 계약 ID: {}", resentContract.getId());
        
        // TODO: 실제 구현에서는 노무자에게 알림 발송 (SMS, 푸시 알림 등)
        
        return ContractResponse.from(resentContract);
    }
}
