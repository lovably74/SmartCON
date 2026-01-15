package com.smartcon.domain.user.service;

import com.smartcon.domain.user.entity.SocialAccount;
import com.smartcon.domain.user.entity.SocialAccount.SocialProvider;
import com.smartcon.domain.user.entity.User;
import com.smartcon.domain.user.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 소셜 계정 관리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SocialAccountServiceImpl implements SocialAccountService {

    private final SocialAccountRepository socialAccountRepository;

    @Override
    public SocialAccount linkSocialAccount(
            User user,
            SocialProvider provider,
            String providerId,
            String providerEmail,
            String providerName,
            String accessToken,
            String refreshToken,
            LocalDateTime expiresAt) {
        
        log.info("소셜 계정 연동 - 사용자 ID: {}, 제공자: {}", user.getId(), provider);

        // 기존 연동 확인
        SocialAccount existingAccount = socialAccountRepository
                .findByUserAndProvider(user, provider)
                .orElse(null);

        if (existingAccount != null) {
            // 기존 계정 정보 업데이트
            log.info("기존 소셜 계정 업데이트 - 사용자 ID: {}, 제공자: {}", user.getId(), provider);
            existingAccount.setProviderId(providerId);
            existingAccount.setProviderEmail(providerEmail);
            existingAccount.setProviderName(providerName);
            existingAccount.updateTokens(accessToken, refreshToken, expiresAt);
            return socialAccountRepository.save(existingAccount);
        }

        // 신규 소셜 계정 생성
        SocialAccount socialAccount = SocialAccount.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .providerEmail(providerEmail)
                .providerName(providerName)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenExpiresAt(expiresAt)
                .linkedAt(LocalDateTime.now())
                .isPrimary(user.getSocialAccounts().isEmpty()) // 첫 번째 계정은 주 계정으로 설정
                .build();

        return socialAccountRepository.save(socialAccount);
    }

    @Override
    public void unlinkSocialAccount(User user, SocialProvider provider) {
        log.info("소셜 계정 연동 해제 - 사용자 ID: {}, 제공자: {}", user.getId(), provider);

        SocialAccount socialAccount = socialAccountRepository
                .findByUserAndProvider(user, provider)
                .orElseThrow(() -> new IllegalArgumentException("연동된 소셜 계정이 없습니다"));

        // 주 계정인 경우 다른 계정을 주 계정으로 설정
        if (socialAccount.isPrimary()) {
            List<SocialAccount> otherAccounts = socialAccountRepository
                    .findByUserAndProviderNot(user, provider);
            if (!otherAccounts.isEmpty()) {
                SocialAccount newPrimary = otherAccounts.get(0);
                newPrimary.setPrimary();
                socialAccountRepository.save(newPrimary);
            }
        }

        socialAccountRepository.delete(socialAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SocialAccount> getSocialAccounts(User user) {
        return socialAccountRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public SocialAccount getSocialAccount(User user, SocialProvider provider) {
        return socialAccountRepository.findByUserAndProvider(user, provider).orElse(null);
    }

    @Override
    public void updateTokens(SocialAccount socialAccount, String accessToken, String refreshToken, LocalDateTime expiresAt) {
        log.info("소셜 계정 토큰 업데이트 - 계정 ID: {}", socialAccount.getId());
        socialAccount.updateTokens(accessToken, refreshToken, expiresAt);
        socialAccountRepository.save(socialAccount);
    }

    @Override
    public void setPrimarySocialAccount(User user, SocialProvider provider) {
        log.info("주 소셜 계정 설정 - 사용자 ID: {}, 제공자: {}", user.getId(), provider);

        // 기존 주 계정 해제
        List<SocialAccount> accounts = socialAccountRepository.findByUser(user);
        accounts.forEach(account -> {
            if (account.isPrimary()) {
                account.unsetPrimary();
                socialAccountRepository.save(account);
            }
        });

        // 새로운 주 계정 설정
        SocialAccount socialAccount = socialAccountRepository
                .findByUserAndProvider(user, provider)
                .orElseThrow(() -> new IllegalArgumentException("연동된 소셜 계정이 없습니다"));

        socialAccount.setPrimary();
        socialAccountRepository.save(socialAccount);
    }
}
