package com.smartcon.domain.contract.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.regex.Pattern;

/**
 * 전자서명 검증 서비스
 * Base64 인코딩된 서명 데이터의 유효성을 검증
 */
@Slf4j
@Service
public class SignatureValidationService {

    // Base64 패턴 (표준 Base64 또는 URL-safe Base64)
    private static final Pattern BASE64_PATTERN = Pattern.compile(
            "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$"
    );

    // 최소 서명 데이터 크기 (바이트) - 너무 작은 서명은 유효하지 않음
    private static final int MIN_SIGNATURE_SIZE = 100;

    // 최대 서명 데이터 크기 (바이트) - 너무 큰 서명은 거부
    private static final int MAX_SIGNATURE_SIZE = 1024 * 1024; // 1MB

    /**
     * 전자서명 데이터 유효성 검증
     * 
     * @param signatureData Base64 인코딩된 서명 데이터
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateSignature(String signatureData) {
        if (signatureData == null || signatureData.trim().isEmpty()) {
            log.warn("서명 데이터가 비어있습니다");
            return false;
        }

        // 공백 제거
        String cleanedData = signatureData.trim();

        // Base64 형식 검증
        if (!isValidBase64(cleanedData)) {
            log.warn("유효하지 않은 Base64 형식입니다");
            return false;
        }

        // 디코딩 가능 여부 확인
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(cleanedData);
            
            // 크기 검증
            if (decodedBytes.length < MIN_SIGNATURE_SIZE) {
                log.warn("서명 데이터가 너무 작습니다: {} bytes", decodedBytes.length);
                return false;
            }

            if (decodedBytes.length > MAX_SIGNATURE_SIZE) {
                log.warn("서명 데이터가 너무 큽니다: {} bytes", decodedBytes.length);
                return false;
            }

            log.debug("서명 데이터 검증 성공 - 크기: {} bytes", decodedBytes.length);
            return true;

        } catch (IllegalArgumentException e) {
            log.warn("Base64 디코딩 실패", e);
            return false;
        }
    }

    /**
     * Base64 형식 검증
     * 
     * @param data 검증할 데이터
     * @return Base64 형식이면 true
     */
    private boolean isValidBase64(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }

        // 길이가 4의 배수가 아니면 패딩 추가
        int paddingLength = (4 - (data.length() % 4)) % 4;
        String paddedData = data + "=".repeat(paddingLength);

        return BASE64_PATTERN.matcher(paddedData).matches();
    }

    /**
     * 서명 데이터 정규화
     * 공백 제거 및 패딩 추가
     * 
     * @param signatureData 원본 서명 데이터
     * @return 정규화된 서명 데이터
     */
    public String normalizeSignature(String signatureData) {
        if (signatureData == null) {
            return null;
        }

        // 공백 제거
        String cleaned = signatureData.trim().replaceAll("\\s+", "");

        // 패딩 추가
        int paddingLength = (4 - (cleaned.length() % 4)) % 4;
        return cleaned + "=".repeat(paddingLength);
    }

    /**
     * 서명 데이터 크기 확인 (바이트)
     * 
     * @param signatureData Base64 인코딩된 서명 데이터
     * @return 디코딩된 데이터의 크기 (바이트), 실패시 -1
     */
    public int getSignatureSize(String signatureData) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(signatureData);
            return decodedBytes.length;
        } catch (Exception e) {
            log.warn("서명 데이터 크기 확인 실패", e);
            return -1;
        }
    }

    /**
     * 서명 데이터가 이미지 형식인지 확인
     * PNG, JPEG 등의 이미지 매직 넘버 확인
     * 
     * @param signatureData Base64 인코딩된 서명 데이터
     * @return 이미지 형식이면 true
     */
    public boolean isImageSignature(String signatureData) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(signatureData);
            
            if (decodedBytes.length < 4) {
                return false;
            }

            // PNG 매직 넘버: 89 50 4E 47
            if (decodedBytes[0] == (byte) 0x89 && 
                decodedBytes[1] == (byte) 0x50 && 
                decodedBytes[2] == (byte) 0x4E && 
                decodedBytes[3] == (byte) 0x47) {
                log.debug("PNG 이미지 서명 감지");
                return true;
            }

            // JPEG 매직 넘버: FF D8 FF
            if (decodedBytes[0] == (byte) 0xFF && 
                decodedBytes[1] == (byte) 0xD8 && 
                decodedBytes[2] == (byte) 0xFF) {
                log.debug("JPEG 이미지 서명 감지");
                return true;
            }

            return false;

        } catch (Exception e) {
            log.warn("이미지 형식 확인 실패", e);
            return false;
        }
    }
}
