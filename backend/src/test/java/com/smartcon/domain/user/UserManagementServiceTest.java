package com.smartcon.domain.user;

import com.smartcon.domain.user.entity.*;
import com.smartcon.domain.user.repository.UserRepository;
import com.smartcon.domain.user.service.UserManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserManagementService 기본 기능 테스트
 * Task 2.1 검증용 단위 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserManagementServiceTest {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("CI값 생성 - 동일한 전화번호는 항상 동일한 CI값을 생성해야 함")
    void testCiValueGeneration() {
        // Given: 동일한 전화번호
        String phoneNumber = "010-1234-5678";

        // When: 여러 번 CI값 생성
        String ciValue1 = userManagementService.generateCiValue(phoneNumber);
        String ciValue2 = userManagementService.generateCiValue(phoneNumber);
        String ciValue3 = userManagementService.generateCiValue(phoneNumber);

        // Then: 항상 동일한 CI값이 생성되어야 함
        assertThat(ciValue1).isNotNull();
        assertThat(ciValue1).startsWith("CI_");
        assertThat(ciValue1).isEqualTo(ciValue2);
        assertThat(ciValue2).isEqualTo(ciValue3);
    }

    @Test
    @DisplayName("새 사용자 생성 - CI값이 존재하지 않으면 새 사용자를 생성해야 함")
    void testNewUserCreation() {
        // Given: 새로운 CI값
        String phoneNumber = "010-9876-5432";
        String ciValue = userManagementService.generateCiValue(phoneNumber);
        
        // 해당 CI값으로 기존 사용자가 없음을 확인
        Optional<User> existingUser = userRepository.findByCiValue(ciValue);
        assertThat(existingUser).isEmpty();

        // When: CI값으로 사용자 조회/생성
        User user = userManagementService.findOrCreateUserByCi(ciValue, SocialAccount.SocialProvider.KAKAO);

        // Then: 새 사용자가 생성되어야 함
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getCiValue()).isEqualTo(ciValue);
        assertThat(user.getAuthProvider()).isEqualTo(AuthProvider.KAKAO);
        assertThat(user.getLoginType()).isEqualTo(LoginType.SOCIAL);
        assertThat(user.isPhoneVerified()).isTrue();
        assertThat(user.isActive()).isTrue();
        assertThat(user.getRoles()).contains(User.Role.ROLE_WORKER);
        
        // 데이터베이스에 저장되었는지 확인
        Optional<User> savedUser = userRepository.findByCiValue(ciValue);
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("기존 계정 연동 - CI값이 존재하면 기존 계정에 소셜 계정을 연동해야 함")
    void testExistingAccountLinking() {
        // Given: 기존 사용자가 있음
        String phoneNumber = "010-1111-2222";
        String ciValue = userManagementService.generateCiValue(phoneNumber);
        
        User existingUser = User.builder()
                .name("기존사용자")
                .email("existing@example.com")
                .phoneNumber(phoneNumber)
                .authProvider(AuthProvider.KAKAO)
                .loginType(LoginType.SOCIAL)
                .isPhoneVerified(true)
                .build();
        existingUser.setCiValueDirect(ciValue);
        existingUser.setTenantId(1L); // 테스트용 테넌트 ID 설정
        existingUser.addRole(User.Role.ROLE_WORKER);
        userRepository.save(existingUser);

        // When: 동일한 CI값으로 다른 소셜 제공자로 로그인 시도
        User linkedUser = userManagementService.findOrCreateUserByCi(ciValue, SocialAccount.SocialProvider.NAVER);

        // Then: 기존 사용자가 반환되고 새 소셜 계정이 연동되어야 함
        assertThat(linkedUser).isNotNull();
        assertThat(linkedUser.getId()).isEqualTo(existingUser.getId());
        assertThat(linkedUser.getCiValue()).isEqualTo(ciValue);
        
        // 소셜 계정이 추가되었는지 확인
        assertThat(linkedUser.getSocialAccounts()).hasSize(1);
        assertThat(linkedUser.hasSocialProvider(SocialAccount.SocialProvider.NAVER)).isTrue();
    }

    @Test
    @DisplayName("계정 정보 일관성 - CI값 기반 사용자는 항상 소셜 로그인 유형이어야 함")
    void testAccountInfoConsistency() {
        // Given: CI값으로 생성된 사용자
        String phoneNumber = "010-3333-4444";
        String ciValue = userManagementService.generateCiValue(phoneNumber);
        
        User user = userManagementService.findOrCreateUserByCi(ciValue, SocialAccount.SocialProvider.KAKAO);

        // Then: 사용자 정보 일관성 검증
        assertThat(user.isCiBasedUser()).isTrue();
        assertThat(user.isBusinessUser()).isFalse();
        assertThat(user.getLoginType()).isEqualTo(LoginType.SOCIAL);
        assertThat(user.canUseLoginType(LoginType.SOCIAL)).isTrue();
        assertThat(user.canUseLoginType(LoginType.BUSINESS)).isFalse();
        assertThat(user.isPersonalUser()).isTrue();
        assertThat(user.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("개인정보 업데이트 - 개인정보를 올바르게 업데이트해야 함")
    void testPersonalInfoUpdate() {
        // Given: 기존 사용자
        String phoneNumber = "010-5555-6666";
        String ciValue = userManagementService.generateCiValue(phoneNumber);
        User user = userManagementService.findOrCreateUserByCi(ciValue, SocialAccount.SocialProvider.KAKAO);

        // When: 개인정보 업데이트
        UserManagementService.PersonalInfoUpdateRequest request = 
            new UserManagementService.PersonalInfoUpdateRequest(
                "123456-1234567",
                "서울시 강남구 테헤란로 123",
                "010-9999-8888",
                "https://example.com/profile.jpg",
                new UserManagementService.BankAccountInfo(
                    "국민은행",
                    "123-456-789012",
                    "홍길동"
                )
            );

        userManagementService.updatePersonalInfo(user.getId(), request);

        // Then: 개인정보가 올바르게 업데이트되어야 함
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getPersonalInfo()).isNotNull();
        assertThat(updatedUser.getPersonalInfo().getHomeAddress()).isEqualTo("서울시 강남구 테헤란로 123");
        assertThat(updatedUser.getPersonalInfo().getEmergencyContact()).isEqualTo("010-9999-8888");
        assertThat(updatedUser.getBankAccount()).isNotNull();
        assertThat(updatedUser.getBankAccount().getBankName()).isEqualTo("국민은행");
    }
}