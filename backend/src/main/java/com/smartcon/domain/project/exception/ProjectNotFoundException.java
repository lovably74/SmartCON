package com.smartcon.domain.project.exception;

/**
 * 프로젝트를 찾을 수 없을 때 발생하는 예외
 */
public class ProjectNotFoundException extends RuntimeException {
    
    public ProjectNotFoundException(String message) {
        super(message);
    }
    
    public ProjectNotFoundException(Long projectId) {
        super("프로젝트를 찾을 수 없습니다. ID: " + projectId);
    }
    
    public ProjectNotFoundException(Long projectId, Long tenantId) {
        super("프로젝트를 찾을 수 없습니다. ID: " + projectId + ", Tenant ID: " + tenantId);
    }
}
