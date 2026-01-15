package com.smartcon.domain.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 사업자번호 유효성 검증 서비스
 */
@Component
@Slf4j
public class BusinessNumberValidator {

    /**
     * 사업자번호 유효성 검증
     * @param businessNumber 사업자번호 (10자리 숫자)
     * @return 유효하면 true
     */
    public boolean validate(String businessNumber) {
        if (businessNumber == null || businessNumber.isBlank()) {
            return false;
        }

        // 하이픈 제거
        String cleanNumber = businessNumber.replaceAll("-", "");

        // 10자리 숫자 확인
        if (!cleanNumber.matches("^\\d{10}$")) {
            log.warn("사업자번호 형식 오류 - 10자리 숫자가 아님: {}", businessNumber);
            return false;
        }

        // 체크섬 검증
        return validateChecksum(cleanNumber);
    }

    /**
     * 사업자번호 체크섬 검증
     * @param businessNumber 10자리 사업자번호
     * @return 유효하면 true
     */
    private boolean validateChecksum(String businessNumber) {
        try {
            int[] weights = {1, 3, 7, 1, 3, 7, 1, 3, 5};
            int sum = 0;

            // 첫 9자리 계산
            for (int i = 0; i < 9; i++) {
                int digit = Character.getNumericValue(businessNumber.charAt(i));
                sum += digit * weights[i];
            }

            // 8번째 자리 특별 처리
            int eighthDigit = Character.getNumericValue(businessNumber.charAt(8));
            sum += (eighthDigit * 5) / 10;

            // 체크섬 계산
            int checksum = (10 - (sum % 10)) % 10;
            int lastDigit = Character.getNumericValue(businessNumber.charAt(9));

            boolean isValid = checksum == lastDigit;
            if (!isValid) {
                log.warn("사업자번호 체크섬 오류 - 입력: {}, 계산된 체크섬: {}, 실제 마지막 자리: {}", 
                        businessNumber, checksum, lastDigit);
            }

            return isValid;
        } catch (Exception e) {
            log.error("사업자번호 검증 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 사업자번호 포맷팅 (123-45-67890)
     * @param businessNumber 사업자번호
     * @return 포맷팅된 사업자번호
     */
    public String format(String businessNumber) {
        if (businessNumber == null || businessNumber.isBlank()) {
            return businessNumber;
        }

        String cleanNumber = businessNumber.replaceAll("-", "");
        if (cleanNumber.length() != 10) {
            return businessNumber;
        }

        return String.format("%s-%s-%s",
                cleanNumber.substring(0, 3),
                cleanNumber.substring(3, 5),
                cleanNumber.substring(5, 10));
    }

    /**
     * 사업자번호 정규화 (하이픈 제거)
     * @param businessNumber 사업자번호
     * @return 정규화된 사업자번호
     */
    public String normalize(String businessNumber) {
        if (businessNumber == null || businessNumber.isBlank()) {
            return businessNumber;
        }
        return businessNumber.replaceAll("-", "");
    }
}
