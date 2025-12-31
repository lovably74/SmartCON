package com.smartcon.domain.subscription.service;

import com.smartcon.domain.subscription.dto.DataIntegrityReportDto;

import java.util.List;

/**
 * 데이터 무결성 검증 및 복구 서비스 인터페이스
 * 
 * 주기적 데이터 무결성 검사와 불일치 데이터 감지 및 복구 기능을 제공합니다.
 */
public interface DataIntegrityService {
    
    /**
     * 전체 데이터 무결성 검사 실행
     * 
     * @return 검사 결과 보고서
     */
    DataIntegrityReportDto performFullIntegrityCheck();
    
    /**
     * 구독 데이터 무결성 검사
     * 
     * @return 발견된 문제 목록
     */
    List<String> checkSubscriptionIntegrity();
    
    /**
     * 승인 이력 데이터 무결성 검사
     * 
     * @return 발견된 문제 목록
     */
    List<String> checkApprovalHistoryIntegrity();
    
    /**
     * 알림 데이터 무결성 검사
     * 
     * @return 발견된 문제 목록
     */
    List<String> checkNotificationIntegrity();
    
    /**
     * 자동 복구 가능한 문제들을 복구
     * 
     * @return 복구된 문제 개수
     */
    int performAutoRecovery();
    
    /**
     * 특정 구독의 데이터 일관성 복구
     * 
     * @param subscriptionId 구독 ID
     * @return 복구 성공 여부
     */
    boolean recoverSubscriptionData(Long subscriptionId);
    
    /**
     * 고아 데이터 정리
     * 
     * @return 정리된 레코드 수
     */
    int cleanupOrphanedData();
    
    /**
     * 데이터 무결성 검사 스케줄링 설정
     * 
     * @param enabled 스케줄링 활성화 여부
     */
    void setScheduledCheckEnabled(boolean enabled);
}