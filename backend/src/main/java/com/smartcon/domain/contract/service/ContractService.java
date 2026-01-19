package com.smartcon.domain.contract.service;

import com.smartcon.domain.contract.dto.*;
import com.smartcon.domain.contract.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 근로계약 서비스 인터페이스
 */
public interface ContractService {

    /**
     * 계약 생성
     * @param request 계약 생성 요청
     * @param createdByUserId 생성자 사용자 ID
     * @return 생성된 계약 응답
     */
    ContractResponse createContract(ContractRequest request, Long createdByUserId);

    /**
     * 계약 조회
     * @param contractId 계약 ID
     * @return 계약 응답
     */
    ContractResponse getContract(Long contractId);

    /**
     * 노무자별 계약 목록 조회
     * @param workerId 노무자 ID
     * @return 계약 목록
     */
    List<ContractResponse> getContractsByWorker(Long workerId);

    /**
     * 프로젝트별 계약 목록 조회
     * @param projectId 프로젝트 ID
     * @return 계약 목록
     */
    List<ContractResponse> getContractsByProject(Long projectId);

    /**
     * 상태별 계약 목록 조회
     * @param status 계약 상태
     * @param pageable 페이징 정보
     * @return 계약 목록 (페이징)
     */
    Page<ContractResponse> getContractsByStatus(Contract.ContractStatus status, Pageable pageable);

    /**
     * 프로젝트별 상태별 계약 목록 조회
     * @param projectId 프로젝트 ID
     * @param status 계약 상태
     * @param pageable 페이징 정보
     * @return 계약 목록 (페이징)
     */
    Page<ContractResponse> getContractsByProjectAndStatus(Long projectId, Contract.ContractStatus status, Pageable pageable);

    /**
     * 계약서 서명
     * @param request 서명 요청
     * @param workerId 노무자 ID (서명자)
     * @return 서명된 계약 응답
     */
    ContractResponse signContract(SignContractRequest request, Long workerId);

    /**
     * 계약 수정 요청
     * @param request 수정 요청
     * @param workerId 노무자 ID (요청자)
     * @return 수정 요청된 계약 응답
     */
    ContractResponse requestModification(ModificationRequest request, Long workerId);

    /**
     * 계약 취소
     * @param contractId 계약 ID
     * @param userId 취소 요청자 ID
     * @return 취소된 계약 응답
     */
    ContractResponse cancelContract(Long contractId, Long userId);

    /**
     * 만료된 계약 자동 처리
     * @return 처리된 계약 수
     */
    int processExpiredContracts();

    /**
     * 수정 요청이 있는 계약 목록 조회
     * @return 수정 요청 계약 목록
     */
    List<ContractResponse> getContractsWithModificationRequests();

    /**
     * 프로젝트별 수정 요청이 있는 계약 목록 조회
     * @param projectId 프로젝트 ID
     * @return 수정 요청 계약 목록
     */
    List<ContractResponse> getContractsWithModificationRequestsByProject(Long projectId);

    /**
     * 프로젝트별 상태별 계약 수 조회
     * @param projectId 프로젝트 ID
     * @param status 계약 상태
     * @return 계약 수
     */
    long countContractsByProjectAndStatus(Long projectId, Contract.ContractStatus status);

    /**
     * 계약서 재발송
     * @param contractId 계약 ID
     * @param reason 재발송 사유
     * @param userId 재발송 요청자 ID
     * @return 재발송된 계약 응답
     */
    ContractResponse resendContract(Long contractId, String reason, Long userId);
}
