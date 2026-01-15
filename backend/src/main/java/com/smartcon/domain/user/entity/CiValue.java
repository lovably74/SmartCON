package com.smartcon.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

/**
 * CI값 관리를 위한 Value Object
 * 휴대폰 인증을 통한 연계정보 고유 키값 관리
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CiValue {

    @Column(name = "ci_value", unique = true, length = 255)
    private String value; // CI값

    @Column(name = "ci_generated_at")
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now(); // CI값 생성 시간

    @Column(name = "ci_phone_number", length = 255)
    private String phoneNumber; // CI값 생성에 사용된 휴대폰 번호 (암호화)

    /**
     * 휴대폰 번호로 CI값 생성
     * @param phoneNumber 휴대폰 번호
     */
    public CiValue(String phoneNumber) {
        this.value = generateCiValue(phoneNumber);
        this.generatedAt = LocalDateTime.now();
        this.phoneNumber = encryptPhoneNumber(phoneNumber);
    }

    /**
     * CI값 생성 로직
     * 실제 구현에서는 통신사 CI값 생성 로직 사용
     * @param phoneNumber 휴대폰 번호
     * @return 생성된 CI값
     */
    private String generateCiValue(String phoneNumber) {
        // 개발용 CI값 생성 (실제 운영에서는 통신사 API 사용)
        String seed = phoneNumber + System.currentTimeMillis();
        return "CI_" + DigestUtils.md5DigestAsHex(seed.getBytes());
    }

    /**
     * 휴대폰 번호 암호화
     * @param phoneNumber 원본 휴대폰 번호
     * @return 암호화된 휴대폰 번호
     */
    private String encryptPhoneNumber(String phoneNumber) {
        // 개발용 간단한 암호화 (실제 운영에서는 AES 등 사용)
        return DigestUtils.md5DigestAsHex(phoneNumber.getBytes());
    }

    /**
     * CI값이 유효한지 확인
     * @return 유효하면 true
     */
    public boolean isValid() {
        return value != null && !value.trim().isEmpty() && 
               value.startsWith("CI_") && value.length() > 10;
    }

    /**
     * CI값 생성 후 경과 시간 (일 단위)
     * @return 경과 일수
     */
    public long getDaysFromGeneration() {
        if (generatedAt == null) {
            return 0;
        }
        return java.time.Duration.between(generatedAt, LocalDateTime.now()).toDays();
    }

    /**
     * 마스킹된 CI값 반환 (보안용)
     * @return 마스킹된 CI값
     */
    public String getMaskedValue() {
        if (value == null || value.length() < 10) {
            return "CI_****";
        }
        return value.substring(0, 6) + "****" + value.substring(value.length() - 4);
    }
}