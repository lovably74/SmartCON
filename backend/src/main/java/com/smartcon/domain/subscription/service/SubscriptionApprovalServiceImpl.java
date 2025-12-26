package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.CreateSubscriptionRequest;
import com.smartcon.domain.subscription.dto.SubscriptionApprovalDto;
import com.smartcon.domain.subscription.dto.SubscriptionDto;
import com.smartcon.domain.subscription.entity.*;
import com.smartcon.domain.subscription.repository.SubscriptionApprovalRepository;
import com.smartcon.domain.subscription.repository.SubscriptionRepository;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 구독 승인 서비스 구현체
 * 
 * 구독 신청의 승인, 거부, 중지, 종료, 재활성화 등의 기능을 제공합니다.
 * 상태 전환 검증 로직과 트랜잭션 관리, 동시성 제어를 포함합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SubscriptionApprovalServiceImpl implements SubscriptionApprovalService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionApprovalRepository subscriptionApprovalRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AutoApprovalRuleService autoApprovalRuleService;
    
    /**
     * 구독 승인
     */
    @Override
    @Transactional
    public SubscriptionDto approveSubscription(Long subscriptionId, String reason) {
        log.info("구독 승인 처리 시작 - subscriptionId: {}, reason: {}", subscriptionId, reason);
        
        Subscription subscription = getSubscription(subscriptionId);
        User currentAdmin = getCurrentAdmin();
        
        // 상태 전환 검증
        validateStateTransition(subscription.getStatus(), SubscriptionStatus.ACTIVE, "승인");
        
        SubscriptionStatus fromStatus = subscription.getStatus();
        
        // 구독 승인 처리
        subscription.approve(currentAdmin);
        subscription = subscriptionRepository.save(subscription);
        
        // 승인 이력 기록
        recordApprovalHistory(subscriptionId, currentAdmin, fromStatus, 
                             SubscriptionStatus.ACTIVE, reason, ApprovalAction.APPROVE);
        
        // 승인 결과 알림 발송
        notificationService.sendApprovalResultNotification(subscriptionId, true, reason);
        
        log.info("구독 승인 완료 - subscriptionId: {}", subscriptionId);
        return SubscriptionDto.from(subscription);
    }
    
    /**
     * 구독 거부
     */
    @Override
    @Transactional
    public SubscriptionDto rejectSubscription(Long subscriptionId, String reason) {
        log.info("구독 거부 처리 시작 - subscriptionId: {}, reason: {}", subscriptionId, reason);
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("거부 사유는 필수입니다");
        }
        
        Subscription subscription = getSubscription(subscriptionId);
        User currentAdmin = getCurrentAdmin();
        
        // 상태 전환 검증
        validateStateTransition(subscription.getStatus(), SubscriptionStatus.REJECTED, "거부");
        
        SubscriptionStatus fromStatus = subscription.getStatus();
        
        // 구독 거부 처리
        subscription.reject(reason);
        subscription = subscriptionRepository.save(subscription);
        
        // 승인 이력 기록
        recordApprovalHistory(subscriptionId, currentAdmin, fromStatus, 
                             SubscriptionStatus.REJECTED, reason, ApprovalAction.REJECT);
        
        // 거부 결과 알림 발송
        notificationService.sendApprovalResultNotification(subscriptionId, false, reason);
        
        log.info("구독 거부 완료 - subscriptionId: {}", subscriptionId);
        return SubscriptionDto.from(subscription);
    }
    
    /**
     * 구독 중지
     */
    @Override
    @Transactional
    public SubscriptionDto suspendSubscription(Long subscriptionId, String reason) {
        log.info("구독 중지 처리 시작 - subscriptionId: {}, reason: {}", subscriptionId, reason);
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("중지 사유는 필수입니다");
        }
        
        Subscription subscription = getSubscription(subscriptionId);
        User currentAdmin = getCurrentAdmin();
        
        // 상태 전환 검증
        validateStateTransition(subscription.getStatus(), SubscriptionStatus.SUSPENDED, "중지");
        
        SubscriptionStatus fromStatus = subscription.getStatus();
        
        // 구독 중지 처리
        subscription.suspend(reason);
        subscription = subscriptionRepository.save(subscription);
        
        // 승인 이력 기록
        recordApprovalHistory(subscriptionId, currentAdmin, fromStatus, 
                             SubscriptionStatus.SUSPENDED, reason, ApprovalAction.SUSPEND);
        
        // 중지 알림 발송
        notificationService.sendSubscriptionSuspendedNotification(subscriptionId, reason);
        
        log.info("구독 중지 완료 - subscriptionId: {}", subscriptionId);
        return SubscriptionDto.from(subscription);
    }
    
    /**
     * 구독 종료
     */
    @Override
    @Transactional
    public SubscriptionDto terminateSubscription(Long subscriptionId, String reason) {
        log.info("구독 종료 처리 시작 - subscriptionId: {}, reason: {}", subscriptionId, reason);
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("종료 사유는 필수입니다");
        }
        
        Subscription subscription = getSubscription(subscriptionId);
        User currentAdmin = getCurrentAdmin();
        
        // 상태 전환 검증
        validateStateTransition(subscription.getStatus(), SubscriptionStatus.TERMINATED, "종료");
        
        SubscriptionStatus fromStatus = subscription.getStatus();
        
        // 구독 종료 처리
        subscription.terminate(reason);
        subscription = subscriptionRepository.save(subscription);
        
        // 승인 이력 기록
        recordApprovalHistory(subscriptionId, currentAdmin, fromStatus, 
                             SubscriptionStatus.TERMINATED, reason, ApprovalAction.TERMINATE);
        
        // 종료 알림 발송
        notificationService.sendSubscriptionTerminatedNotification(subscriptionId, reason);
        
        log.info("구독 종료 완료 - subscriptionId: {}", subscriptionId);
        return SubscriptionDto.from(subscription);
    }
    
    /**
     * 구독 재활성화
     */
    @Override
    @Transactional
    public SubscriptionDto reactivateSubscription(Long subscriptionId, String reason) {
        log.info("구독 재활성화 처리 시작 - subscriptionId: {}, reason: {}", subscriptionId, reason);
        
        Subscription subscription = getSubscription(subscriptionId);
        User currentAdmin = getCurrentAdmin();
        
        // 중지된 구독만 재활성화 가능
        if (subscription.getStatus() != SubscriptionStatus.SUSPENDED) {
            throw new IllegalStateException("중지된 구독만 재활성화할 수 있습니다");
        }
        
        SubscriptionStatus fromStatus = subscription.getStatus();
        
        // 구독 재활성화 처리
        subscription.resume();
        subscription = subscriptionRepository.save(subscription);
        
        // 승인 이력 기록
        recordApprovalHistory(subscriptionId, currentAdmin, fromStatus, 
                             SubscriptionStatus.ACTIVE, reason, ApprovalAction.REACTIVATE);
        
        // 재활성화 알림 발송
        notificationService.sendSubscriptionReactivatedNotification(subscriptionId);
        
        log.info("구독 재활성화 완료 - subscriptionId: {}", subscriptionId);
        return SubscriptionDto.from(subscription);
    }
    
    /**
     * 승인 대기 중인 구독 목록 조회
     */
    @Override
    public Page<SubscriptionDto> getPendingApprovals(Pageable pageable) {
        log.info("승인 대기 구독 목록 조회 - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        return subscriptionRepository.findByStatusOrderByCreatedAtAsc(SubscriptionStatus.PENDING_APPROVAL, pageable)
                .map(SubscriptionDto::from);
    }
    
    /**
     * 구독 승인 이력 조회
     */
    @Override
    public List<SubscriptionApprovalDto> getApprovalHistory(Long subscriptionId) {
        log.info("구독 승인 이력 조회 - subscriptionId: {}", subscriptionId);
        
        return subscriptionApprovalRepository.findBySubscriptionIdOrderByProcessedAtDesc(subscriptionId)
                .stream()
                .map(SubscriptionApprovalDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 자동 승인 여부 확인
     */
    @Override
    public boolean checkAutoApproval(CreateSubscriptionRequest request) {
        log.info("자동 승인 여부 확인 - planId: {}", request.getPlanId());
        
        return autoApprovalRuleService.evaluateAutoApproval(request);
    }
    
    /**
     * 자동 승인 처리
     */
    @Override
    @Transactional
    public SubscriptionDto processAutoApproval(CreateSubscriptionRequest request) {
        log.info("자동 승인 처리 시작 - planId: {}", request.getPlanId());
        
        // 자동 승인 가능 여부 재확인
        if (!checkAutoApproval(request)) {
            throw new IllegalStateException("자동 승인 조건을 만족하지 않습니다");
        }
        
        // TODO: 실제 구독 생성 로직은 SubscriptionService에서 처리
        // 여기서는 자동 승인 로직만 처리
        
        log.info("자동 승인 처리 완료 - planId: {}", request.getPlanId());
        return null; // 임시로 null 반환, 실제 구현에서는 구독 정보 반환
    }
    
    /**
     * 구독 조회
     */
    private Subscription getSubscription(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독입니다: " + subscriptionId));
    }
    
    /**
     * 현재 관리자 조회
     * TODO: 실제 인증 시스템 구현 후 SecurityContext에서 가져오도록 수정
     */
    private User getCurrentAdmin() {
        // 개발 단계에서는 임시로 첫 번째 사용자를 반환
        return userRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("관리자 정보를 찾을 수 없습니다"));
    }
    
    /**
     * 상태 전환 검증
     */
    private void validateStateTransition(SubscriptionStatus fromStatus, SubscriptionStatus toStatus, String action) {
        log.debug("상태 전환 검증 - from: {}, to: {}, action: {}", fromStatus, toStatus, action);
        
        switch (toStatus) {
            case ACTIVE:
                if (fromStatus != SubscriptionStatus.PENDING_APPROVAL && 
                    fromStatus != SubscriptionStatus.SUSPENDED) {
                    throw new IllegalStateException(
                        String.format("%s 상태에서는 %s할 수 없습니다", fromStatus, action));
                }
                break;
                
            case REJECTED:
                if (fromStatus != SubscriptionStatus.PENDING_APPROVAL) {
                    throw new IllegalStateException(
                        String.format("%s 상태에서는 %s할 수 없습니다", fromStatus, action));
                }
                break;
                
            case SUSPENDED:
                if (fromStatus != SubscriptionStatus.ACTIVE && 
                    fromStatus != SubscriptionStatus.AUTO_APPROVED) {
                    throw new IllegalStateException(
                        String.format("%s 상태에서는 %s할 수 없습니다", fromStatus, action));
                }
                break;
                
            case TERMINATED:
                if (fromStatus == SubscriptionStatus.TERMINATED) {
                    throw new IllegalStateException("이미 종료된 구독입니다");
                }
                break;
                
            default:
                throw new IllegalArgumentException("지원하지 않는 상태 전환입니다: " + toStatus);
        }
    }
    
    /**
     * 승인 이력 기록
     */
    private void recordApprovalHistory(Long subscriptionId, User admin, SubscriptionStatus fromStatus,
                                     SubscriptionStatus toStatus, String reason, ApprovalAction action) {
        log.debug("승인 이력 기록 - subscriptionId: {}, action: {}", subscriptionId, action);
        
        SubscriptionApproval approval = SubscriptionApproval.builder()
                .subscriptionId(subscriptionId)
                .admin(admin)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .reason(reason)
                .action(action)
                .processedAt(LocalDateTime.now())
                .build();
        
        subscriptionApprovalRepository.save(approval);
    }
}