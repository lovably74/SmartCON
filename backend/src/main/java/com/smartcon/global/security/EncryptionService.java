package com.smartcon.global.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 암호화 서비스
 * 민감한 개인정보 및 사업자 정보 암호화/복호화 담당
 */
@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // 128 bits
    private static final int GCM_IV_LENGTH = 12; // 12 bytes (96 bits)
    
    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    /**
     * 생성자 - 암호화 키 초기화
     * @param encryptionKey Base64 인코딩된 암호화 키 (application.yml에서 주입)
     */
    public EncryptionService(@Value("${smartcon.security.encryption-key:}") String encryptionKey) {
        this.secureRandom = new SecureRandom();
        
        if (encryptionKey == null || encryptionKey.trim().isEmpty()) {
            // 개발 환경: 기본 키 생성 (운영 환경에서는 반드시 설정 필요)
            this.secretKey = generateDefaultKey();
        } else {
            // 운영 환경: 설정된 키 사용
            byte[] decodedKey = Base64.getDecoder().decode(encryptionKey);
            this.secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        }
    }

    /**
     * 기본 암호화 키 생성 (개발 환경용)
     * @return 생성된 SecretKey
     */
    private SecretKey generateDefaultKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256, secureRandom);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("암호화 키 생성 실패", e);
        }
    }

    /**
     * 데이터 암호화
     * @param plainText 평문 데이터
     * @return Base64 인코딩된 암호화 데이터 (IV + 암호문)
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.trim().isEmpty()) {
            return null;
        }

        try {
            // IV(Initialization Vector) 생성
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Cipher 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // 암호화 수행
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // IV + 암호문 결합
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            // Base64 인코딩하여 반환
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("데이터 암호화 실패", e);
        }
    }

    /**
     * 데이터 복호화
     * @param encryptedText Base64 인코딩된 암호화 데이터
     * @return 복호화된 평문 데이터
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return null;
        }

        try {
            // Base64 디코딩
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);

            // IV와 암호문 분리
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            // Cipher 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // 복호화 수행
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("데이터 복호화 실패", e);
        }
    }

    /**
     * 암호화된 데이터인지 확인
     * @param text 확인할 텍스트
     * @return 암호화된 데이터이면 true
     */
    public boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        try {
            // Base64 디코딩 시도
            byte[] decoded = Base64.getDecoder().decode(text);
            // 최소 길이 확인 (IV + 최소 암호문)
            return decoded.length > GCM_IV_LENGTH;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 데이터 마스킹 (주민번호용)
     * @param ssn 주민번호 (13자리)
     * @return 마스킹된 주민번호 (예: 123456-1******)
     */
    public String maskSsn(String ssn) {
        if (ssn == null || ssn.length() < 8) {
            return "******-*******";
        }

        String cleaned = ssn.replaceAll("[^0-9]", "");
        if (cleaned.length() == 13) {
            return cleaned.substring(0, 6) + "-" + cleaned.charAt(6) + "******";
        }

        return "******-*******";
    }

    /**
     * 데이터 마스킹 (전화번호용)
     * @param phoneNumber 전화번호
     * @return 마스킹된 전화번호 (예: 010-****-5678)
     */
    public String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return phoneNumber;
        }

        String cleaned = phoneNumber.replaceAll("[^0-9]", "");
        if (cleaned.length() == 11) {
            return cleaned.substring(0, 3) + "-****-" + cleaned.substring(7);
        } else if (cleaned.length() == 10) {
            return cleaned.substring(0, 3) + "-***-" + cleaned.substring(6);
        }

        return phoneNumber;
    }

    /**
     * 데이터 마스킹 (계좌번호용)
     * @param accountNumber 계좌번호
     * @return 마스킹된 계좌번호 (예: 123-****-****-78)
     */
    public String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) {
            return "****-****-****";
        }

        String cleaned = accountNumber.replaceAll("[^0-9]", "");
        if (cleaned.length() >= 10) {
            return cleaned.substring(0, 3) + "-****-****-" + cleaned.substring(cleaned.length() - 2);
        }

        return "****-****-****";
    }

    /**
     * 데이터 마스킹 (사업자번호용)
     * @param businessNumber 사업자번호
     * @return 마스킹된 사업자번호 (예: 123-**-*****)
     */
    public String maskBusinessNumber(String businessNumber) {
        if (businessNumber == null || businessNumber.length() < 8) {
            return "***-**-*****";
        }

        String cleaned = businessNumber.replaceAll("[^0-9]", "");
        if (cleaned.length() == 10) {
            return cleaned.substring(0, 3) + "-**-*****";
        }

        return "***-**-*****";
    }

    /**
     * 암호화 키를 Base64로 인코딩하여 반환 (키 생성용)
     * @return Base64 인코딩된 암호화 키
     */
    public String getEncodedKey() {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}
