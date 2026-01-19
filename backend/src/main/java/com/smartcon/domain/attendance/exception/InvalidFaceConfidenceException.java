package com.smartcon.domain.attendance.exception;

import java.math.BigDecimal;

/**
 * 안면인식 신뢰도가 임계값 미만일 때 발생하는 예외
 */
public class InvalidFaceConfidenceException extends RuntimeException {
    
    private static final BigDecimal THRESHOLD = new BigDecimal("0.85");
    
    public InvalidFaceConfidenceException(BigDecimal confidence) {
        super(String.format("안면인식 신뢰도가 임계값(%.2f) 미만입니다: %.4f", 
                THRESHOLD, confidence));
    }
    
    public InvalidFaceConfidenceException(String message) {
        super(message);
    }
}
