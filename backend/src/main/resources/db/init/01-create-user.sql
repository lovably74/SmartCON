-- MariaDB 초기화 스크립트
-- 사용자 생성 및 권한 부여

-- smartcon 사용자 생성 (모든 호스트에서 접근 가능)
CREATE USER IF NOT EXISTS 'smartcon'@'%' IDENTIFIED BY 'smartcon123';
CREATE USER IF NOT EXISTS 'smartcon'@'localhost' IDENTIFIED BY 'smartcon123';

-- 권한 부여
GRANT ALL PRIVILEGES ON smartcon_dev.* TO 'smartcon'@'%';
GRANT ALL PRIVILEGES ON smartcon_dev.* TO 'smartcon'@'localhost';

-- 권한 새로고침
FLUSH PRIVILEGES;