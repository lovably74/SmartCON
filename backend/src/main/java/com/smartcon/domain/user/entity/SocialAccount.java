package com.smartcon.domain.user.entity;

import com.smartcon.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 소셜 계정 연동 엔티티
 * 다중 소셜 계정 연동 지원 (카카오, 네이버 등)
 */
@Entity
@Table(name = "social_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 연동된 사용자

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private SocialProvider provider; // 소셜 제공자

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId; // 소셜 제공자의 사용자 ID

    @Column(name = "provider_email", length = 100)
    private String providerEmail; // 소셜 제공자의 이메일

    @Column(name = "provider_name", length = 50)
    private String providerName; // 소셜 제공자의 사용자명

    @Column(name = "linked_at", nullable = false)
    @Builder.Default
    private LocalDateTime linkedAt = LocalDateTime.now(); // 연동 일시

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false; // 주 계정 여부

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken; // 액세스 토큰 (암호화 저장)

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken; // 리프레시 토큰 (암호화 저장)

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt; // 토큰 만료 시간

    /**
     * 소셜 제공자 열거형
     */
    public enum SocialProvider {
        KAKAO("카카오", "https://kauth.kakao.com/oauth/authorize"),
        NAVER("네이버", "https://nid.naver.com/oauth2.0/authorize");

        private final String displayName;
        private final String authUrl;

        SocialProvider(String displayName, String authUrl) {
            this.displayName = displayName;
            this.authUrl = authUrl;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getAuthUrl() {
            return authUrl;
        }
    }

    /**
     * 주 계정인지 확인
     */
    public boolean isPrimary() {
        return Boolean.TRUE.equals(this.isPrimary);
    }

    /**
     * 토큰이 유효한지 확인
     */
    public boolean isTokenValid() {
        return tokenExpiresAt != null && LocalDateTime.now().isBefore(tokenExpiresAt);
    }

    /**
     * 토큰 정보 업데이트
     */
    public void updateTokens(String accessToken, String refreshToken, LocalDateTime expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = expiresAt;
    }

    /**
     * 주 계정으로 설정
     */
    public void setPrimary() {
        this.isPrimary = true;
    }

    /**
     * 주 계정 해제
     */
    public void unsetPrimary() {
        this.isPrimary = false;
    }
}