#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# 파일 읽기
with open('src/main/java/com/smartcon/domain/user/service/AuthServiceImpl.java', 'r', encoding='utf-8') as f:
    content = f.read()

# validateLoginTypeForRole 메서드가 이미 있는지 확인
if 'validateLoginTypeForRole' not in content:
    # 마지막 메서드 뒤에 추가 (generateUserPermissions 메서드 앞에)
    method_to_add = '''
    @Override
    public boolean validateLoginTypeForRole(Role role, com.smartcon.domain.user.entity.LoginType loginType) {
        return role.isValidLoginType(loginType);
    }

'''
    
    # generateUserPermissions 메서드 앞에 추가
    content = content.replace(
        '    /**\n     * 사용자 역할에 따른 권한 정보 생성\n     */',
        method_to_add + '    /**\n     * 사용자 역할에 따른 권한 정보 생성\n     */'
    )
    print("validateLoginTypeForRole 메서드 추가됨")
else:
    print("validateLoginTypeForRole 메서드 이미 존재함")

# 파일 쓰기
with open('src/main/java/com/smartcon/domain/user/service/AuthServiceImpl.java', 'w', encoding='utf-8') as f:
    f.write(content)

print("AuthServiceImpl.java 수정 완료")
