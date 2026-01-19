package com.smartcon.domain.project.exception;

/**
 * 프로젝트 접근 권한이 없을 때 발생하는 예외
 */
public class ProjectAccessDeniedException extends RuntimeException {
    
    public ProjectAccessDeniedException(String message) {
        super(message);
    }
    
    public ProjectAccessDeniedException(Long projectId, String role) {
        super("프로젝트 접근 권한이 없습니다. Project ID: " + projectId + ", Role: " + role);
    }
}
