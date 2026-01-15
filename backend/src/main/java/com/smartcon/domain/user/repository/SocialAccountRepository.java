package com.smartcon.domain.user.repository;

import com.smartcon.domain.user.entity.SocialAccount;
import com.smartcon.domain.user.entity.SocialAccount.SocialProvider;
import com.smartcon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 소셜 계정 데이터 접근 리포지토리
 */
@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    /**
     * 사용자의 소셜 계정 목록 조회
     */
    List<SocialAccount> findByUser(User user);

    /**
     * 사용자의 특정 소셜 제공자 계정 조회
     */
    Optional<SocialAccount> findByUserAndProvider(User user, SocialProvider provider);

    /**
     * 사용자의 특정 소셜 제공자를 제외한 계정 목록 조회
     */
    List<SocialAccount> findByUserAndProviderNot(User user, SocialProvider provider);

    /**
     * 제공자 ID로 소셜 계정 조회
     */
    Optional<SocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);

    /**
     * 사용자의 주 소셜 계정 조회
     */
    Optional<SocialAccount> findByUserAndIsPrimaryTrue(User user);
}
