package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.entity.Subscription;
import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import com.smartcon.domain.subscription.repository.SubscriptionRepository;
import com.smartcon.domain.tenant.entity.Tenant;
import com.smartcon.domain.tenant.repository.TenantRepository;
import com.smartcon.global.security.SubscriptionAccessResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 구독 상태별 접근 제어 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SubscriptionAccessControlServiceImpl implements SubscriptionAccessControlService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final TenantRepository tenantRepository;
    
    @Override
    public SubscriptionAccessResult checkAccess(Long tenantId, String requestPath) {
        log.debug("구독 접근 제어 확인 - 테넌트: {}, 경로: {}", tenantId, requestPath);
        
        // 특정 경로는 구독 상태와 관계없이 접근 허용
        if (isExemptPath(requestPath)) {
            return SubscriptionAccessResult.allowed();
        }
        
        // 테넌트 조회
        Optional<Tenant> tenantOpt = tenantRepository.findById(tenantId);
        if (tenantOpt.isEmpty()) {
            log.warn("존재하지 않는 테넌트: {}", tenantId);
            return SubscriptionAccessResult.denied(
                null, 
                "존재하지 않는 테넌트입니다.", 
                "/error"
            );
        }
        
        Tenant tenant = tenantOpt.get();
        
        // 현재 구독 조회 (모든 상태)
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findCurrentByTenant(tenant);
        
        // 구독이 없는 경우
        if (subscriptionOpt.isEmpty()) {
            log.debug("구독이 없음 - 테넌트: {}", tenantId);
            return SubscriptionAccessResult.denied(
                null,
                "활성 구독이 없습니다. 구독을 신청해주세요.",
                "/subscription/plans"
            );
        }
        
        Subscription subscription = subscriptionOpt.get();
        SubscriptionStatus status = subscription.getStatus();
        
        log.debug("구독 상태 확인 - 테넌트: {}, 상태: {}", tenantId, status);
        
        // 상태별 접근 제어
        switch (status) {
            case ACTIVE:
            case TRIAL:
            case AUTO_APPROVED:
                // 정상 서비스 이용 가능
                return SubscriptionAccessResult.allowedWithStatus(status);
                
            case PENDING_APPROVAL:
                // 승인 대기 중 - 서비스 접근 차단
                return SubscriptionAccessResult.denied(
                    status,
                    "구독 승인이 진행 중입니다. 승인 완료까지 잠시만 기다려주세요.",
                    "/subscription/pending"
                );
                
            case REJECTED:
                // 승인 거부 - 재신청 안내
                String rejectionReason = subscription.getRejectionReason();
                String rejectionMessage = rejectionReason != null ? 
                    String.format("구독 신청이 거부되었습니다. 사유: %s", rejectionReason) :
                    "구독 신청이 거부되었습니다. 다시 신청하거나 고객센터에 문의해주세요.";
                    
                return SubscriptionAccessResult.denied(
                    status,
                    rejectionMessage,
                    "/subscription/rejected"
                );
                
            case SUSPENDED:
                // 일시 중지 - 재활성화 안내
                String suspensionReason = subscription.getSuspensionReason();
                String suspensionMessage = suspensionReason != null ?
                    String.format("구독이 일시 중지되었습니다. 사유: %s 고객센터에 문의해주세요.", suspensionReason) :
                    "구독이 일시 중지되었습니다. 고객센터에 문의해주세요.";
                    
                return SubscriptionAccessResult.denied(
                    status,
                    suspensionMessage,
                    "/subscription/suspended"
                );
                
            case TERMINATED:
                // 종료됨 - 새 구독 신청 안내
                String terminationReason = subscription.getTerminationReason();
                String terminationMessage = terminationReason != null ?
                    String.format("구독이 종료되었습니다. 사유: %s 새로운 구독을 신청해주세요.", terminationReason) :
                    "구독이 종료되었습니다. 새로운 구독을 신청해주세요.";
                    
                return SubscriptionAccessResult.denied(
                    status,
                    terminationMessage,
                    "/subscription/terminated"
                );
                
            case CANCELLED:
                // 해지됨 - 새 구독 신청 안내
                return SubscriptionAccessResult.denied(
                    status,
                    "구독이 해지되었습니다. 새로운 구독을 신청해주세요.",
                    "/subscription/cancelled"
                );
                
            case EXPIRED:
                // 만료됨 - 갱신 안내
                return SubscriptionAccessResult.denied(
                    status,
                    "구독이 만료되었습니다. 구독을 갱신해주세요.",
                    "/subscription/expired"
                );
                
            default:
                log.warn("알 수 없는 구독 상태: {} - 테넌트: {}", status, tenantId);
                return SubscriptionAccessResult.denied(
                    status,
                    "구독 상태를 확인할 수 없습니다. 고객센터에 문의해주세요.",
                    "/error"
                );
        }
    }
    
    /**
     * 구독 상태와 관계없이 접근이 허용되는 경로인지 확인
     */
    private boolean isExemptPath(String requestPath) {
        if (requestPath == null) {
            return false;
        }
        
        // 인증 관련 API
        if (requestPath.startsWith("/api/v1/auth/") || 
            requestPath.startsWith("/auth/") ||
            requestPath.equals("/api/v1/auth/login") ||
            requestPath.equals("/api/v1/auth/logout") ||
            requestPath.equals("/api/v1/auth/refresh")) {
            return true;
        }
        
        // 슈퍼 관리자 API
        if (requestPath.startsWith("/api/v1/super/") || 
            requestPath.startsWith("/super/")) {
            return true;
        }
        
        // 공개 API (헬스체크, 정적 리소스 등)
        if (requestPath.startsWith("/health") ||
            requestPath.startsWith("/actuator") ||
            requestPath.startsWith("/static/") ||
            requestPath.startsWith("/public/")) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean isServiceAccessible(Long tenantId) {
        SubscriptionAccessResult result = checkAccess(tenantId, "/api/service");
        return result.isAllowed();
    }
    
    @Override
    public String getStatusMessage(Long tenantId) {
        SubscriptionAccessResult result = checkAccess(tenantId, "/api/service");
        
        if (result.isAllowed()) {
            return "정상적으로 서비스를 이용하실 수 있습니다.";
        } else {
            return result.getMessage();
        }
    }
}