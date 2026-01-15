package com.smartcon.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CI값 생성 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CiValueResponse {

    private String ciValue; // 생성된 CI값
    private LocalDateTime generatedAt; // 생성 일시
    private boolean isNewUser; // 신규 사용자 여부
    private String message; // 응답 메시지
}
