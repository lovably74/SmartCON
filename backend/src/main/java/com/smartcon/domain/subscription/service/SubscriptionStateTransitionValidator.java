package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 구독 상태 전환 검증 서비스
 * 
 * 비즈니스 규칙에 따른 상태 전환 매트릭스를 정의하고
 * 잘못된 상태 전환 시도를 차단합니다.
 */
@Component
public class SubscriptionStateTransitionValidator {
    
    /**
     * 상태 전환 매트릭스
     * 각 상태에서 전환 가능한 상태들을 정의
     */
    private static final Map<SubscriptionStatus, Set<SubscriptionStatus>> TRANSITION_MATRIX;
    
    static {
        TRANSITION_MATRIX = new EnumMap<>(SubscriptionStatus.class);
        
        // PENDING_APPROVAL에서 가능한 전환
        TRANSITION_MATRIX.put(SubscriptionStatus.PENDING_APPROVAL, EnumSet.of(
            SubscriptionStatus.ACTIVE,      // 수동 승인
            SubscriptionStatus.AUTO_APPROVED, // 자동 승인
            SubscriptionStatus.REJECTED     // 거부
        ));
        
        // REJECTED에서 가능한 전환
        TRANSITION_MATRIX.put(SubscriptionStatus.REJECTED, EnumSet.of(
            SubscriptionStatus.PENDING_APPROVAL // 재신청
        ));
        
        // AUTO_APPROVED에서 가능한 전환
        TRANSITION_MATRIX.put(SubscriptionStatus.AUTO_APPROVED, EnumSet.of(
            SubscriptionStatus.ACTIVE,      // 즉시 활성화
            SubscriptionStatus.SUSPENDED,   // 중지
            SubscriptionStatus.TERMINATED,  // 종료
            SubscriptionStatus.CANCELLED,   // 해지
            SubscriptionStatus.EXPIRED      // 만료
        ));
        
        // ACTIVE에서 가능한 전환
        TRANSITION_MATRIX.put(SubscriptionStatus.ACTIVE, EnumSet.of(
            SubscriptionStatus.SUSPENDED,   // 중지
            SubscriptionStatus.TERMINATED,  // 종료
            SubscriptionStatus.CANCELLED,   // 해지
            SubscriptionStatus.EXPIRED      // 만료
        ));
        
        // SUSPENDED에서 가능한 전환
        TRANSITION_MATRIX.put(SubscriptionStatus.SUSPENDED, EnumSet.of(
            SubscriptionStatus.ACTIVE,      // 재활성화
            SubscriptionStatus.TERMINATED,  // 종료
            SubscriptionStatus.CANCELLED,   // 해지
            SubscriptionStatus.EXPIRED      // 만료
        ));
        
        // CANCELLED에서 가능한 전환 (없음 - 최종 상태)
        TRANSITION_MATRIX.put(SubscriptionStatus.CANCELLED, EnumSet.noneOf(SubscriptionStatus.class));
        
        // EXPIRED에서 가능한 전환
        TRANSITION_MATRIX.put(SubscriptionStatus.EXPIRED, EnumSet.of(
            SubscriptionStatus.PENDING_APPROVAL // 재신청
        ));
        
        // TERMINATED에서 가능한 전환 (없음 - 최종 상태)
        TRANSITION_MATRIX.put(SubscriptionStatus.TERMINATED, EnumSet.noneOf(SubscriptionStatus.class));
        
        // TRIAL에서 가능한 전환
        TRANSITION_MATRIX.put(SubscriptionStatus.TRIAL, EnumSet.of(
            SubscriptionStatus.PENDING_APPROVAL, // 정식 구독 신청
            SubscriptionStatus.ACTIVE,           // 직접 활성화
            SubscriptionStatus.EXPIRED,          // 체험 만료
            SubscriptionStatus.CANCELLED         // 체험 취소
        ));
    }
    
    /**
     * 상태 전환 가능 여부 검증
     * 
     * @param fromStatus 현재 상태
     * @param toStatus 전환하려는 상태
     * @return 전환 가능 여부
     */
    public boolean isValidTransition(SubscriptionStatus fromStatus, SubscriptionStatus toStatus) {
        if (fromStatus == null || toStatus == null) {
            return false;
        }
        
        // 동일한 상태로의 전환은 허용하지 않음
        if (fromStatus == toStatus) {
            return false;
        }
        
        Set<SubscriptionStatus> allowedTransitions = TRANSITION_MATRIX.get(fromStatus);
        return allowedTransitions != null && allowedTransitions.contains(toStatus);
    }
    
    /**
     * 상태 전환 검증 및 예외 발생
     * 
     * @param fromStatus 현재 상태
     * @param toStatus 전환하려는 상태
     * @throws IllegalStateTransitionException 잘못된 상태 전환 시
     */
    public void validateTransition(SubscriptionStatus fromStatus, SubscriptionStatus toStatus) {
        if (!isValidTransition(fromStatus, toStatus)) {
            throw new IllegalStateTransitionException(
                String.format("잘못된 상태 전환입니다: %s -> %s", fromStatus, toStatus)
            );
        }
    }
    
    /**
     * 특정 상태에서 가능한 전환 상태 목록 조회
     * 
     * @param fromStatus 현재 상태
     * @return 가능한 전환 상태 목록
     */
    public Set<SubscriptionStatus> getAllowedTransitions(SubscriptionStatus fromStatus) {
        return TRANSITION_MATRIX.getOrDefault(fromStatus, EnumSet.noneOf(SubscriptionStatus.class));
    }
    
    /**
     * 최종 상태 여부 확인
     * 
     * @param status 확인할 상태
     * @return 최종 상태 여부
     */
    public boolean isFinalState(SubscriptionStatus status) {
        Set<SubscriptionStatus> allowedTransitions = TRANSITION_MATRIX.get(status);
        return allowedTransitions == null || allowedTransitions.isEmpty();
    }
}