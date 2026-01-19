package com.smartcon.domain.user.service;

import com.smartcon.domain.project.entity.Project;
import com.smartcon.domain.project.entity.ProjectManager;
import com.smartcon.domain.project.repository.ProjectManagerRepository;
import com.smartcon.domain.project.repository.ProjectRepository;
import com.smartcon.domain.user.dto.*;
import com.smartcon.domain.user.entity.Role;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.global.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 역할 및 현장 선택 서비스 구현
 * 다중 역할 및 현장 선택 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleSelectionServiceImpl implements RoleSelectionService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectManagerRepository projectManagerRepository;

    @Override
    public RoleSelectionResponse getAvailableRolesAndSites(Long userId) {
        log.info("사용자 역할 및 현장 목록 조회 시작 - userId: {}", userId);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 사용자의 모든 역할에 대해 접근 가능한 현장 목록 조회
        List<UserRoleInfo> roleInfoList = new ArrayList<>();
        
        for (Role role : user.getRoles()) {
            List<SiteInfo> sites = getAvailableSitesForRole(userId, role);
            UserRoleInfo roleInfo = UserRoleInfo.from(role, sites);
            roleInfoList.add(roleInfo);
        }

        // 역할 레벨 순으로 정렬 (낮은 레벨이 높은 권한)
        roleInfoList.sort(Comparator.comparingInt(UserRoleInfo::getRoleLevel));

        boolean requiresRoleSelection = roleInfoList.size() > 1;
        boolean requiresSiteSelection = roleInfoList.stream()
                .anyMatch(UserRoleInfo::isRequiresSiteSelection);

        RoleSelectionResponse response = RoleSelectionResponse.builder()
                .userId(userId)
                .userName(user.getName())
                .availableRoles(roleInfoList)
                .requiresRoleSelection(requiresRoleSelection)
                .requiresSiteSelection(requiresSiteSelection)
                .build();

        log.info("사용자 역할 및 현장 목록 조회 완료 - userId: {}, 역할 수: {}, 역할 선택 필요: {}, 현장 선택 필요: {}", 
                userId, roleInfoList.size(), requiresRoleSelection, requiresSiteSelection);

        return response;
    }

    @Override
    public List<SiteInfo> getAvailableSitesForRole(Long userId, Role role) {
        log.info("역할별 현장 목록 조회 시작 - userId: {}, role: {}", userId, role);

        Long tenantId = TenantContext.getCurrentTenantId();
        List<SiteInfo> sites = new ArrayList<>();

        switch (role) {
            case ROLE_SUPER:
                // 슈퍼관리자는 모든 테넌트의 모든 현장 접근 가능
                // 실제로는 테넌트 선택 후 해당 테넌트의 현장 목록 제공
                sites = getAllActiveSites();
                break;

            case ROLE_HQ:
                // 본사관리자는 자신의 테넌트 내 모든 현장 접근 가능
                sites = getAllActiveSitesInTenant(tenantId);
                break;

            case ROLE_SITE:
            case ROLE_TEAM:
                // 현장관리자와 팀장은 배정된 현장만 접근 가능
                sites = getAssignedSites(userId, role);
                break;

            case ROLE_WORKER:
                // 일반노무자는 소속된 현장만 접근 가능
                sites = getWorkerSites(userId);
                break;

            default:
                log.warn("알 수 없는 역할: {}", role);
        }

        // 현장 목록 정렬: 최근 접근 > 최근 배정 > 남은 공사 기간
        sites.sort(this::compareSites);

        log.info("역할별 현장 목록 조회 완료 - userId: {}, role: {}, 현장 수: {}", userId, role, sites.size());
        return sites;
    }

    @Override
    @Transactional
    public SiteSelectionResponse selectRoleAndSite(Long userId, SiteSelectionRequest request) {
        log.info("역할 및 현장 선택 처리 시작 - userId: {}, role: {}, siteId: {}", 
                userId, request.getSelectedRole(), request.getSelectedSiteId());

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 역할 검증
        if (!user.hasRole(request.getSelectedRole())) {
            throw new IllegalArgumentException("사용자가 해당 역할을 가지고 있지 않습니다: " + request.getSelectedRole());
        }

        // 현장 접근 권한 검증
        if (!validateRoleAndSiteAccess(userId, request.getSelectedRole(), request.getSelectedSiteId())) {
            throw new IllegalArgumentException("해당 역할로 현장에 접근할 권한이 없습니다");
        }

        // 현장 조회
        Project project = projectRepository.findById(request.getSelectedSiteId())
                .orElseThrow(() -> new IllegalArgumentException("현장을 찾을 수 없습니다: " + request.getSelectedSiteId()));

        // 현장 접근 시간 업데이트
        updateSiteAccessTime(userId, request.getSelectedSiteId());

        // TODO: JWT 토큰 생성 (역할 및 현장 정보 포함)
        // 실제 구현에서는 JwtTokenProvider를 사용하여 토큰 생성
        String accessToken = "temp_access_token";
        String refreshToken = "temp_refresh_token";

        SiteSelectionResponse response = SiteSelectionResponse.builder()
                .userId(userId)
                .selectedRole(request.getSelectedRole())
                .selectedSiteId(request.getSelectedSiteId())
                .siteName(project.getName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        log.info("역할 및 현장 선택 처리 완료 - userId: {}, role: {}, siteId: {}", 
                userId, request.getSelectedRole(), request.getSelectedSiteId());

        return response;
    }

    @Override
    public boolean validateRoleAndSiteAccess(Long userId, Role role, Long siteId) {
        log.debug("역할 및 현장 접근 권한 검증 - userId: {}, role: {}, siteId: {}", userId, role, siteId);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 역할 확인
        if (!user.hasRole(role)) {
            log.warn("사용자가 해당 역할을 가지고 있지 않음 - userId: {}, role: {}", userId, role);
            return false;
        }

        // 역할별 현장 접근 권한 확인
        switch (role) {
            case ROLE_SUPER:
                // 슈퍼관리자는 모든 현장 접근 가능
                return projectRepository.existsById(siteId);

            case ROLE_HQ:
                // 본사관리자는 자신의 테넌트 내 모든 현장 접근 가능
                Long tenantId = TenantContext.getCurrentTenantId();
                return projectRepository.findByTenantIdAndId(tenantId, siteId).isPresent();

            case ROLE_SITE:
            case ROLE_TEAM:
                // 현장관리자와 팀장은 배정된 현장만 접근 가능
                return projectManagerRepository.hasAccessToProject(userId, siteId);

            case ROLE_WORKER:
                // 일반노무자는 소속된 현장만 접근 가능
                // TODO: 노무자-현장 관계 테이블 구현 후 검증 로직 추가
                return projectManagerRepository.hasAccessToProject(userId, siteId);

            default:
                log.warn("알 수 없는 역할: {}", role);
                return false;
        }
    }

    @Override
    @Transactional
    public void updateSiteAccessTime(Long userId, Long siteId) {
        log.debug("현장 접근 시간 업데이트 - userId: {}, siteId: {}", userId, siteId);

        projectManagerRepository.findActiveByUserIdAndProjectId(userId, siteId)
                .ifPresent(pm -> {
                    pm.updateLastAccessTime();
                    projectManagerRepository.save(pm);
                    log.debug("현장 접근 시간 업데이트 완료 - userId: {}, siteId: {}", userId, siteId);
                });
    }

    /**
     * 모든 활성 현장 조회 (슈퍼관리자용)
     */
    private List<SiteInfo> getAllActiveSites() {
        // 슈퍼관리자는 테넌트 필터링 없이 모든 현장 조회
        // 실제로는 페이징 처리 필요
        return projectRepository.findAll().stream()
                .filter(Project::isActive)
                .map(SiteInfo::from)
                .collect(Collectors.toList());
    }

    /**
     * 테넌트 내 모든 활성 현장 조회 (본사관리자용)
     */
    private List<SiteInfo> getAllActiveSitesInTenant(Long tenantId) {
        return projectRepository.findActiveProjectsByTenantId(tenantId).stream()
                .map(SiteInfo::from)
                .collect(Collectors.toList());
    }

    /**
     * 배정된 현장 조회 (현장관리자, 팀장용)
     */
    private List<SiteInfo> getAssignedSites(Long userId, Role role) {
        ProjectManager.ManagerRole managerRole = convertToManagerRole(role);
        if (managerRole == null) {
            return new ArrayList<>();
        }

        return projectManagerRepository.findActiveByUserIdAndRole(userId, managerRole).stream()
                .map(pm -> SiteInfo.from(pm.getProject(), pm.getLastAccessAt(), pm.getAssignedAt()))
                .collect(Collectors.toList());
    }

    /**
     * 노무자 소속 현장 조회
     */
    private List<SiteInfo> getWorkerSites(Long userId) {
        // TODO: 노무자-현장 관계 테이블 구현 후 실제 조회 로직 추가
        // 현재는 ProjectManager 테이블을 사용하여 임시 구현
        return projectManagerRepository.findActiveByUserId(userId).stream()
                .map(pm -> SiteInfo.from(pm.getProject(), pm.getLastAccessAt(), pm.getAssignedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Role을 ProjectManager.ManagerRole로 변환
     */
    private ProjectManager.ManagerRole convertToManagerRole(Role role) {
        return switch (role) {
            case ROLE_SITE -> ProjectManager.ManagerRole.SITE_MANAGER;
            case ROLE_TEAM -> ProjectManager.ManagerRole.TEAM_LEADER;
            default -> null;
        };
    }

    /**
     * 현장 정렬 비교자
     * 정렬 순서: 최근 접근 > 최근 배정 > 남은 공사 기간 (오름차순)
     */
    private int compareSites(SiteInfo s1, SiteInfo s2) {
        // 1. 최근 접근 시간 비교 (최근이 우선)
        if (s1.getLastAccessAt() != null && s2.getLastAccessAt() != null) {
            int accessCompare = s2.getLastAccessAt().compareTo(s1.getLastAccessAt());
            if (accessCompare != 0) {
                return accessCompare;
            }
        } else if (s1.getLastAccessAt() != null) {
            return -1; // s1이 최근 접근 있음
        } else if (s2.getLastAccessAt() != null) {
            return 1; // s2가 최근 접근 있음
        }

        // 2. 최근 배정 시간 비교 (최근이 우선)
        if (s1.getAssignedAt() != null && s2.getAssignedAt() != null) {
            int assignedCompare = s2.getAssignedAt().compareTo(s1.getAssignedAt());
            if (assignedCompare != 0) {
                return assignedCompare;
            }
        } else if (s1.getAssignedAt() != null) {
            return -1; // s1이 최근 배정 있음
        } else if (s2.getAssignedAt() != null) {
            return 1; // s2가 최근 배정 있음
        }

        // 3. 남은 공사 기간 비교 (적게 남은 것이 우선)
        if (s1.getRemainingDays() != null && s2.getRemainingDays() != null) {
            return s1.getRemainingDays().compareTo(s2.getRemainingDays());
        } else if (s1.getRemainingDays() != null) {
            return -1; // s1이 종료일 있음
        } else if (s2.getRemainingDays() != null) {
            return 1; // s2가 종료일 있음
        }

        return 0; // 모든 조건이 동일
    }
}
