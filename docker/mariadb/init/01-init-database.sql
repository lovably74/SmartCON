-- MariaDB 초기화 스크립트
-- 로컬 개발용 데이터베이스 및 사용자 설정

-- 데이터베이스가 존재하지 않으면 생성
CREATE DATABASE IF NOT EXISTS smartcon_local 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- 사용자 권한 설정
GRANT ALL PRIVILEGES ON smartcon_local.* TO 'smartcon_user'@'%';
FLUSH PRIVILEGES;

-- 기본 설정 확인
SELECT 'MariaDB 초기화 완료' AS status;