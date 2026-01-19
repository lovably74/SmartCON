package com.smartcon.domain.user.service;

import com.smartcon.domain.user.entity.*;
import com.smartcon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 관리 서비스 구현체
 * CI값 기반 사용자 관리 및 소셜 계정 연동 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final UserEncryptionService userEncryptionService;

    @Override
    public User findOrCreateUserByCi(String ciValue, SocialAccount.SocialProvider provider) {
        log.debug("CI값으로 사용자 조회/생성 시작: ciValue={}, provider={}", ciValue, provider);

        Optional<User> existingUser = userRepository.findByCiValueValue(ciValue);
        
        if (existingUser.isPresent()) {
            // 기존 사용자에게 새 소셜 계정 연동
            User user = existingUser.get();
            addSocialAccountIfNotExists(user, provider);
            log.debug("기존 사용자에 소셜 계정 연동: userId={}, provider={}", user.getId(), provider);
            return user;
        } else {
            // 새 사용자 생성
            User newUser = createNewSocialUser(ciValue, provider);
            User savedUser = userRepository.save(newUser);
            log.debug("새 사용자 생성 완료: userId={}, ciValue={}", savedUser.getId(), ciValue);
            return savedUser;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserByBusinessNumber(String businessNumber) {
        log.debug("사업자번호로 사용자 조회: businessNumber={}", businessNumber);
        return userRepository.findByBusinessNumber(businessNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        return user.getRoles().stream().toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getUserSiteIds(Long userId, Role role) {
        // TODO: 프로젝트 관리자 엔티티와 연동하여 구현
        log.debug("사용자 현장 목록 조회: userId={}, role={}", userId, role);
        return List.of(); // 임시 구현
    }

    @Override
    public void updatePersonalInfo(Long userId, PersonalInfoUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // PersonalInfo 업데이트
        if (user.getPersonalInfo() == null) {
            user.initializePersonalInfo();
        }
        
        if (request.residentNumber() != null) {
            userEncryptionService.encryptAndSetSsn(user.getPersonalInfo(), request.residentNumber());
        }
        if (request.address() != null) {
            user.getPersonalInfo().setHomeAddress(request.address());
        }
        if (request.emergencyContact() != null) {
            user.getPersonalInfo().setEmergencyContact(request.emergencyContact());
        }
        if (request.profileImageUrl() != null) {
            user.getPersonalInfo().setProfilePhotoUrl(request.profileImageUrl());
        }

        // BankAccount 업데이트
        if (request.bankAccount() != null) {
            if (user.getBankAccount() == null) {
                user.initializeBankAccount();
            }
            
            if (request.bankAccount().bankName() != null) {
                user.getBankAccount().setBankCodeAndName(getBankCodeFromName(request.bankAccount().bankName()));
            }
            if (request.bankAccount().accountNumber() != null) {
                userEncryptionService.encryptAndSetAccountNumber(user.getBankAccount(), request.bankAccount().accountNumber());
            }
            if (request.bankAccount().accountHolder() != null) {
                user.getBankAccount().setAccountHolder(request.bankAccount().accountHolder());
            }
        }

        userRepository.save(user);
        log.debug("개인정보 업데이트 완료: userId={}", userId);
    }
    
    /**
     * 은행명으로 은행코드 조회 (간단한 매핑)
     */
    private String getBankCodeFromName(String bankName) {
        return switch (bankName) {
            case "KB국민은행", "국민은행" -> "004";
            case "신한은행" -> "088";
            case "우리은행" -> "020";
            case "하나은행" -> "081";
            case "NH농협은행", "농협은행" -> "011";
            case "IBK기업은행", "기업은행" -> "003";
            default -> "999"; // 기타
        };
    }

    @Override
    public void updateBusinessInfo(Long userId, BusinessInfoUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // BusinessInfo 업데이트
        if (user.getBusinessInfo() == null) {
            user.initializeBusinessInfo();
        }
        
        if (request.companyName() != null) {
            user.getBusinessInfo().setCompanyName(request.companyName());
        }
        if (request.businessRegistrationNumber() != null) {
            userEncryptionService.encryptAndSetBusinessNumber(user.getBusinessInfo(), request.businessRegistrationNumber());
        }
        if (request.representativeName() != null) {
            user.getBusinessInfo().setCeoName(request.representativeName());
        }
        if (request.businessAddress() != null) {
            user.getBusinessInfo().setBusinessAddress(request.businessAddress());
        }
        if (request.businessPhone() != null) {
            user.getBusinessInfo().setBusinessPhone(request.businessPhone());
        }
        if (request.businessEmail() != null) {
            user.getBusinessInfo().setBusinessEmail(request.businessEmail());
        }
        if (request.businessType() != null) {
            user.getBusinessInfo().setBusinessType(request.businessType());
        }

        userRepository.save(user);
        log.debug("사업자 정보 업데이트 완료: userId={}", userId);
    }

    @Override
    public void addSocialAccount(Long userId, SocialAccount.SocialProvider provider, String providerId, String providerEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        addSocialAccountIfNotExists(user, provider, providerId, providerEmail);
        userRepository.save(user);
        log.debug("소셜 계정 추가 완료: userId={}, provider={}", userId, provider);
    }

    @Override
    public void toggleUserStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        user.setIsActive(isActive);
        userRepository.save(user);
        log.debug("사용자 상태 변경 완료: userId={}, isActive={}", userId, isActive);
    }

    @Override
    public String generateCiValue(String phoneNumber) {
        try {
            // 실제 구현에서는 통신사 CI값 생성 API 사용
            // 테스트용으로 전화번호 기반 일관된 해시 생성 (동일한 전화번호는 항상 동일한 CI값)
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String input = "SMARTCON_CI_" + phoneNumber; // 고정된 접두사로 일관성 보장
            byte[] hash = md.digest(input.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return "CI_" + hexString.toString().substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("CI값 생성 실패", e);
        }
    }

    @Override
    public void completePhoneVerification(Long userId, String phoneNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        user.setPhoneNumber(phoneNumber);
        user.setIsPhoneVerified(true);
        
        // CI값이 없으면 생성
        if (user.getCiValue() == null) {
            user.setCiValue(phoneNumber);
        }

        userRepository.save(user);
        log.debug("전화번호 인증 완료: userId={}, phoneNumber={}", userId, phoneNumber);
    }

    // === 내부 헬퍼 메서드들 ===

    /**
     * 새 소셜 사용자 생성
     */
    private User createNewSocialUser(String ciValue, SocialAccount.SocialProvider provider) {
        User user = User.builder()
                .name("사용자") // 기본 이름, 나중에 소셜 정보로 업데이트
                .email("temp@example.com") // 임시 이메일, 나중에 소셜 정보로 업데이트
                .authProvider(AuthProvider.valueOf(provider.name()))
                .loginType(LoginType.SOCIAL)
                .isPhoneVerified(true)
                .isActive(true)
                .build();

        // 테스트용 기본 테넌트 ID 설정 (실제로는 테넌트 컨텍스트에서 가져옴)
        user.setTenantId(1L);

        // CI값 직접 설정 (이미 생성된 CI값)
        user.setCiValueDirect(ciValue);

        // 기본 역할 설정 (일반노무자)
        user.addRole(Role.ROLE_WORKER);

        // 소셜 계정 추가
        addSocialAccountIfNotExists(user, provider);

        return user;
    }

    /**
     * 소셜 계정이 없으면 추가
     */
    private void addSocialAccountIfNotExists(User user, SocialAccount.SocialProvider provider) {
        addSocialAccountIfNotExists(user, provider, "temp_id", "temp@example.com");
    }

    /**
     * 소셜 계정이 없으면 추가 (상세 정보 포함)
     */
    private void addSocialAccountIfNotExists(User user, SocialAccount.SocialProvider provider, 
                                           String providerId, String providerEmail) {
        if (!user.hasSocialProvider(provider)) {
            SocialAccount socialAccount = SocialAccount.builder()
                    .user(user)
                    .provider(provider)
                    .providerId(providerId)
                    .providerEmail(providerEmail)
                    .linkedAt(LocalDateTime.now())
                    .isPrimary(user.getSocialAccounts().isEmpty()) // 첫 번째 계정이면 주 계정으로 설정
                    .build();

            user.addSocialAccount(socialAccount);
        }
    }
}