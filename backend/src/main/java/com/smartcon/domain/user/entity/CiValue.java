package com.smartcon.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
     * 주의: 이 생성자는 CiValueValidator를 통해 호출되어야 합니다
     * @param phoneNumber 휴대폰 번호
     */
    public CiValue(String phoneNumber) {
        this.value = null; // CiValueValidator에서 설정
        this.generatedAt = LocalDateTime.now();
        this.phoneNumber = null; // EncryptionService에서 암호화하여 설정
    }

    /**
     * CI값 생성 (외부에서 생성된 값 사용)
     * @param ciValue 생성된 CI값
     * @param encryptedPhoneNumber 암호화된 휴대폰 번호
     */
    public CiValue(String ciValue, String encryptedPhoneNumber) {
        this.value = ciValue;
        this.generatedAt = LocalDateTime.now();
        this.phoneNumber = encryptedPhoneNumber;
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