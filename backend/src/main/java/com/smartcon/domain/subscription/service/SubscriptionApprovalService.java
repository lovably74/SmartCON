package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.CreateSubscriptionRequest;
import com.smartcon.domain.subscription.dto.SubscriptionApprovalDto;
import com.smartcon.domain.subscription.dto.SubscriptionDto;
import com.smartcon.domain.subscription.dto.SubscriptionStatsDto;
import com.smartcon.domain.subscription.dto.MonthlyApprovalStatsDto;
import com.smartcon.domain.subscription.dto.AdminApprovalStatsDto;
import com.smartcon.domain.subscription.dto.DailyApprovalStatsDto;
import com.smartcon.domain.subscription.dto.AutoApprovalEfficiencyDto;
import com.smartcon.domain.subscription.dto.ApprovalPerformanceDto;
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
    
    // =============================================================================
    // 성능 최적화된 메서드들
    // =============================================================================
    
    /**
     * 승인 대기 목록 조회 (성능 최적화)
     * 
     * @param limit 조회 개수
     * @param offset 시작 위치
     * @return 승인 대기 구독 목록
     */
    List<SubscriptionDto> getPendingApprovalsOptimized(int limit, int offset);
    
    /**
     * 승인 대기 목록 총 개수 조회 (성능 최적화)
     * 
     * @return 승인 대기 구독 총 개수
     */
    long countPendingApprovalsOptimized();
    
    /**
     * 대시보드 통계 조회 (성능 최적화)
     * 
     * @return 구독 상태별 통계
     */
    SubscriptionStatsDto getSubscriptionStatsOptimized();
    
    /**
     * 24시간 이상 대기 중인 승인 건수 조회
     * 
     * @return 지연된 승인 건수
     */
    long countOverduePendingApprovals();
    
    /**
     * 구독 필터링 조회 (성능 최적화)
     * 
     * @param status 구독 상태
     * @param tenantName 테넌트명
     * @param limit 조회 개수
     * @param offset 시작 위치
     * @return 필터링된 구독 목록
     */
    List<SubscriptionDto> getSubscriptionsFilteredOptimized(
            String status, String tenantName, int limit, int offset);
    
    /**
     * 필터링된 구독 총 개수 조회
     * 
     * @param status 구독 상태
     * @param tenantName 테넌트명
     * @return 필터링된 구독 총 개수
     */
    long countSubscriptionsFilteredOptimized(String status, String tenantName);
    
    /**
     * 커서 기반 페이지네이션을 위한 승인 대기 목록 조회
     * 
     * @param cursorId 커서 ID
     * @param limit 조회 개수
     * @return 승인 대기 구독 목록
     */
    List<SubscriptionDto> getPendingApprovalsCursorBased(Long cursorId, int limit);
    
    /**
     * 월별 승인 통계 조회
     * 
     * @return 월별 승인 통계
     */
    List<MonthlyApprovalStatsDto> getMonthlyApprovalStats();
    
    /**
     * 승인 이력 조회 (성능 최적화)
     * 
     * @param subscriptionId 구독 ID
     * @return 승인 이력 목록
     */
    List<SubscriptionApprovalDto> getApprovalHistoryOptimized(Long subscriptionId);
    
    /**
     * 관리자별 승인 통계 조회
     * 
     * @return 관리자별 승인 통계
     */
    List<AdminApprovalStatsDto> getAdminApprovalStats();
    
    /**
     * 일별 승인 통계 조회
     * 
     * @return 일별 승인 통계
     */
    List<DailyApprovalStatsDto> getDailyApprovalStats();
    
    /**
     * 자동 승인 효율성 통계 조회
     * 
     * @return 자동 승인 효율성 통계
     */
    AutoApprovalEfficiencyDto getAutoApprovalEfficiencyStats();
    
    /**
     * 승인 처리 성능 분석
     * 
     * @return 승인 처리 성능 분석 결과
     */
    List<ApprovalPerformanceDto> getApprovalPerformanceAnalysis();
}