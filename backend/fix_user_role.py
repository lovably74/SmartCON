#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
User.Role을 Role로 변경하는 스크립트
UTF-8 인코딩을 유지하면서 안전하게 변경
"""

import os
import re

# 수정할 파일 목록
files_to_fix = [
    "src/test/java/com/smartcon/domain/user/PersonalInfoManagementPropertyTest.java",
    "src/test/java/com/smartcon/domain/user/PersonalInfoTest.java",
    "src/test/java/com/smartcon/domain/user/UserManagementServiceTest.java",
    "src/test/java/com/smartcon/domain/user/CiValueAndAccountManagementPropertyTest.java",
    "src/test/java/com/smartcon/domain/user/service/AuthServiceDevTokenPropertyBasedTest.java",
    "src/test/java/com/smartcon/domain/user/service/AuthServiceImplTest.java",
]

def fix_file(filepath):
    """파일에서 User.Role을 Role로 변경"""
    try:
        # UTF-8로 파일 읽기
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # User.Role을 Role로 변경
        modified_content = re.sub(r'User\.Role\.', 'Role.', content)
        
        # 변경사항이 있는 경우에만 파일 쓰기
        if content != modified_content:
            with open(filepath, 'w', encoding='utf-8', newline='\n') as f:
                f.write(modified_content)
            print(f"✓ Fixed: {filepath}")
            return True
        else:
            print(f"- No changes: {filepath}")
            return False
    except Exception as e:
        print(f"✗ Error fixing {filepath}: {e}")
        return False

def main():
    """메인 함수"""
    print("Starting User.Role to Role conversion...")
    print("=" * 60)
    
    fixed_count = 0
    for filepath in files_to_fix:
        if os.path.exists(filepath):
            if fix_file(filepath):
                fixed_count += 1
        else:
            print(f"✗ File not found: {filepath}")
    
    print("=" * 60)
    print(f"Completed! Fixed {fixed_count} file(s).")

if __name__ == "__main__":
    main()
