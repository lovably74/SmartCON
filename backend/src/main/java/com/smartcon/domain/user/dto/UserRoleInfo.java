package com.smartcon.domain.user.dto;

import com.smartcon.domain.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 사용자 역할 정보 DTO
 * 다중 역할 지원을 위한 역할 목록 및 현장 정보
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleInfo {
    
    private Role role; // 역할
    private String roleDisplayName; // 역할 표시명
    private int roleLevel; // 역할 레벨
    private List<SiteInfo> availableSites; // 해당 역할로 접근 가능한 현장 목록
    private boolean requiresSiteSelection; // 현장 선택 필요 여부
    
    /**
     * Role enum으로부터 UserRoleInfo 생성
     */
    public static UserRoleInfo from(Role role, List<SiteInfo> sites) {
        return UserRoleInfo.builder()
                .role(role)
                .roleDisplayName(role.getDisplayName())
                .roleLevel(role.getLevel())
                .availableSites(sites)
                .requiresSiteSelection(sites != null && sites.size() > 1)
                .build();
    }
}
