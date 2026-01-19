package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.*;
import com.smartcon.domain.subscription.entity.*;
import com.smartcon.domain.subscription.repository.*;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 구독 승인 서비스 구현
 * 
 * 슈퍼관리자의 구독 승인/거부/중지/종료/재활성화 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SubscriptionApprovalServiceImpl implements SubscriptionApprovalService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionApprovalRepository subscriptionApprovalRepository;
    private final AutoApprovalRuleRepository autoApprovalRuleRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final SubscriptionStateTransitionValidator stateTransitionValidator;

    @Override
    @Transactional
    public SubscriptionDto approveSubscription(Long subscriptionId, String reason) {
        log.info("구독 승인 시작 - ID: {}, 사유: {}", subscriptionId, reason);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다: " + subscriptionId));
        
        // 상태 전환 검증
        stateTransitionValidator.validateTransition(
                subscription.getStatus(), 
                SubscriptionStatus.ACTIVE
        );
        
        SubscriptionStatus oldStatus = subscription.getStatus();
        
        // 구독 승인 처리 (현재 사용자는 시스템 관리자로 가정)
        User admin = getCurrentAdmin();
        subscription.approve(admin);
        subscriptionRepository.save(subscription);
        
        // 승인 이력 기록
        SubscriptionApproval approval = SubscriptionApproval.builder()
                .subscriptionId(subscriptionId)
                .admin(admin)
                .fromStatus(oldStatus)
                .toStatus(SubscriptionStatus.ACTIVE)
                .reason(reason)
                .action(ApprovalAction.APPROVE)
                .processedAt(LocalDateTime.now())
                .autoApproved(false)
                .build();
        subscriptionApprovalRepository.save(approval);
        
        // 테넌트 상태 업데이트
        Tenant tenant = subscription.getTenant();
        tenant.setStatus(Tenant.SubscriptionStatus.ACTIVE);
        tenantRepository.save(tenant);
        
        log.info("구독 승인 완료 - ID: {}, 테넌트: {}", subscriptionId, tenant.getCompanyName());
        return SubscriptionDto.from(subscription);
    }

    @Override
    @Transactional
    public SubscriptionDto rejectSubscription(Long subscriptionId, String reason) {
        log.info("구독 거부 시작 - ID: {}, 사유: {}", subscriptionId, reason);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다: " + subscriptionId));
        
        // 상태 전환 검증
        stateTransitionValidator.validateTransition(
                subscription.getStatus(), 
                SubscriptionStatus.REJECTED
        );
        
        SubscriptionStatus oldStatus = subscription.getStatus();
        
        // 구독 거부 처리
        subscription.reject(reason);
        subscriptionRepository.save(subscription);
        
        // 승인 이력 기록
        User admin = getCurrentAdmin();
        SubscriptionApproval approval = SubscriptionApproval.builder()
                .subscriptionId(subscriptionId)
                .admin(admin)
                .fromStatus(oldStatus)
                .toStatus(SubscriptionStatus.REJECTED)
                .reason(reason)
                .action(ApprovalAction.REJECT)
                .processedAt(LocalDateTime.now())
                .autoApproved(false)
                .build();
        subscriptionApprovalRepository.save(approval);
        
        log.info("구독 거부 완료 - ID: {}", subscriptionId);
        return SubscriptionDto.from(subscription);
    }

    @Override
    @Transactional
    public SubscriptionDto suspendSubscription(Long subscriptionId, String reason) {
        log.info("구독 중지 시작 - ID: {}, 사유: {}", subscriptionId, reason);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다: " + subscriptionId));
        
        // 상태 전환 검증
        stateTransitionValidator.validateTransition(
                subscription.getStatus(), 
                SubscriptionStatus.SUSPENDED
        );
        
        SubscriptionStatus oldStatus = subscription.getStatus();
        
        // 구독 중지 처리
        subscription.suspend(reason);
        subscriptionRepository.save(subscription);
        
        // 승인 이력 기록
        User admin = getCurrentAdmin();
        SubscriptionApproval approval = SubscriptionApproval.builder()
                .subscriptionId(subscriptionId)
                .admin(admin)
                .fromStatus(oldStatus)
                .toStatus(SubscriptionStatus.SUSPENDED)
                .reason(reason)
                .action(ApprovalAction.SUSPEND)
                .processedAt(LocalDateTime.now())
                .autoApproved(false)
                .build();
        subscriptionApprovalRepository.save(approval);
        
        // 테넌트 상태 업데이트
        Tenant tenant = subscription.getTenant();
        tenant.setStatus(Tenant.SubscriptionStatus.SUSPENDED);
        tenantRepository.save(tenant);
        
        log.info("구독 중지 완료 - ID: {}", subscriptionId);
        return SubscriptionDto.from(subscription);
    }

    @Override
    @Transactional
    public SubscriptionDto terminateSubscription(Long subscriptionId, String reason) {
        log.info("구독 종료 시작 - ID: {}, 사유: {}", subscriptionId, reason);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다: " + subscriptionId));
        
        // 상태 전환 검증
        stateTransitionValidator.validateTransition(
                subscription.getStatus(), 
                SubscriptionStatus.TERMINATED
        );
        
        SubscriptionStatus oldStatus = subscription.getStatus();
        
        // 구독 종료 처리
        subscription.terminate(reason);
        subscriptionRepository.save(subscription);
        
        // 승인 이력 기록
        User admin = getCurrentAdmin();
        SubscriptionApproval approval = SubscriptionApproval.builder()
                .subscriptionId(subscriptionId)
                .admin(admin)
                .fromStatus(oldStatus)
                .toStatus(SubscriptionStatus.TERMINATED)
                .reason(reason)
                .action(ApprovalAction.TERMINATE)
                .processedAt(LocalDateTime.now())
                .autoApproved(false)
                .build();
        subscriptionApprovalRepository.save(approval);
        
        // 테넌트 상태 업데이트
        Tenant tenant = subscription.getTenant();
        tenant.setStatus(Tenant.SubscriptionStatus.TERMINATED);
        tenantRepository.save(tenant);
        
        log.info("구독 종료 완료 - ID: {}", subscriptionId);
        return SubscriptionDto.from(subscription);
    }

    @Override
    @Transactional
    public SubscriptionDto reactivateSubscription(Long subscriptionId, String reason) {
        log.info("구독 재활성화 시작 - ID: {}, 사유: {}", subscriptionId, reason);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다: " + subscriptionId));
        
        // 상태 전환 검증
        stateTransitionValidator.validateTransition(
                subscription.getStatus(), 
                SubscriptionStatus.ACTIVE
        );
        
        SubscriptionStatus oldStatus = subscription.getStatus();
        
        // 구독 재활성화 처리
        subscription.resume();
        subscriptionRepository.save(subscription);
        
        // 승인 이력 기록
        User admin = getCurrentAdmin();
        SubscriptionApproval approval = SubscriptionApproval.builder()
                .subscriptionId(subscriptionId)
                .admin(admin)
                .fromStatus(oldStatus)
                .toStatus(SubscriptionStatus.ACTIVE)
                .reason(reason)
                .action(ApprovalAction.REACTIVATE)
                .processedAt(LocalDateTime.now())
                .autoApproved(false)
                .build();
        subscriptionApprovalRepository.save(approval);
        
        // 테넌트 상태 업데이트
        Tenant tenant = subscription.getTenant();
        tenant.setStatus(Tenant.SubscriptionStatus.ACTIVE);
        tenantRepository.save(tenant);
        
        log.info("구독 재활성화 완료 - ID: {}", subscriptionId);
        return SubscriptionDto.from(subscription);
    }

    @Override
    public Page<SubscriptionDto> getPendingApprovals(Pageable pageable) {
        log.debug("승인 대기 구독 목록 조회 - 페이지: {}", pageable.getPageNumber());
        
        Page<Subscription> subscriptions = subscriptionRepository.findByStatusOrderByCreatedAtAsc(
                SubscriptionStatus.PENDING_APPROVAL, 
                pageable
        );
        
        return subscriptions.map(SubscriptionDto::from);
    }

    @Override
    public List<SubscriptionApprovalDto> getApprovalHistory(Long subscriptionId) {
        log.debug("구독 승인 이력 조회 - ID: {}", subscriptionId);
        
        List<SubscriptionApproval> approvals = subscriptionApprovalRepository
                .findBySubscriptionIdOrderByProcessedAtDesc(subscriptionId);
        
        return approvals.stream()
                .map(SubscriptionApprovalDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkAutoApproval(CreateSubscriptionRequest request) {
        log.debug("자동 승인 여부 확인");
        
        // 활성화된 자동 승인 규칙 조회
        List<AutoApprovalRule> rules = autoApprovalRuleRepository.findByIsActiveTrueOrderByPriorityDescIdAsc();
        
        // 규칙이 없으면 자동 승인 불가
        if (rules.isEmpty()) {
            return false;
        }
        
        // 간단한 구현: 규칙이 있으면 자동 승인 가능으로 간주
        return true;
    }

    @Override
    @Transactional
    public SubscriptionDto processAutoApproval(CreateSubscriptionRequest request) {
        log.info("자동 승인 처리 시작");
        
        // 간단한 구현: 첫 번째 구독을 자동 승인
        Subscription subscription = subscriptionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.PENDING_APPROVAL)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("승인 대기 중인 구독을 찾을 수 없습니다"));
        
        // 자동 승인 처리
        subscription.autoApprove();
        subscriptionRepository.save(subscription);
        
        // 승인 이력 기록 (자동 승인)
        SubscriptionApproval approval = SubscriptionApproval.builder()
                .subscriptionId(subscription.getId())
                .admin(null) // 자동 승인은 관리자 없음
                .fromStatus(SubscriptionStatus.PENDING_APPROVAL)
                .toStatus(SubscriptionStatus.AUTO_APPROVED)
                .reason("자동 승인 규칙에 따라 승인됨")
                .action(ApprovalAction.AUTO_APPROVE)
                .processedAt(LocalDateTime.now())
                .autoApproved(true)
                .build();
        subscriptionApprovalRepository.save(approval);
        
        // 테넌트 상태 업데이트
        Tenant tenant = subscription.getTenant();
        tenant.setStatus(Tenant.SubscriptionStatus.ACTIVE);
        tenantRepository.save(tenant);
        
        log.info("자동 승인 처리 완료 - ID: {}", subscription.getId());
        return SubscriptionDto.from(subscription);
    }

    // =============================================================================
    // 성능 최적화된 메서드들
    // =============================================================================

    @Override
    public List<SubscriptionDto> getPendingApprovalsOptimized(int limit, int offset) {
        log.debug("승인 대기 목록 조회 (최적화) - limit: {}, offset: {}", limit, offset);
        
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Subscription> subscriptions = subscriptionRepository.findByStatusOrderByCreatedAtAsc(
                SubscriptionStatus.PENDING_APPROVAL, 
                pageable
        );
        
        return subscriptions.stream()
                .map(SubscriptionDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public long countPendingApprovalsOptimized() {
        return subscriptionRepository.countByStatus(SubscriptionStatus.PENDING_APPROVAL);
    }

    @Override
    public SubscriptionStatsDto getSubscriptionStatsOptimized() {
        log.debug("구독 통계 조회 (최적화)");
        
        long active = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        long pending = subscriptionRepository.countByStatus(SubscriptionStatus.PENDING_APPROVAL);
        long suspended = subscriptionRepository.countByStatus(SubscriptionStatus.SUSPENDED);
        long terminated = subscriptionRepository.countByStatus(SubscriptionStatus.TERMINATED);
        long rejected = subscriptionRepository.countByStatus(SubscriptionStatus.REJECTED);
        
        return SubscriptionStatsDto.builder()
                .activeCount(active)
                .pendingCount(pending)
                .suspendedCount(suspended)
                .terminatedCount(terminated)
                .rejectedCount(rejected)
                .build();
    }

    @Override
    public long countOverduePendingApprovals() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
        return subscriptionRepository.countByStatusAndCreatedAtBefore(
                SubscriptionStatus.PENDING_APPROVAL, 
                twentyFourHoursAgo
        );
    }

    @Override
    public List<SubscriptionDto> getSubscriptionsFilteredOptimized(
            String status, String tenantName, int limit, int offset) {
        log.debug("구독 필터링 조회 (최적화) - 상태: {}, 테넌트명: {}", status, tenantName);
        
        // 간단한 구현
        List<Subscription> subscriptions = subscriptionRepository.findAll()
                .stream()
                .filter(s -> status == null || s.getStatus().name().equals(status))
                .filter(s -> tenantName == null || s.getTenant().getCompanyName().contains(tenantName))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
        
        return subscriptions.stream()
                .map(SubscriptionDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public long countSubscriptionsFilteredOptimized(String status, String tenantName) {
        return subscriptionRepository.findAll()
                .stream()
                .filter(s -> status == null || s.getStatus().name().equals(status))
                .filter(s -> tenantName == null || s.getTenant().getCompanyName().contains(tenantName))
                .count();
    }

    @Override
    public List<SubscriptionDto> getPendingApprovalsCursorBased(Long cursorId, int limit) {
        log.debug("커서 기반 승인 대기 목록 조회 - cursorId: {}, limit: {}", cursorId, limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        Page<Subscription> subscriptions = subscriptionRepository.findByStatusOrderByCreatedAtAsc(
                SubscriptionStatus.PENDING_APPROVAL, 
                pageable
        );
        
        List<Subscription> filtered = subscriptions.stream()
                .filter(s -> cursorId == null || s.getId() > cursorId)
                .limit(limit)
                .collect(Collectors.toList());
        
        return filtered.stream()
                .map(SubscriptionDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<MonthlyApprovalStatsDto> getMonthlyApprovalStats() {
        log.debug("월별 승인 통계 조회");
        // 간단한 구현: 빈 리스트 반환
        return new ArrayList<>();
    }

    @Override
    public List<SubscriptionApprovalDto> getApprovalHistoryOptimized(Long subscriptionId) {
        return getApprovalHistory(subscriptionId);
    }

    @Override
    public List<AdminApprovalStatsDto> getAdminApprovalStats() {
        log.debug("관리자별 승인 통계 조회");
        // 간단한 구현: 빈 리스트 반환
        return new ArrayList<>();
    }

    @Override
    public List<DailyApprovalStatsDto> getDailyApprovalStats() {
        log.debug("일별 승인 통계 조회");
        // 간단한 구현: 빈 리스트 반환
        return new ArrayList<>();
    }

    @Override
    public AutoApprovalEfficiencyDto getAutoApprovalEfficiencyStats() {
        log.debug("자동 승인 효율성 통계 조회");
        
        long totalApprovals = subscriptionApprovalRepository.count();
        long autoApprovals = subscriptionApprovalRepository.countByAutoApproved(true);
        long manualApprovals = totalApprovals - autoApprovals;
        
        double autoApprovalRate = totalApprovals > 0 
                ? (double) autoApprovals / totalApprovals * 100 
                : 0.0;
        
        return AutoApprovalEfficiencyDto.builder()
                .autoApproved(autoApprovals)
                .manualProcessed(manualApprovals)
                .autoApprovalRate(autoApprovalRate)
                .build();
    }

    @Override
    public List<ApprovalPerformanceDto> getApprovalPerformanceAnalysis() {
        log.debug("승인 처리 성능 분석");
        // 간단한 구현: 빈 리스트 반환
        return new ArrayList<>();
    }

    /**
     * 현재 관리자 조회 (임시 구현)
     * 실제로는 Spring Security Context에서 조회
     */
    private User getCurrentAdmin() {
        // 임시로 첫 번째 사용자 반환 (실제로는 SecurityContext에서 조회)
        return userRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("관리자를 찾을 수 없습니다"));
    }
}
