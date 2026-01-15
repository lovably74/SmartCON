package com.smartcon.domain.user.dto;

import com.smartcon.domain.user.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 역할 선택 요청 DTO
 * 다중 역할을 가진 사용자가 특정 역할을 선택할 때 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleSelectionRequest {

    @NotNull(message = "역할은 필수입니다")
    private Role role; // 선택한 역할

    private Long siteId; // 현장 ID (현장관리자인 경우)
}
