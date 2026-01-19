package com.smartcon.domain.contract.exception;

/**
 * 계약 접근 권한이 없을 때 발생하는 예외
 */
public class UnauthorizedContractAccessException extends RuntimeException {
    
    public UnauthorizedContractAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedContractAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
