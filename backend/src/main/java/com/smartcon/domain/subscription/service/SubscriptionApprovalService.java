package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.CreateSubscriptionRequest;
import com.smartcon.domain.subscription.dto.SubscriptionApprovalDto;
import com.smartcon.domain.subscription.dto.SubscriptionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 구독 승인 서비스 인터페이스
 * 
 * 구독 신청의 승인, 거부, 중지, 종료, 재활성화 등의 기능을 제공합니다.
 * 슈퍼관리자가 구독을 관리할 수 있는 포괄적인 기능을 포함합니다.
 */
public interface SubscriptionApprovalService {
    
    /**
     * 구독 승인
     * 
     * @param subscriptionId 구독 ID
     * @param reason 승인 사유
     * @return 승인된 구독 정보
     */
    SubscriptionDto approveSubscription(Long subscriptionId, String reason);
    
    /**
     * 구독 거부
     * 
     * @param subscriptionId 구독 ID
     * @param reason 거부 사유
     * @return 거부된 구독 정보
     */
    SubscriptionDto rejectSubscription(Long subscriptionId, String reason);
    
    /**
     * 구독 중지
     * 
     * @param subscriptionId 구독 ID
     * @param reason 중지 사유
     * @return 중지된 구독 정보
     */
    SubscriptionDto suspendSubscription(Long subscriptionId, String reason);
    
    /**
     * 구독 종료
     * 
     * @param subscriptionId 구독 ID
     * @param reason 종료 사유
     * @return 종료된 구독 정보
     */
    SubscriptionDto terminateSubscription(Long subscriptionId, String reason);
    
    /**
     * 구독 재활성화
     * 
     * @param subscriptionId 구독 ID
     * @param reason 재활성화 사유
     * @return 재활성화된 구독 정보
     */
    SubscriptionDto reactivateSubscription(Long subscriptionId, String reason);
    
    /**
     * 승인 대기 중인 구독 목록 조회
     * 
     * @param pageable 페이지 정보
     * @return 승인 대기 구독 목록
     */
    Page<SubscriptionDto> getPendingApprovals(Pageable pageable);
    
    /**
     * 구독 승인 이력 조회
     * 
     * @param subscriptionId 구독 ID
     * @return 승인 이력 목록
     */
    List<SubscriptionApprovalDto> getApprovalHistory(Long subscriptionId);
    
    /**
     * 자동 승인 여부 확인
     * 
     * @param request 구독 신청 요청
     * @return 자동 승인 가능 여부
     */
    boolean checkAutoApproval(CreateSubscriptionRequest request);
    
    /**
     * 자동 승인 처리
     * 
     * @param request 구독 신청 요청
     * @return 자동 승인된 구독 정보
     */
    SubscriptionDto processAutoApproval(CreateSubscriptionRequest request);
}