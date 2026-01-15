#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import re

# 파일 읽기
with open('src/main/java/com/smartcon/domain/user/service/AuthServiceImpl.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Role import 추가 (User import 다음에)
if 'import com.smartcon.domain.user.entity.Role;' not in content:
    content = content.replace(
        'import com.smartcon.domain.user.entity.User;',
        'import com.smartcon.domain.user.entity.Role;\nimport com.smartcon.domain.user.entity.User;'
    )
    print("Role import 추가됨")
else:
    print("Role import 이미 존재함")

# 파일 쓰기 (UTF-8 without BOM)
with open('src/main/java/com/smartcon/domain/user/service/AuthServiceImpl.java', 'w', encoding='utf-8') as f:
    f.write(content)

print("AuthServiceImpl.java 수정 완료")
