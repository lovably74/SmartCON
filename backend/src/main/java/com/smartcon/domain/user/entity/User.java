package com.smartcon.domain.user.entity;

import com.smartcon.global.tenant.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 시스템 사용자 엔티티
 * BaseTenantEntity를 상속받아 tenant_id를 자동으로 관리
 * MariaDB 최적화 적용
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTenantEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "provider_id", length = 100)
    private String socialId; // 카카오/네이버 연동 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 10)
    private Provider provider = Provider.LOCAL;

    @Column(name = "password_hash", length = 255)
    private String passwordHash; // 비밀번호 해시

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    // 안면인식 임베딩 데이터 (MariaDB TEXT 타입 사용)
    @Lob
    @Column(name = "face_embedding", columnDefinition = "TEXT")
    private String faceEmbedding;

    @Column(name = "login_failure_count")
    private Integer loginFailureCount = 0;

    /**
     * 인증 제공자 열거형
     */
    public enum Provider {
        LOCAL,  // 로컬 계정
        KAKAO,  // 카카오
        NAVER   // 네이버
    }

    /**
     * 사용자 역할 열거형
     */
    public enum Role {
        ROLE_SUPER,   // 슈퍼 관리자
        ROLE_HQ,      // 본사 관리자
        ROLE_SITE,    // 현장 관리자
        ROLE_TEAM,    // 노무 팀장
        ROLE_WORKER   // 노무자
    }

    /**
     * 활성 사용자 여부 확인
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * 이메일 인증 여부 확인
     */
    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(isEmailVerified);
    }

    /**
     * 로그인 실패 횟수 증가
     */
    public void incrementLoginFailureCount() {
        this.loginFailureCount = (this.loginFailureCount == null ? 0 : this.loginFailureCount) + 1;
    }

    /**
     * 로그인 실패 횟수 초기화
     */
    public void resetLoginFailureCount() {
        this.loginFailureCount = 0;
    }

    /**
     * 계정 잠금 여부 확인 (5회 이상 실패 시)
     */
    public boolean isLocked() {
        return this.loginFailureCount != null && this.loginFailureCount >= 5;
    }
}
