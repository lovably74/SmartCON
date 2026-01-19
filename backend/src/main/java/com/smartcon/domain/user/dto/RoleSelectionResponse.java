package com.smartcon.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 역할 선택 응답 DTO
 * 사용자의 다중 역할 및 현장 선택 정보
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleSelectionResponse {
    
    private Long userId; // 사용자 ID
    private String userName; // 사용자 이름
    private List<UserRoleInfo> availableRoles; // 사용 가능한 역할 목록
    private boolean requiresRoleSelection; // 역할 선택 필요 여부
    private boolean requiresSiteSelection; // 현장 선택 필요 여부
    
    /**
     * 단일 역할, 단일 현장인 경우 자동 선택 가능 여부
     */
    public boolean canAutoSelect() {
        if (availableRoles == null || availableRoles.isEmpty()) {
            return false;
        }
        
        // 역할이 하나이고, 현장도 하나인 경우
        if (availableRoles.size() == 1) {
            UserRoleInfo roleInfo = availableRoles.get(0);
            return roleInfo.getAvailableSites() != null && 
                   roleInfo.getAvailableSites().size() == 1;
        }
        
        return false;
    }
}
