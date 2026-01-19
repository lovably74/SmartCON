package com.smartcon.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사업자 정보를 위한 임베디드 엔티티
 * 사업자번호는 암호화하여 저장하고 마스킹하여 표시
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessInfo {

    @Column(name = "company_name", length = 200)
    private String companyName; // 회사명

    @Column(name = "encrypted_business_number", length = 255)
    private String encryptedBusinessNumber; // 사업자번호 (암호화)

    @Column(name = "ceo_name", length = 100)
    private String ceoName; // 대표자명

    @Column(name = "business_type", length = 100)
    private String businessType; // 업종

    @Column(name = "business_address", length = 500)
    private String businessAddress; // 사업장 주소

    @Column(name = "business_phone", length = 20)
    private String businessPhone; // 사업장 전화번호

    @Column(name = "business_email", length = 100)
    private String businessEmail; // 사업장 이메일

    @Column(name = "business_is_verified")
    @Builder.Default
    private Boolean isVerified = false; // 사업자 인증 여부

    @Column(name = "tax_invoice_email", length = 100)
    private String taxInvoiceEmail; // 세금계산서 발행 이메일

    /**
     * 사업자번호 설정 (암호화하여 저장)
     * 주의: 이 메서드는 EncryptionService를 통해 호출되어야 합니다
     * @param businessNumber 사업자번호
     * @param encryptedValue 암호화된 사업자번호 (EncryptionService에서 생성)
     */
    public void setBusinessNumber(String businessNumber, String encryptedValue) {
        if (businessNumber != null && !businessNumber.trim().isEmpty()) {
            // 숫자만 추출
            String cleanNumber = businessNumber.replaceAll("[^0-9]", "");
            if (cleanNumber.length() == 10) {
                this.encryptedBusinessNumber = encryptedValue;
            }
        } else {
            this.encryptedBusinessNumber = null;
        }
    }

    /**
     * 마스킹된 사업자번호 반환
     * @param decryptedBusinessNumber 복호화된 사업자번호 (EncryptionService에서 복호화)
     * @return 마스킹된 사업자번호 (예: 123-**-*****)
     */
    public String getMaskedBusinessNumber(String decryptedBusinessNumber) {
        if (decryptedBusinessNumber == null || decryptedBusinessNumber.length() < 8) {
            return "***-**-*****";
        }

        String cleaned = decryptedBusinessNumber.replaceAll("[^0-9]", "");
        if (cleaned.length() == 10) {
            return cleaned.substring(0, 3) + "-**-*****";
        }

        return "***-**-*****";
    }
    
    /**
     * 마스킹된 사업자번호 반환 (암호화된 값만 있을 때)
     * @return 마스킹된 사업자번호 (예: ***-**-*****)
     */
    public String getMaskedBusinessNumber() {
        if (encryptedBusinessNumber == null || encryptedBusinessNumber.trim().isEmpty()) {
            return null;
        }
        // 복호화 없이 기본 마스킹 패턴 반환
        return "***-**-*****";
    }

    /**
     * 마스킹된 사업장 전화번호 반환
     * @return 마스킹된 전화번호 (예: 02-****-5678)
     */
    public String getMaskedBusinessPhone() {
        if (businessPhone == null || businessPhone.length() < 8) {
            return businessPhone;
        }
        
        String cleaned = businessPhone.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("02") && cleaned.length() >= 9) {
            return "02-****-" + cleaned.substring(cleaned.length() - 4);
        } else if (cleaned.length() >= 10) {
            return cleaned.substring(0, 3) + "-****-" + cleaned.substring(cleaned.length() - 4);
        }
        
        return businessPhone;
    }

    /**
     * 사업자 정보가 완전히 입력되었는지 확인
     */
    public boolean isComplete() {
        return companyName != null && !companyName.trim().isEmpty() &&
               encryptedBusinessNumber != null && !encryptedBusinessNumber.trim().isEmpty() &&
               ceoName != null && !ceoName.trim().isEmpty() &&
               businessAddress != null && !businessAddress.trim().isEmpty();
    }

    /**
     * 사업자 정보 완성도 계산
     * @return 완성도 퍼센트 (0-100)
     */
    public int getCompletionPercentage() {
        int totalFields = 7; // 필수 필드 수
        int completedFields = 0;
        
        if (companyName != null && !companyName.trim().isEmpty()) completedFields++;
        if (encryptedBusinessNumber != null && !encryptedBusinessNumber.trim().isEmpty()) completedFields++;
        if (ceoName != null && !ceoName.trim().isEmpty()) completedFields++;
        if (businessType != null && !businessType.trim().isEmpty()) completedFields++;
        if (businessAddress != null && !businessAddress.trim().isEmpty()) completedFields++;
        if (businessPhone != null && !businessPhone.trim().isEmpty()) completedFields++;
        if (businessEmail != null && !businessEmail.trim().isEmpty()) completedFields++;
        
        return (completedFields * 100) / totalFields;
    }

    /**
     * 사업자 인증 가능 여부
     * @return 인증 가능하면 true
     */
    public boolean canVerify() {
        return isComplete() && !isVerified;
    }

    /**
     * 사업자 인증 처리
     */
    public void verify() {
        if (canVerify()) {
            this.isVerified = true;
        }
    }

    /**
     * 사업자 정보 유효성 검증
     * @return 유효하면 true
     */
    public boolean isValid() {
        // 회사명 검증
        if (companyName != null && (companyName.trim().isEmpty() || companyName.length() > 200)) {
            return false;
        }
        
        // 대표자명 검증
        if (ceoName != null && (ceoName.trim().isEmpty() || ceoName.length() > 100)) {
            return false;
        }
        
        // 이메일 검증
        if (businessEmail != null && !businessEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return false;
        }
        
        if (taxInvoiceEmail != null && !taxInvoiceEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return false;
        }
        
        // 전화번호 검증
        if (businessPhone != null && !businessPhone.matches("^[0-9-+()\\s]*$")) {
            return false;
        }
        
        return true;
    }

    /**
     * 사업자 정보 요약 반환
     */
    public String getBusinessSummary() {
        if (!isComplete()) {
            return "사업자 정보 미입력";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append(companyName);
        
        if (getMaskedBusinessNumber() != null) {
            summary.append(" (").append(getMaskedBusinessNumber()).append(")");
        }
        
        if (ceoName != null) {
            summary.append(" - ").append(ceoName);
        }
        
        return summary.toString();
    }

    /**
     * 세금계산서 발행 가능 여부
     * @return 발행 가능하면 true
     */
    public boolean canIssueTaxInvoice() {
        return isVerified && 
               taxInvoiceEmail != null && 
               !taxInvoiceEmail.trim().isEmpty();
    }
}