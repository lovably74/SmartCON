package com.smartcon.domain.contract.controller;

import com.smartcon.domain.contract.dto.*;
import com.smartcon.domain.contract.entity.Contract;
import com.smartcon.domain.contract.service.ContractService;
import com.smartcon.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 근로계약 관리 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    /**
     * 계약 생성
     * 
     * @param request 계약 생성 요청
     * @param createdByUserId 생성자 사용자 ID (헤더 또는 인증 정보에서 추출)
     * @return 생성된 계약 정보
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(
            @Valid @RequestBody ContractRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long createdByUserId) {
        
        log.info("계약 생성 API 호출 - 노무자 ID: {}, 프로젝트 ID: {}", 
                request.getWorkerId(), request.getProjectId());

        // TODO: 실제 구현에서는 Spring Security를 통해 인증된 사용자 ID를 가져와야 함
        if (createdByUserId == null) {
            createdByUserId = 1L; // 임시 기본값
        }

        ContractResponse response = contractService.createContract(request, createdByUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "계약이 성공적으로 생성되었습니다"));
    }

    /**
     * 계약 조회
     * 
     * @param contractId 계약 ID
     * @return 계약 정보
     */
    @GetMapping("/{contractId}")
    public ResponseEntity<ApiResponse<ContractResponse>> getContract(
            @PathVariable Long contractId) {
        
        log.debug("계약 조회 API 호출 - 계약 ID: {}", contractId);

        ContractResponse response = contractService.getContract(contractId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 노무자별 계약 목록 조회
     * 
     * @param workerId 노무자 ID
     * @return 계약 목록
     */
    @GetMapping("/worker/{workerId}")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getContractsByWorker(
            @PathVariable Long workerId) {
        
        log.debug("노무자별 계약 목록 조회 API 호출 - 노무자 ID: {}", workerId);

        List<ContractResponse> response = contractService.getContractsByWorker(workerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로젝트별 계약 목록 조회
     * 
     * @param projectId 프로젝트 ID
     * @return 계약 목록
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getContractsByProject(
            @PathVariable Long projectId) {
        
        log.debug("프로젝트별 계약 목록 조회 API 호출 - 프로젝트 ID: {}", projectId);

        List<ContractResponse> response = contractService.getContractsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 상태별 계약 목록 조회 (페이징)
     * 
     * @param status 계약 상태
     * @param pageable 페이징 정보
     * @return 계약 목록 (페이징)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> getContractsByStatus(
            @PathVariable Contract.ContractStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.debug("상태별 계약 목록 조회 API 호출 - 상태: {}", status);

        Page<ContractResponse> response = contractService.getContractsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로젝트별 상태별 계약 목록 조회 (페이징)
     * 
     * @param projectId 프로젝트 ID
     * @param status 계약 상태
     * @param pageable 페이징 정보
     * @return 계약 목록 (페이징)
     */
    @GetMapping("/project/{projectId}/status/{status}")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> getContractsByProjectAndStatus(
            @PathVariable Long projectId,
            @PathVariable Contract.ContractStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.debug("프로젝트별 상태별 계약 목록 조회 API 호출 - 프로젝트 ID: {}, 상태: {}", projectId, status);

        Page<ContractResponse> response = contractService.getContractsByProjectAndStatus(projectId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 계약서 서명
     * 
     * @param request 서명 요청
     * @param workerId 노무자 ID (헤더 또는 인증 정보에서 추출)
     * @return 서명된 계약 정보
     */
    @PostMapping("/sign")
    public ResponseEntity<ApiResponse<ContractResponse>> signContract(
            @Valid @RequestBody SignContractRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long workerId) {
        
        log.info("계약서 서명 API 호출 - 계약 ID: {}", request.getContractId());

        // TODO: 실제 구현에서는 Spring Security를 통해 인증된 사용자 ID를 가져와야 함
        if (workerId == null) {
            workerId = 1L; // 임시 기본값
        }

        ContractResponse response = contractService.signContract(request, workerId);
        return ResponseEntity.ok(ApiResponse.success(response, "계약서가 성공적으로 서명되었습니다"));
    }

    /**
     * 계약 수정 요청
     * 
     * @param request 수정 요청
     * @param workerId 노무자 ID (헤더 또는 인증 정보에서 추출)
     * @return 수정 요청된 계약 정보
     */
    @PostMapping("/modification-request")
    public ResponseEntity<ApiResponse<ContractResponse>> requestModification(
            @Valid @RequestBody ModificationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long workerId) {
        
        log.info("계약 수정 요청 API 호출 - 계약 ID: {}", request.getContractId());

        // TODO: 실제 구현에서는 Spring Security를 통해 인증된 사용자 ID를 가져와야 함
        if (workerId == null) {
            workerId = 1L; // 임시 기본값
        }

        ContractResponse response = contractService.requestModification(request, workerId);
        return ResponseEntity.ok(ApiResponse.success(response, "계약 수정 요청이 성공적으로 등록되었습니다"));
    }

    /**
     * 계약 취소
     * 
     * @param contractId 계약 ID
     * @param userId 사용자 ID (헤더 또는 인증 정보에서 추출)
     * @return 취소된 계약 정보
     */
    @PostMapping("/{contractId}/cancel")
    public ResponseEntity<ApiResponse<ContractResponse>> cancelContract(
            @PathVariable Long contractId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("계약 취소 API 호출 - 계약 ID: {}", contractId);

        // TODO: 실제 구현에서는 Spring Security를 통해 인증된 사용자 ID를 가져와야 함
        if (userId == null) {
            userId = 1L; // 임시 기본값
        }

        ContractResponse response = contractService.cancelContract(contractId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "계약이 성공적으로 취소되었습니다"));
    }

    /**
     * 수정 요청이 있는 계약 목록 조회
     * 
     * @return 수정 요청 계약 목록
     */
    @GetMapping("/modification-requests")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getContractsWithModificationRequests() {
        
        log.debug("수정 요청이 있는 계약 목록 조회 API 호출");

        List<ContractResponse> response = contractService.getContractsWithModificationRequests();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로젝트별 수정 요청이 있는 계약 목록 조회
     * 
     * @param projectId 프로젝트 ID
     * @return 수정 요청 계약 목록
     */
    @GetMapping("/project/{projectId}/modification-requests")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getContractsWithModificationRequestsByProject(
            @PathVariable Long projectId) {
        
        log.debug("프로젝트별 수정 요청이 있는 계약 목록 조회 API 호출 - 프로젝트 ID: {}", projectId);

        List<ContractResponse> response = contractService.getContractsWithModificationRequestsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로젝트별 상태별 계약 수 조회
     * 
     * @param projectId 프로젝트 ID
     * @param status 계약 상태
     * @return 계약 수
     */
    @GetMapping("/project/{projectId}/status/{status}/count")
    public ResponseEntity<ApiResponse<Long>> countContractsByProjectAndStatus(
            @PathVariable Long projectId,
            @PathVariable Contract.ContractStatus status) {
        
        log.debug("프로젝트별 상태별 계약 수 조회 API 호출 - 프로젝트 ID: {}, 상태: {}", projectId, status);

        long count = contractService.countContractsByProjectAndStatus(projectId, status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * 계약서 재발송
     * 
     * @param request 재발송 요청
     * @param userId 사용자 ID (헤더 또는 인증 정보에서 추출)
     * @return 재발송된 계약 정보
     */
    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<ContractResponse>> resendContract(
            @Valid @RequestBody ResendContractRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("계약서 재발송 API 호출 - 계약 ID: {}", request.getContractId());

        // TODO: 실제 구현에서는 Spring Security를 통해 인증된 사용자 ID를 가져와야 함
        if (userId == null) {
            userId = 1L; // 임시 기본값
        }

        ContractResponse response = contractService.resendContract(
                request.getContractId(), 
                request.getReason(), 
                userId
        );
        return ResponseEntity.ok(ApiResponse.success(response, "계약서가 성공적으로 재발송되었습니다"));
    }
}
