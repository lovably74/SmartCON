package com.smartcon.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 급여 계좌 정보를 위한 임베디드 엔티티
 * 계좌번호는 암호화하여 저장하고 마스킹하여 표시
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {

    @Column(name = "bank_name", length = 50)
    private String bankName; // 은행명

    @Column(name = "bank_code", length = 10)
    private String bankCode; // 은행코드

    @Column(name = "encrypted_account_number", length = 255)
    private String encryptedAccountNumber; // 계좌번호 (암호화)

    @Column(name = "account_holder", length = 100)
    private String accountHolder; // 예금주명

    @Column(name = "is_salary_account")
    @Builder.Default
    private Boolean isSalaryAccount = true; // 급여계좌 여부

    @Column(name = "bank_account_is_verified")
    @Builder.Default
    private Boolean isVerified = false; // 계좌 인증 여부

    /**
     * 계좌번호 설정 (암호화하여 저장)
     * 주의: 이 메서드는 EncryptionService를 통해 호출되어야 합니다
     * @param accountNumber 계좌번호
     * @param encryptedValue 암호화된 계좌번호 (EncryptionService에서 생성)
     */
    public void setAccountNumber(String accountNumber, String encryptedValue) {
        if (accountNumber != null && !accountNumber.trim().isEmpty()) {
            // 숫자만 추출
            String cleanNumber = accountNumber.replaceAll("[^0-9]", "");
            this.encryptedAccountNumber = encryptedValue;
        } else {
            this.encryptedAccountNumber = null;
        }
    }

    /**
     * 마스킹된 계좌번호 반환
     * @param decryptedAccountNumber 복호화된 계좌번호 (EncryptionService에서 복호화)
     * @return 마스킹된 계좌번호 (예: 123-****-****-78)
     */
    public String getMaskedAccountNumber(String decryptedAccountNumber) {
        if (decryptedAccountNumber == null || decryptedAccountNumber.length() < 8) {
            return "****-****-****";
        }

        String cleaned = decryptedAccountNumber.replaceAll("[^0-9]", "");
        if (cleaned.length() >= 10) {
            return cleaned.substring(0, 3) + "-****-****-" + cleaned.substring(cleaned.length() - 2);
        }

        return "****-****-****";
    }
    
    /**
     * 마스킹된 계좌번호 반환 (암호화된 값만 있을 때)
     * @return 마스킹된 계좌번호 (예: ****-****-****)
     */
    public String getMaskedAccountNumber() {
        if (encryptedAccountNumber == null || encryptedAccountNumber.trim().isEmpty()) {
            return null;
        }

        // 복호화 없이 은행별 기본 마스킹 패턴 반환
        if (bankCode != null) {
            switch (bankCode) {
                case "004": // KB국민은행
                    return "***-**-****-***";
                case "011": // NH농협은행
                    return "***-****-****-**";
                case "020": // 우리은행
                    return "****-***-******";
                default:
                    return "****-****-****";
            }
        }

        return "****-****-****";
    }

    /**
     * 계좌 정보가 완전히 입력되었는지 확인
     */
    public boolean isComplete() {
        return bankName != null && !bankName.trim().isEmpty() &&
               bankCode != null && !bankCode.trim().isEmpty() &&
               encryptedAccountNumber != null && !encryptedAccountNumber.trim().isEmpty() &&
               accountHolder != null && !accountHolder.trim().isEmpty();
    }

    /**
     * 계좌 정보 요약 반환
     */
    public String getAccountSummary() {
        if (!isComplete()) {
            return "계좌 정보 미입력";
        }
        return bankName + " " + getMaskedAccountNumber() + " (" + accountHolder + ")";
    }

    /**
     * 계좌 정보 완성도 계산
     * @return 완성도 퍼센트 (0-100)
     */
    public int getCompletionPercentage() {
        int totalFields = 4; // 필수 필드 수
        int completedFields = 0;
        
        if (bankName != null && !bankName.trim().isEmpty()) completedFields++;
        if (bankCode != null && !bankCode.trim().isEmpty()) completedFields++;
        if (encryptedAccountNumber != null && !encryptedAccountNumber.trim().isEmpty()) completedFields++;
        if (accountHolder != null && !accountHolder.trim().isEmpty()) completedFields++;
        
        return (completedFields * 100) / totalFields;
    }

    /**
     * 계좌 인증 가능 여부
     * @return 인증 가능하면 true
     */
    public boolean canVerify() {
        return isComplete() && !isVerified;
    }

    /**
     * 계좌 인증 처리
     */
    public void verify() {
        if (canVerify()) {
            this.isVerified = true;
        }
    }

    /**
     * 계좌 정보 유효성 검증
     * @return 유효하면 true
     */
    public boolean isValid() {
        // 은행명 검증
        if (bankName != null && (bankName.trim().isEmpty() || bankName.length() > 50)) {
            return false;
        }
        
        // 은행코드 검증 (3자리 숫자)
        if (bankCode != null && !bankCode.matches("^[0-9]{3}$")) {
            return false;
        }
        
        // 예금주명 검증
        if (accountHolder != null && (accountHolder.trim().isEmpty() || accountHolder.length() > 100)) {
            return false;
        }
        
        return true;
    }

    /**
     * 은행코드로 은행명 자동 설정
     * @param code 은행코드
     */
    public void setBankCodeAndName(String code) {
        this.bankCode = code;
        this.bankName = getBankNameByCode(code);
    }

    /**
     * 은행코드로 은행명 조회
     * @param code 은행코드
     * @return 은행명
     */
    private String getBankNameByCode(String code) {
        if (code == null) return null;
        
        switch (code) {
            case "004": return "KB국민은행";
            case "011": return "NH농협은행";
            case "020": return "우리은행";
            case "003": return "IBK기업은행";
            case "088": return "신한은행";
            case "081": return "하나은행";
            case "027": return "한국씨티은행";
            case "023": return "SC제일은행";
            case "039": return "경남은행";
            case "034": return "광주은행";
            case "032": return "부산은행";
            case "045": return "새마을금고";
            case "048": return "신협";
            case "071": return "우체국";
            case "089": return "케이뱅크";
            case "090": return "카카오뱅크";
            case "092": return "토스뱅크";
            default: return "기타은행";
        }
    }
}