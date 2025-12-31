package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.DataIntegrityReportDto;
import com.smartcon.domain.subscription.entity.Subscription;
import com.smartcon.domain.subscription.entity.SubscriptionApproval;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.subscription.entity.Notification;
import com.smartcon.domain.subscription.repository.SubscriptionRepository;
import com.smartcon.domain.subscription.repository.SubscriptionApprovalRepository;
import com.smartcon.domain.subscription.repository.NotificationRepository;
import com.smartcon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 데이터 무결성 검증 및 복구 서비스 구현체
 * 
 * 주기적 데이터 무결성 검사와 불일치 데이터 감지 및 복구 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DataIntegrityServiceImpl implements DataIntegrityService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionApprovalRepository subscriptionApprovalRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    private boolean scheduledCheckEnabled = true;
    
    /**
     * 전체 데이터 무결성 검사 실행
     */
    @Override
    public DataIntegrityReportDto performFullIntegrityCheck() {
        log.info("전체 데이터 무결성 검사 시작");
        long startTime = System.currentTimeMillis();
        
        // 각 영역별 검사 실행
        List<String> subscriptionIssues = checkSubscriptionIntegrity();
        List<String> approvalHistoryIssues = checkApprovalHistoryIntegrity();
        List<String> notificationIssues = checkNotificationIntegrity();
        
        // 자동 복구 실행
        int autoRecoveredCount = performAutoRecovery();
        
        // 통계 수집
        long totalSubscriptions = subscriptionRepository.count();
        long totalApprovals = subscriptionApprovalRepository.count();
        long totalNotifications = notificationRepository.count();
        
        long duration = System.currentTimeMillis() - startTime;
        
        DataIntegrityReportDto report = DataIntegrityReportDto.builder()
                .checkTime(LocalDateTime.now())
                .durationMs(duration)
                .totalSubscriptions(totalSubscriptions)
                .totalApprovals(totalApprovals)
                .totalNotifications(totalNotifications)
                .subscriptionIssues(subscriptionIssues)
                .approvalHistoryIssues(approvalHistoryIssues)
                .notificationIssues(notificationIssues)
                .autoRecoveredCount(autoRecoveredCount)
                .manualRecoveryRequiredCount(calculateManualRecoveryCount(subscriptionIssues, approvalHistoryIssues, notificationIssues))
                .build();
        
        log.info("전체 데이터 무결성 검사 완료 - 소요시간: {}ms, 총 문제: {}, 자동복구: {}", 
                duration, report.getTotalIssuesCount(), autoRecoveredCount);
        
        // 심각한 문제가 발견된 경우 관리자에게 알림
        if (report.getTotalIssuesCount() > 0) {
            sendIntegrityAlertToAdmins(report);
        }
        
        return report;
    }
    
    /**
     * 구독 데이터 무결성 검사
     */
    @Override
    public List<String> checkSubscriptionIntegrity() {
        log.debug("구독 데이터 무결성 검사 시작");
        List<String> issues = new ArrayList<>();
        
        // 1. 승인 관련 필드 일관성 검사
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        for (Subscription subscription : subscriptions) {
            // 승인된 구독인데 승인자나 승인일시가 없는 경우
            if (subscription.getStatus() == SubscriptionStatus.ACTIVE && 
                (subscription.getApprovedBy() == null || subscription.getApprovedAt() == null)) {
                issues.add(String.format("구독 ID %d: ACTIVE 상태이지만 승인 정보가 누락됨", subscription.getId()));
            }
            
            // 거부된 구독인데 거부 사유가 없는 경우
            if (subscription.getStatus() == SubscriptionStatus.REJECTED && 
                (subscription.getRejectionReason() == null || subscription.getRejectionReason().trim().isEmpty())) {
                issues.add(String.format("구독 ID %d: REJECTED 상태이지만 거부 사유가 누락됨", subscription.getId()));
            }
            
            // 중지된 구독인데 중지 사유가 없는 경우
            if (subscription.getStatus() == SubscriptionStatus.SUSPENDED && 
                (subscription.getSuspensionReason() == null || subscription.getSuspensionReason().trim().isEmpty())) {
                issues.add(String.format("구독 ID %d: SUSPENDED 상태이지만 중지 사유가 누락됨", subscription.getId()));
            }
            
            // 종료된 구독인데 종료 사유가 없는 경우
            if (subscription.getStatus() == SubscriptionStatus.TERMINATED && 
                (subscription.getTerminationReason() == null || subscription.getTerminationReason().trim().isEmpty())) {
                issues.add(String.format("구독 ID %d: TERMINATED 상태이지만 종료 사유가 누락됨", subscription.getId()));
            }
            
            // 승인 요청 시간이 생성 시간보다 이른 경우
            if (subscription.getApprovalRequestedAt() != null && 
                subscription.getCreatedAt() != null &&
                subscription.getApprovalRequestedAt().isBefore(subscription.getCreatedAt())) {
                issues.add(String.format("구독 ID %d: 승인 요청 시간이 생성 시간보다 이름", subscription.getId()));
            }
        }
        
        log.debug("구독 데이터 무결성 검사 완료 - 발견된 문제: {}", issues.size());
        return issues;
    }
    
    /**
     * 승인 이력 데이터 무결성 검사
     */
    @Override
    public List<String> checkApprovalHistoryIntegrity() {
        log.debug("승인 이력 데이터 무결성 검사 시작");
        List<String> issues = new ArrayList<>();
        
        List<SubscriptionApproval> approvals = subscriptionApprovalRepository.findAll();
        for (SubscriptionApproval approval : approvals) {
            // 존재하지 않는 구독을 참조하는 경우
            if (!subscriptionRepository.existsById(approval.getSubscriptionId())) {
                issues.add(String.format("승인 이력 ID %d: 존재하지 않는 구독 ID %d를 참조", 
                        approval.getId(), approval.getSubscriptionId()));
            }
            
            // 존재하지 않는 관리자를 참조하는 경우
            if (approval.getAdmin() != null && !userRepository.existsById(approval.getAdmin().getId())) {
                issues.add(String.format("승인 이력 ID %d: 존재하지 않는 관리자 ID %d를 참조", 
                        approval.getId(), approval.getAdmin().getId()));
            }
            
            // 상태 전환이 논리적으로 맞지 않는 경우
            if (approval.getFromStatus() == approval.getToStatus()) {
                issues.add(String.format("승인 이력 ID %d: 동일한 상태로의 전환 (%s -> %s)", 
                        approval.getId(), approval.getFromStatus(), approval.getToStatus()));
            }
            
            // 처리 시간이 누락된 경우
            if (approval.getProcessedAt() == null) {
                issues.add(String.format("승인 이력 ID %d: 처리 시간이 누락됨", approval.getId()));
            }
        }
        
        log.debug("승인 이력 데이터 무결성 검사 완료 - 발견된 문제: {}", issues.size());
        return issues;
    }
    
    /**
     * 알림 데이터 무결성 검사
     */
    @Override
    public List<String> checkNotificationIntegrity() {
        log.debug("알림 데이터 무결성 검사 시작");
        List<String> issues = new ArrayList<>();
        
        List<Notification> notifications = notificationRepository.findAll();
        for (Notification notification : notifications) {
            // 존재하지 않는 수신자를 참조하는 경우
            if (!userRepository.existsById(notification.getRecipientId())) {
                issues.add(String.format("알림 ID %d: 존재하지 않는 수신자 ID %d를 참조", 
                        notification.getId(), notification.getRecipientId()));
            }
            
            // 읽음 처리되었는데 읽은 시간이 없는 경우
            if (notification.getIsRead() && notification.getReadAt() == null) {
                issues.add(String.format("알림 ID %d: 읽음 처리되었지만 읽은 시간이 누락됨", notification.getId()));
            }
            
            // 읽지 않았는데 읽은 시간이 있는 경우
            if (!notification.getIsRead() && notification.getReadAt() != null) {
                issues.add(String.format("알림 ID %d: 읽지 않았지만 읽은 시간이 설정됨", notification.getId()));
            }
            
            // 관련 엔티티가 존재하지 않는 경우
            if ("subscription".equals(notification.getRelatedEntityType()) && 
                notification.getRelatedEntityId() != null &&
                !subscriptionRepository.existsById(notification.getRelatedEntityId())) {
                issues.add(String.format("알림 ID %d: 존재하지 않는 구독 ID %d를 참조", 
                        notification.getId(), notification.getRelatedEntityId()));
            }
        }
        
        log.debug("알림 데이터 무결성 검사 완료 - 발견된 문제: {}", issues.size());
        return issues;
    }
    
    /**
     * 자동 복구 가능한 문제들을 복구
     */
    @Override
    @Transactional
    public int performAutoRecovery() {
        log.info("자동 복구 시작");
        int recoveredCount = 0;
        
        // 1. 읽음 상태와 읽은 시간 불일치 복구
        recoveredCount += fixNotificationReadStatus();
        
        // 2. 고아 데이터 정리
        recoveredCount += cleanupOrphanedData();
        
        // 3. 누락된 승인 요청 시간 복구
        recoveredCount += fixMissingApprovalRequestTime();
        
        log.info("자동 복구 완료 - 복구된 문제: {}", recoveredCount);
        return recoveredCount;
    }
    
    /**
     * 특정 구독의 데이터 일관성 복구
     */
    @Override
    @Transactional
    public boolean recoverSubscriptionData(Long subscriptionId) {
        log.info("구독 데이터 복구 시작 - subscriptionId: {}", subscriptionId);
        
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findById(subscriptionId);
        if (subscriptionOpt.isEmpty()) {
            log.warn("존재하지 않는 구독 ID: {}", subscriptionId);
            return false;
        }
        
        Subscription subscription = subscriptionOpt.get();
        boolean recovered = false;
        
        // 승인 요청 시간이 없는 경우 생성 시간으로 설정
        if (subscription.getApprovalRequestedAt() == null) {
            subscription.updateApprovalRequestedAt(subscription.getCreatedAt());
            recovered = true;
        }
        
        if (recovered) {
            subscriptionRepository.save(subscription);
            log.info("구독 데이터 복구 완료 - subscriptionId: {}", subscriptionId);
        }
        
        return recovered;
    }
    
    /**
     * 고아 데이터 정리
     */
    @Override
    @Transactional
    public int cleanupOrphanedData() {
        log.info("고아 데이터 정리 시작");
        int cleanedCount = 0;
        
        // 존재하지 않는 구독을 참조하는 승인 이력 삭제
        List<SubscriptionApproval> orphanedApprovals = subscriptionApprovalRepository.findAll()
                .stream()
                .filter(approval -> !subscriptionRepository.existsById(approval.getSubscriptionId()))
                .toList();
        
        for (SubscriptionApproval approval : orphanedApprovals) {
            subscriptionApprovalRepository.delete(approval);
            cleanedCount++;
            log.debug("고아 승인 이력 삭제 - approvalId: {}, subscriptionId: {}", 
                    approval.getId(), approval.getSubscriptionId());
        }
        
        // 존재하지 않는 사용자를 참조하는 알림 삭제
        List<Notification> orphanedNotifications = notificationRepository.findAll()
                .stream()
                .filter(notification -> !userRepository.existsById(notification.getRecipientId()))
                .toList();
        
        for (Notification notification : orphanedNotifications) {
            notificationRepository.delete(notification);
            cleanedCount++;
            log.debug("고아 알림 삭제 - notificationId: {}, recipientId: {}", 
                    notification.getId(), notification.getRecipientId());
        }
        
        log.info("고아 데이터 정리 완료 - 정리된 레코드: {}", cleanedCount);
        return cleanedCount;
    }
    
    /**
     * 데이터 무결성 검사 스케줄링 설정
     */
    @Override
    public void setScheduledCheckEnabled(boolean enabled) {
        this.scheduledCheckEnabled = enabled;
        log.info("데이터 무결성 검사 스케줄링 설정 변경: {}", enabled);
    }
    
    /**
     * 주기적 데이터 무결성 검사 (매일 새벽 2시)
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledIntegrityCheck() {
        if (!scheduledCheckEnabled) {
            log.debug("스케줄된 데이터 무결성 검사가 비활성화됨");
            return;
        }
        
        log.info("스케줄된 데이터 무결성 검사 시작");
        try {
            DataIntegrityReportDto report = performFullIntegrityCheck();
            log.info("스케줄된 데이터 무결성 검사 완료 - 문제: {}, 자동복구: {}", 
                    report.getTotalIssuesCount(), report.getAutoRecoveredCount());
        } catch (Exception e) {
            log.error("스케줄된 데이터 무결성 검사 중 오류 발생", e);
        }
    }
    
    /**
     * 알림 읽음 상태 불일치 복구
     */
    private int fixNotificationReadStatus() {
        int fixedCount = 0;
        
        // 읽음 처리되었는데 읽은 시간이 없는 알림들을 현재 시간으로 설정
        List<Notification> inconsistentNotifications = notificationRepository.findAll()
                .stream()
                .filter(n -> n.getIsRead() && n.getReadAt() == null)
                .toList();
        
        for (Notification notification : inconsistentNotifications) {
            notification.markAsRead();
            notificationRepository.save(notification);
            fixedCount++;
        }
        
        // 읽지 않았는데 읽은 시간이 있는 알림들의 읽은 시간 제거
        List<Notification> anotherInconsistentNotifications = notificationRepository.findAll()
                .stream()
                .filter(n -> !n.getIsRead() && n.getReadAt() != null)
                .toList();
        
        for (Notification notification : anotherInconsistentNotifications) {
            notification.markAsUnread();
            notificationRepository.save(notification);
            fixedCount++;
        }
        
        return fixedCount;
    }
    
    /**
     * 누락된 승인 요청 시간 복구
     */
    private int fixMissingApprovalRequestTime() {
        int fixedCount = 0;
        
        List<Subscription> subscriptionsWithoutRequestTime = subscriptionRepository.findAll()
                .stream()
                .filter(s -> s.getApprovalRequestedAt() == null)
                .toList();
        
        for (Subscription subscription : subscriptionsWithoutRequestTime) {
            subscription.updateApprovalRequestedAt(subscription.getCreatedAt());
            subscriptionRepository.save(subscription);
            fixedCount++;
        }
        
        return fixedCount;
    }
    
    /**
     * 수동 복구가 필요한 문제 수 계산
     */
    private int calculateManualRecoveryCount(List<String> subscriptionIssues, 
                                           List<String> approvalHistoryIssues, 
                                           List<String> notificationIssues) {
        // 자동 복구 불가능한 심각한 문제들만 카운트
        int manualCount = 0;
        
        // 승인 정보 누락 등 심각한 문제들
        manualCount += (int) subscriptionIssues.stream()
                .filter(issue -> issue.contains("승인 정보가 누락") || issue.contains("사유가 누락"))
                .count();
        
        // 존재하지 않는 참조 등
        manualCount += (int) approvalHistoryIssues.stream()
                .filter(issue -> issue.contains("존재하지 않는"))
                .count();
        
        return manualCount;
    }
    
    /**
     * 관리자에게 무결성 검사 결과 알림 발송
     */
    private void sendIntegrityAlertToAdmins(DataIntegrityReportDto report) {
        try {
            // 슈퍼관리자들에게 알림 발송
            // TODO: 실제 구현에서는 슈퍼관리자 목록을 조회하여 알림 발송
            log.warn("데이터 무결성 문제 발견 - 총 {}개 문제, 자동복구 {}개", 
                    report.getTotalIssuesCount(), report.getAutoRecoveredCount());
        } catch (Exception e) {
            log.error("무결성 검사 결과 알림 발송 중 오류", e);
        }
    }
}