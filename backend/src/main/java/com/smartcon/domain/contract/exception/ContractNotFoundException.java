package com.smartcon.domain.contract.exception;

/**
 * 계약을 찾을 수 없을 때 발생하는 예외
 */
public class ContractNotFoundException extends RuntimeException {
    
    public ContractNotFoundException(String message) {
        super(message);
    }
    
    public ContractNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
