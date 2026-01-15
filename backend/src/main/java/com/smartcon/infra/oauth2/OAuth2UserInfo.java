package com.smartcon.infra.oauth2;

import com.smartcon.domain.user.entity.SocialAccount.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OAuth2 사용자 정보 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2UserInfo {

    private SocialProvider provider; // 소셜 제공자
    private String providerId; // 제공자의 사용자 ID
    private String email; // 이메일
    private String name; // 이름
    private String phoneNumber; // 휴대폰 번호
    private String profileImageUrl; // 프로필 이미지 URL
    private String ciValue; // CI값 (제공자가 제공하는 경우)

    /**
     * 이메일이 인증되었는지 확인
     */
    public boolean hasEmail() {
        return email != null && !email.isBlank();
    }

    /**
     * 휴대폰 번호가 있는지 확인
     */
    public boolean hasPhoneNumber() {
        return phoneNumber != null && !phoneNumber.isBlank();
    }

    /**
     * CI값이 있는지 확인
     */
    public boolean hasCiValue() {
        return ciValue != null && !ciValue.isBlank();
    }
}
