@echo off
chcp 65001 > nul
echo === SmartCON Lite - MariaDB 설정 ===
echo.
echo 이 스크립트는 로컬 개발 환경용 MariaDB를 설정합니다.
echo.
echo 사전 요구사항:
echo 1. MariaDB 10.11이 설치되어 있어야 합니다
echo 2. MariaDB 서비스가 실행 중이어야 합니다
echo.
pause

powershell -ExecutionPolicy Bypass -File "scripts\setup-local-mariadb.ps1"

echo.
echo 설정이 완료되었습니다.
echo 자세한 내용은 docs\로컬_MariaDB_설치_가이드.md를 참조하세요.
pause