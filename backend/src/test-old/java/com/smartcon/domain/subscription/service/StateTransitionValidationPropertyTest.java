package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.entity.SubscriptionStatus;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * 구독 상태 전환 검증 속성 기반 테스트
 * 
 * Property 31: State Transition Validation을 검증합니다.
 * Requirements 8.3: 비즈니스 규칙에 따른 상태 전환 검증
 */
class StateTransitionValidationPropertyTest {
    
    private final SubscriptionStateTransitionValidator validator = new SubscriptionStateTransitionValidator();
    
    /**
     * Property 31: State Transition Validation
     * 모든 유효한 상태 전환은 허용되고, 무효한 상태 전환은 거부되어야 함
     * **Validates: Requirements 8.3**
     */
    @Property(tries = 100)
    @Label("유효한 상태 전환은 허용되고 무효한 전환은 거부되어야 함")
    void validTransitionsAreAllowedAndInvalidTransitionsAreRejected(
            @ForAll("subscriptionStatuses") SubscriptionStatus fromStatus,
            @ForAll("subscriptionStatuses") SubscriptionStatus toStatus) {
        
        // Given: 임의의 두 구독 상태
        
        // When: 상태 전환 가능 여부를 확인
        boolean isValidTransition = validator.isValidTransition(fromStatus, toStatus);
        Set<SubscriptionStatus> allowedTransitions = validator.getAllowedTransitions(fromStatus);
        
        // Then: 허용된 전환 목록과 검증 결과가 일치해야 함
        if (fromStatus == toStatus) {
            // 동일한 상태로의 전환은 허용하지 않음
            assertThat(isValidTransition).isFalse();
        } else if (allowedTransitions.contains(toStatus)) {
            // 허용된 전환 목록에 있으면 유효해야 함
            assertThat(isValidTransition).isTrue();
        } else {
            // 허용된 전환 목록에 없으면 무효해야 함
            assertThat(isValidTransition).isFalse();
        }
    }
    
    /**
     * 유효한 상태 전환에 대해서는 예외가 발생하지 않아야 함
     */
    @Property(tries = 100)
    @Label("유효한 상태 전환에 대해서는 예외가 발생하지 않아야 함")
    void validTransitionsShouldNotThrowException(
            @ForAll("validTransitionPairs") TransitionPair transitionPair) {
        
        // Given: 유효한 상태 전환 쌍
        SubscriptionStatus fromStatus = transitionPair.fromStatus;
        SubscriptionStatus toStatus = transitionPair.toStatus;
        
        // When & Then: 유효한 전환에 대해서는 예외가 발생하지 않아야 함
        assertThatCode(() -> validator.validateTransition(fromStatus, toStatus))
                .doesNotThrowAnyException();
    }
    
    /**
     * 무효한 상태 전환에 대해서는 예외가 발생해야 함
     */
    @Property(tries = 100)
    @Label("무효한 상태 전환에 대해서는 예외가 발생해야 함")
    void invalidTransitionsShouldThrowException(
            @ForAll("invalidTransitionPairs") TransitionPair transitionPair) {
        
        // Given: 무효한 상태 전환 쌍
        SubscriptionStatus fromStatus = transitionPair.fromStatus;
        SubscriptionStatus toStatus = transitionPair.toStatus;
        
        // When & Then: 무효한 전환에 대해서는 예외가 발생해야 함
        assertThatThrownBy(() -> validator.validateTransition(fromStatus, toStatus))
                .isInstanceOf(IllegalStateTransitionException.class)
                .hasMessageContaining("잘못된 상태 전환입니다");
    }
    
    /**
     * 최종 상태에서는 다른 상태로 전환할 수 없어야 함
     */
    @Property(tries = 50)
    @Label("최종 상태에서는 다른 상태로 전환할 수 없어야 함")
    void finalStatesShouldNotAllowTransitions(
            @ForAll("finalStates") SubscriptionStatus finalState,
            @ForAll("subscriptionStatuses") SubscriptionStatus toStatus) {
        
        // Given: 최종 상태와 임의의 대상 상태
        Assume.that(finalState != toStatus); // 동일한 상태는 제외
        
        // When: 최종 상태에서 다른 상태로의 전환 시도
        boolean isValidTransition = validator.isValidTransition(finalState, toStatus);
        
        // Then: 전환이 허용되지 않아야 함
        assertThat(isValidTransition).isFalse();
    }
    
    /**
     * 허용된 전환 목록은 비어있지 않거나 최종 상태여야 함
     */
    @Property(tries = 50)
    @Label("허용된 전환 목록은 비어있지 않거나 최종 상태여야 함")
    void allowedTransitionsConsistencyCheck(@ForAll("subscriptionStatuses") SubscriptionStatus status) {
        
        // Given: 임의의 구독 상태
        
        // When: 허용된 전환 목록과 최종 상태 여부를 확인
        Set<SubscriptionStatus> allowedTransitions = validator.getAllowedTransitions(status);
        boolean isFinalState = validator.isFinalState(status);
        
        // Then: 최종 상태이면 허용된 전환이 없고, 그렇지 않으면 허용된 전환이 있어야 함
        if (isFinalState) {
            assertThat(allowedTransitions).isEmpty();
        } else {
            assertThat(allowedTransitions).isNotEmpty();
        }
    }
    
    /**
     * 구독 상태 생성기
     */
    @Provide
    Arbitrary<SubscriptionStatus> subscriptionStatuses() {
        return Arbitraries.of(SubscriptionStatus.values());
    }
    
    /**
     * 최종 상태 생성기
     */
    @Provide
    Arbitrary<SubscriptionStatus> finalStates() {
        return Arbitraries.of(SubscriptionStatus.values())
                .filter(status -> validator.isFinalState(status));
    }
    
    /**
     * 유효한 상태 전환 쌍 생성기
     */
    @Provide
    Arbitrary<TransitionPair> validTransitionPairs() {
        return Arbitraries.of(SubscriptionStatus.values())
                .flatMap(fromStatus -> {
                    Set<SubscriptionStatus> allowedTransitions = validator.getAllowedTransitions(fromStatus);
                    if (allowedTransitions.isEmpty()) {
                        return Arbitraries.just(new TransitionPair(fromStatus, fromStatus)); // 더미 값
                    }
                    return Arbitraries.of(allowedTransitions.toArray(new SubscriptionStatus[0]))
                            .map(toStatus -> new TransitionPair(fromStatus, toStatus));
                })
                .filter(pair -> validator.isValidTransition(pair.fromStatus, pair.toStatus));
    }
    
    /**
     * 무효한 상태 전환 쌍 생성기
     */
    @Provide
    Arbitrary<TransitionPair> invalidTransitionPairs() {
        return Arbitraries.of(SubscriptionStatus.values())
                .flatMap(fromStatus -> 
                    Arbitraries.of(SubscriptionStatus.values())
                            .map(toStatus -> new TransitionPair(fromStatus, toStatus))
                )
                .filter(pair -> !validator.isValidTransition(pair.fromStatus, pair.toStatus));
    }
    
    /**
     * 상태 전환 쌍을 나타내는 헬퍼 클래스
     */
    private static class TransitionPair {
        final SubscriptionStatus fromStatus;
        final SubscriptionStatus toStatus;
        
        TransitionPair(SubscriptionStatus fromStatus, SubscriptionStatus toStatus) {
            this.fromStatus = fromStatus;
            this.toStatus = toStatus;
        }
        
        @Override
        public String toString() {
            return String.format("%s -> %s", fromStatus, toStatus);
        }
    }
}