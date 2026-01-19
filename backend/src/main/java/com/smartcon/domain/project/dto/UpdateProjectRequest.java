package com.smartcon.domain.project.dto;

import com.smartcon.domain.project.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 프로젝트 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProjectRequest {

    @NotBlank(message = "현장명은 필수입니다")
    private String name; // 현장명

    @NotNull(message = "공사 시작일은 필수입니다")
    private LocalDate constructionPeriodStart; // 공사 시작일

    @NotNull(message = "공사 종료일은 필수입니다")
    private LocalDate constructionPeriodEnd; // 공사 종료일

    private String siteManagerName; // 현장소장명

    private String location; // 현장 위치

    private String description; // 프로젝트 설명

    @NotNull(message = "프로젝트 상태는 필수입니다")
    private Project.ProjectStatus status; // 프로젝트 상태
}
