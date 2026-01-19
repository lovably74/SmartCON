package com.smartcon.domain.project.dto;

import com.smartcon.domain.project.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 프로젝트 목록 조회 요청 DTO
 * 정렬, 필터링, 검색 기능 지원
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectListRequest {

    private Project.ProjectStatus status; // 프로젝트 상태 필터
    private String search; // 검색어 (현장명)
    private String sortBy; // 정렬 기준 (recentLogin, recentAssignment, constructionPeriod)
    private String sortOrder; // 정렬 순서 (asc, desc)
    private Integer page; // 페이지 번호 (0부터 시작)
    private Integer size; // 페이지 크기

    /**
     * 기본값 설정
     */
    public void setDefaults() {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "recentLogin"; // 기본: 최근 로그인 순
        }
        if (sortOrder == null || sortOrder.trim().isEmpty()) {
            sortOrder = "desc"; // 기본: 내림차순
        }
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size <= 0) {
            size = 20; // 기본 페이지 크기
        }
    }

    /**
     * 검색어가 있는지 확인
     */
    public boolean hasSearch() {
        return search != null && !search.trim().isEmpty();
    }

    /**
     * 상태 필터가 있는지 확인
     */
    public boolean hasStatusFilter() {
        return status != null;
    }
}
