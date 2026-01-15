package com.smartcon.domain.user.dto;

import com.smartcon.domain.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 사용자 역할 목록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRolesResponse {

    private List<RoleInfo> roles; // 사용자가 가진 역할 목록
    private boolean requiresSelection; // 역할 선택이 필요한지 여부

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleInfo {
        private Role role; // 역할
        private String displayName; // 표시명
        private List<SiteInfo> sites; // 해당 역할로 접근 가능한 현장 목록 (현장관리자인 경우)
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SiteInfo {
        private Long siteId; // 현장 ID
        private String siteName; // 현장명
        private String status; // 현장 상태
    }
}
