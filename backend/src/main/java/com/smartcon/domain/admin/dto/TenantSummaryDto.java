package com.smartcon.domain.admin.dto;

import com.smartcon.domain.tenant.entity.Tenant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 테넌트 요약 정보 DTO
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TenantSummaryDto {

    private Long id;
    private String businessNo;          // 사업자번호
    private String companyName;         // 회사명
    private String representativeName;  // 대표자명
    private Tenant.SubscriptionStatus status; // 구독 상태
    private String planId;              // 구독 플랜 ID
    private long userCount;             // 사용자 수
    private LocalDateTime createdAt;    // 생성일
    private LocalDateTime lastLoginAt;  // 마지막 로그인 시간

    /**
     * Tenant 엔티티로부터 DTO 생성
     */
    public static TenantSummaryDto from(Tenant tenant, long userCount) {
        TenantSummaryDto dto = new TenantSummaryDto();
        dto.setId(tenant.getId());
        dto.setBusinessNo(tenant.getBusinessNo());
        dto.setCompanyName(tenant.getCompanyName());
        dto.setRepresentativeName(tenant.getRepresentativeName());
        dto.setStatus(tenant.getStatus());
        dto.setPlanId(tenant.getPlanId());
        dto.setUserCount(userCount);
        dto.setCreatedAt(tenant.getCreatedAt());
        // lastLoginAt은 별도 로직으로 설정 필요
        return dto;
    }
}