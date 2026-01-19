package com.smartcon.domain.user.service;

import com.smartcon.domain.user.dto.*;
import com.smartcon.domain.user.entity.Role;

import java.util.List;

/**
 * 역할 및 현장 선택 서비스 인터페이스
 * 다중 역할 및 현장 선택 로직 처리
 */
public interface RoleSelectionService {

    /**
     * 사용자의 사용 가능한 역할 및 현장 목록 조회
     * 
     * @param userId 사용자 ID
     * @return 역할 선택 응답
     */
    RoleSelectionResponse getAvailableRolesAndSites(Long userId);

    /**
     * 특정 역할로 접근 가능한 현장 목록 조회
     * 
     * @param userId 사용자 ID
     * @param role 역할
     * @return 현장 정보 목록
     */
    List<SiteInfo> getAvailableSitesForRole(Long userId, Role role);

    /**
     * 역할 및 현장 선택 처리
     * 
     * @param userId 사용자 ID
     * @param request 현장 선택 요청
     * @return 현장 선택 응답 (새로운 토큰 포함)
     */
    SiteSelectionResponse selectRoleAndSite(Long userId, SiteSelectionRequest request);

    /**
     * 사용자가 특정 역할과 현장에 접근 권한이 있는지 검증
     * 
     * @param userId 사용자 ID
     * @param role 역할
     * @param siteId 현장 ID
     * @return 접근 권한 여부
     */
    boolean validateRoleAndSiteAccess(Long userId, Role role, Long siteId);

    /**
     * 현장 접근 시간 업데이트
     * 
     * @param userId 사용자 ID
     * @param siteId 현장 ID
     */
    void updateSiteAccessTime(Long userId, Long siteId);
}
