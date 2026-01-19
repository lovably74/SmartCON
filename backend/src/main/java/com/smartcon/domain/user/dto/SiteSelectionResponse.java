package com.smartcon.domain.user.dto;

import com.smartcon.domain.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 현장 선택 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSelectionResponse {
    
    private Long userId; // 사용자 ID
    private Role selectedRole; // 선택된 역할
    private Long selectedSiteId; // 선택된 현장 ID
    private String siteName; // 현장명
    private String accessToken; // 새로운 액세스 토큰 (역할/현장 정보 포함)
    private String refreshToken; // 리프레시 토큰
}
