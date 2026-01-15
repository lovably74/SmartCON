package com.smartcon.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.DigestUtils;

import java.time.LocalDate;

/**
 * 개인정보 관리를 위한 임베디드 엔티티
 * 민감한 개인정보는 암호화하여 저장하고 마스킹하여 표시
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalInfo {

    @Column(name = "real_name", length = 100)
    private String realName; // 실명

    @Column(name = "encrypted_ssn", length = 255)
    private String encryptedSsn; // 주민번호 (암호화)

    @Column(name = "birth_date")
    private LocalDate birthDate; // 생년월일

    @Column(name = "gender", length = 10)
    private String gender; // 성별

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl; // 프로필 사진 URL

    @Column(name = "emergency_contact", length = 20)
    private String emergencyContact; // 비상연락처

    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName; // 비상연락처 이름

    @Column(name = "home_address", length = 500)
    private String homeAddress; // 자택주소

    @Column(name = "postal_code", length = 10)
    private String postalCode; // 우편번호

    /**
     * 주민번호 설정 (암호화하여 저장)
     * @param ssn 주민번호
     */
    public void setSsn(String ssn) {
        if (ssn != null && !ssn.trim().isEmpty()) {
            this.encryptedSsn = encryptSensitiveData(ssn);
            // 생년월일 자동 추출
            if (ssn.length() >= 6) {
                try {
                    String birthStr = ssn.substring(0, 6);
                    int year = Integer.parseInt(birthStr.substring(0, 2));
                    int month = Integer.parseInt(birthStr.substring(2, 4));
                    int day = Integer.parseInt(birthStr.substring(4, 6));
                    
                    // 2000년 기준으로 연도 계산
                    if (year <= 30) {
                        year += 2000;
                    } else {
                        year += 1900;
                    }
                    
                    this.birthDate = LocalDate.of(year, month, day);
                    
                    // 성별 자동 추출 (7번째 자리)
                    if (ssn.length() >= 7) {
                        char genderChar = ssn.charAt(6);
                        this.gender = (genderChar == '1' || genderChar == '3') ? "남성" : "여성";
                    }
                } catch (Exception e) {
                    // 주민번호 형식이 잘못된 경우 무시
                }
            }
        } else {
            this.encryptedSsn = null;
        }
    }

    /**
     * 마스킹된 주민번호 반환
     * @return 마스킹된 주민번호 (예: 123456-1******)
     */
    public String getMaskedSsn() {
        if (encryptedSsn == null || encryptedSsn.trim().isEmpty()) {
            return null;
        }
        // 실제로는 복호화 후 마스킹해야 하지만, 개발용으로 패턴만 반환
        return "******-*******";
    }

    /**
     * 마스킹된 비상연락처 반환
     * @return 마스킹된 비상연락처 (예: 010-****-5678)
     */
    public String getMaskedEmergencyContact() {
        if (emergencyContact == null || emergencyContact.length() < 8) {
            return emergencyContact;
        }
        
        String cleaned = emergencyContact.replaceAll("[^0-9]", "");
        if (cleaned.length() == 11) {
            return cleaned.substring(0, 3) + "-****-" + cleaned.substring(7);
        } else if (cleaned.length() == 10) {
            return cleaned.substring(0, 3) + "-***-" + cleaned.substring(6);
        }
        
        return emergencyContact;
    }

    /**
     * 마스킹된 주소 반환
     * @return 마스킹된 주소 (상세주소 부분 마스킹)
     */
    public String getMaskedHomeAddress() {
        if (homeAddress == null || homeAddress.length() < 10) {
            return homeAddress;
        }
        
        // 주소의 뒷부분을 마스킹 (예: "서울시 강남구 ****")
        int maskStart = homeAddress.length() / 2;
        StringBuilder masked = new StringBuilder(homeAddress.substring(0, maskStart));
        for (int i = maskStart; i < homeAddress.length(); i++) {
            if (homeAddress.charAt(i) == ' ') {
                masked.append(' ');
            } else {
                masked.append('*');
            }
        }
        
        return masked.toString();
    }

    /**
     * 개인정보 완성도 계산
     * @return 완성도 퍼센트 (0-100)
     */
    public int getCompletionPercentage() {
        int totalFields = 8; // 필수 필드 수
        int completedFields = 0;
        
        if (realName != null && !realName.trim().isEmpty()) completedFields++;
        if (encryptedSsn != null && !encryptedSsn.trim().isEmpty()) completedFields++;
        if (birthDate != null) completedFields++;
        if (gender != null && !gender.trim().isEmpty()) completedFields++;
        if (profilePhotoUrl != null && !profilePhotoUrl.trim().isEmpty()) completedFields++;
        if (emergencyContact != null && !emergencyContact.trim().isEmpty()) completedFields++;
        if (emergencyContactName != null && !emergencyContactName.trim().isEmpty()) completedFields++;
        if (homeAddress != null && !homeAddress.trim().isEmpty()) completedFields++;
        
        return (completedFields * 100) / totalFields;
    }

    /**
     * 필수 정보 입력 완료 여부
     * @return 필수 정보가 모두 입력되었으면 true
     */
    public boolean isEssentialInfoComplete() {
        return realName != null && !realName.trim().isEmpty() &&
               encryptedSsn != null && !encryptedSsn.trim().isEmpty() &&
               emergencyContact != null && !emergencyContact.trim().isEmpty();
    }

    /**
     * 민감한 데이터 암호화
     * @param data 원본 데이터
     * @return 암호화된 데이터
     */
    private String encryptSensitiveData(String data) {
        // 개발용 간단한 암호화 (실제 운영에서는 AES-256 사용)
        String salt = "SMARTCON_PERSONAL_INFO_SALT";
        return DigestUtils.md5DigestAsHex((salt + data).getBytes());
    }

    /**
     * 개인정보 유효성 검증
     * @return 유효하면 true
     */
    public boolean isValid() {
        // 실명 검증
        if (realName != null && (realName.trim().isEmpty() || realName.length() > 100)) {
            return false;
        }
        
        // 비상연락처 검증
        if (emergencyContact != null && !emergencyContact.matches("^[0-9-+()\\s]*$")) {
            return false;
        }
        
        // 생년월일 검증
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            return false;
        }
        
        return true;
    }
}