package com.smartcon.domain.user.dto;

import com.smartcon.domain.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 현장 선택 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SiteSelectionRequest {
    
    private Role selectedRole; // 선택한 역할
    private Long selectedSiteId; // 선택한 현장 ID
}
