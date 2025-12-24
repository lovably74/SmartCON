package com.smartcon.global.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 엔티티의 공통 부모 클래스
 * MariaDB 최적화된 기본 필드들을 제공
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 생성일시 (MariaDB TIMESTAMP 타입 사용)
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, 
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    /**
     * 수정일시 (MariaDB TIMESTAMP 타입 사용, 자동 업데이트)
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, 
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    /**
     * 엔티티 저장 전 처리
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 엔티티 업데이트 전 처리
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 엔티티 동등성 비교 (ID 기반)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BaseEntity that = (BaseEntity) obj;
        return id != null && id.equals(that.id);
    }

    /**
     * 해시코드 생성 (ID 기반)
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    /**
     * 문자열 표현
     */
    @Override
    public String toString() {
        return String.format("%s{id=%d, createdAt=%s, updatedAt=%s}", 
                getClass().getSimpleName(), id, createdAt, updatedAt);
    }
}