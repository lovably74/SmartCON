package com.smartcon.global.util;

/**
 * 사업자번호 유효성 검증 유틸리티
 * 한국 국세청 사업자번호 체크섬 알고리즘 구현
 */
public class BusinessNumberValidator {

    /**
     * 사업자번호 유효성 검증
     * @param businessNumber 사업자번호
     * @return 유효하면 true
     */
    public static boolean isValid(String businessNumber) {
        if (businessNumber == null) {
            return false;
        }

        String cleanNumber = businessNumber.replaceAll("[^0-9]", "");
        if (cleanNumber.length() != 10) {
            return false;
        }

        // 모든 자리가 0인 경우 무효
        if (cleanNumber.equals("0000000000")) {
            return false;
        }

        // 모든 자리가 같은 숫자인 경우 무효 (예: 1111111111, 2222222222)
        char firstDigit = cleanNumber.charAt(0);
        boolean allSame = true;
        for (int i = 1; i < cleanNumber.length(); i++) {
            if (cleanNumber.charAt(i) != firstDigit) {
                allSame = false;
                break;
            }
        }
        if (allSame) {
            return false;
        }

        // 사업자번호 체크섬 검증 (한국 국세청 알고리즘)
        int[] weights = {1, 3, 7, 1, 3, 7, 1, 3, 5};
        int sum = 0;

        // 첫 8자리까지 가중치 곱셈
        for (int i = 0; i < 8; i++) {
            sum += Character.getNumericValue(cleanNumber.charAt(i)) * weights[i];
        }

        // 9번째 자리는 특별 처리 (5를 곱하고 10으로 나눈 몫을 더함)
        sum += (Character.getNumericValue(cleanNumber.charAt(8)) * 5) / 10;

        // 체크 디지트 계산
        int remainder = sum % 10;
        int checkDigit = remainder == 0 ? 0 : 10 - remainder;

        return checkDigit == Character.getNumericValue(cleanNumber.charAt(9));
    }

    /**
     * 사업자번호 정규화 (숫자만 추출)
     * @param businessNumber 원본 사업자번호
     * @return 정규화된 사업자번호 (숫자만)
     */
    public static String normalize(String businessNumber) {
        if (businessNumber == null) {
            return null;
        }
        return businessNumber.replaceAll("[^0-9]", "");
    }

    /**
     * 사업자번호 포맷팅 (123-45-67890 형식)
     * @param businessNumber 사업자번호
     * @return 포맷팅된 사업자번호
     */
    public static String format(String businessNumber) {
        String cleaned = normalize(businessNumber);
        if (cleaned == null || cleaned.length() != 10) {
            return businessNumber;
        }

        return cleaned.substring(0, 3) + "-" + 
               cleaned.substring(3, 5) + "-" + 
               cleaned.substring(5);
    }
}
