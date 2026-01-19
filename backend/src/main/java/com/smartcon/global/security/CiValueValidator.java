package com.smartcon.global.security;

import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * CI값 검증 서비스
 * 휴대폰 인증을 통한 연계정보(CI) 값의 유효성 검증
 */
@Component
public class CiValueValidator {

    // CI값 형식 패턴 (CI_ 접두사 + 32자리 16진수)
    private static final Pattern CI_PATTERN = Pattern.compile("^CI_[0-9a-fA-F]{32}$");
    
    // CI값 최소 길이
    private static final int MIN_CI_LENGTH = 35; // "CI_" + 32자리
    
    // CI값 최대 유효 기간 (일)
    private static final long MAX_CI_VALIDITY_DAYS = 365 * 5; // 5년

    /**
     * CI값 형식 검증
     * @param ciValue 검증할 CI값
     * @return 형식이 올바르면 true
     */
    public boolean isValidFormat(String ciValue) {
        if (ciValue == null || ciValue.trim().isEmpty()) {
            return false;
        }

        // 최소 길이 확인
        if (ciValue.length() < MIN_CI_LENGTH) {
            return false;
        }

        // 패턴 매칭
        return CI_PATTERN.matcher(ciValue).matches();
    }

    /**
     * CI값 생성 (개발용)
     * 실제 운영 환경에서는 통신사 API를 통해 생성
     * @param phoneNumber 휴대폰 번호
     * @return 생성된 CI값
     */
    public String generateCiValue(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("휴대폰 번호는 필수입니다");
        }

        // 휴대폰 번호 정규화 (숫자만 추출)
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        
        // 정규화 후 빈 문자열 체크
        if (normalizedPhone == null || normalizedPhone.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 휴대폰 번호입니다");
        }
        
        // 유효성 검증
        if (!isValidPhoneNumber(normalizedPhone)) {
            throw new IllegalArgumentException("유효하지 않은 휴대폰 번호입니다");
        }

        // CI값 생성 (타임스탬프 포함하여 고유성 보장)
        String seed = normalizedPhone + System.currentTimeMillis();
        String hash = DigestUtils.md5DigestAsHex(seed.getBytes(StandardCharsets.UTF_8));
        
        return "CI_" + hash;
    }

    /**
     * 휴대폰 번호 정규화
     * @param phoneNumber 원본 휴대폰 번호
     * @return 정규화된 휴대폰 번호 (숫자만)
     */
    public String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        return phoneNumber.replaceAll("[^0-9]", "");
    }

    /**
     * 휴대폰 번호 유효성 검증
     * @param phoneNumber 휴대폰 번호 (숫자만)
     * @return 유효하면 true
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        // 한국 휴대폰 번호 형식 (010, 011, 016, 017, 018, 019로 시작하는 11자리)
        return phoneNumber.matches("^01[0-9]{9}$");
    }

    /**
     * CI값 유효 기간 검증
     * @param generatedAt CI값 생성 시간
     * @return 유효 기간 내이면 true
     */
    public boolean isWithinValidityPeriod(LocalDateTime generatedAt) {
        if (generatedAt == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = generatedAt.plusDays(MAX_CI_VALIDITY_DAYS);
        
        return now.isBefore(expiryDate);
    }

    /**
     * CI값 전체 검증 (형식 + 유효 기간)
     * @param ciValue CI값
     * @param generatedAt 생성 시간
     * @return 모든 검증을 통과하면 true
     */
    public boolean validate(String ciValue, LocalDateTime generatedAt) {
        return isValidFormat(ciValue) && isWithinValidityPeriod(generatedAt);
    }

    /**
     * CI값 마스킹
     * @param ciValue 원본 CI값
     * @return 마스킹된 CI값 (예: CI_abc****xyz)
     */
    public String maskCiValue(String ciValue) {
        if (ciValue == null || ciValue.length() < 10) {
            return "CI_****";
        }

        return ciValue.substring(0, 6) + "****" + ciValue.substring(ciValue.length() - 4);
    }

    /**
     * 두 CI값이 동일한지 비교
     * @param ciValue1 첫 번째 CI값
     * @param ciValue2 두 번째 CI값
     * @return 동일하면 true
     */
    public boolean equals(String ciValue1, String ciValue2) {
        if (ciValue1 == null || ciValue2 == null) {
            return false;
        }

        return ciValue1.equals(ciValue2);
    }

    /**
     * CI값으로부터 생성 시간 추정 (불가능하므로 null 반환)
     * 실제 시스템에서는 DB에 저장된 생성 시간을 사용해야 함
     * @param ciValue CI값
     * @return null (CI값만으로는 생성 시간을 알 수 없음)
     */
    public LocalDateTime extractGeneratedAt(String ciValue) {
        // CI값은 해시값이므로 생성 시간을 추출할 수 없음
        // DB에 저장된 생성 시간을 사용해야 함
        return null;
    }

    /**
     * CI값 검증 결과 객체
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * CI값 상세 검증 (검증 결과 객체 반환)
     * @param ciValue CI값
     * @param generatedAt 생성 시간
     * @return 검증 결과 객체
     */
    public ValidationResult validateDetailed(String ciValue, LocalDateTime generatedAt) {
        if (ciValue == null || ciValue.trim().isEmpty()) {
            return new ValidationResult(false, "CI값이 비어있습니다");
        }

        if (!isValidFormat(ciValue)) {
            return new ValidationResult(false, "CI값 형식이 올바르지 않습니다");
        }

        if (generatedAt == null) {
            return new ValidationResult(false, "CI값 생성 시간이 없습니다");
        }

        if (!isWithinValidityPeriod(generatedAt)) {
            return new ValidationResult(false, "CI값 유효 기간이 만료되었습니다");
        }

        return new ValidationResult(true, "유효한 CI값입니다");
    }
}
