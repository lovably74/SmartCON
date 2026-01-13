# SmartCON Lite - Local MariaDB Setup Script
# 로컬 개발 환경용 MariaDB 설정 스크립트

Write-Host "=== SmartCON Lite - Local MariaDB Setup ===" -ForegroundColor Green

# MariaDB 서비스 상태 확인
Write-Host "Checking MariaDB service status..." -ForegroundColor Yellow
$mariadbService = Get-Service -Name "MySQL" -ErrorAction SilentlyContinue

if ($mariadbService) {
    Write-Host "MariaDB service found: $($mariadbService.Status)" -ForegroundColor Green
    
    if ($mariadbService.Status -ne "Running") {
        Write-Host "Starting MariaDB service..." -ForegroundColor Yellow
        Start-Service -Name "MySQL"
        Start-Sleep -Seconds 3
    }
} else {
    Write-Host "MariaDB service not found. Please install MariaDB 10.11 first." -ForegroundColor Red
    Write-Host "Download from: https://mariadb.org/download/" -ForegroundColor Cyan
    exit 1
}

# MariaDB 연결 테스트
Write-Host "Testing MariaDB connection..." -ForegroundColor Yellow

# 데이터베이스 및 사용자 생성 SQL 스크립트
$setupSql = @"
-- Create database
CREATE DATABASE IF NOT EXISTS smartcon_local 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER IF NOT EXISTS 'smartcon_user'@'localhost' IDENTIFIED BY 'smartcon_pass';

-- Grant privileges
GRANT ALL PRIVILEGES ON smartcon_local.* TO 'smartcon_user'@'localhost';

-- Flush privileges
FLUSH PRIVILEGES;

-- Show databases
SHOW DATABASES;

-- Show user
SELECT User, Host FROM mysql.user WHERE User = 'smartcon_user';
"@

# SQL 스크립트를 임시 파일로 저장
$tempSqlFile = "$env:TEMP\smartcon_setup.sql"
$setupSql | Out-File -FilePath $tempSqlFile -Encoding UTF8

Write-Host "Creating database and user..." -ForegroundColor Yellow
Write-Host "Please enter MariaDB root password (fhdlxpzm1*) when prompted." -ForegroundColor Cyan

# MySQL 클라이언트로 스크립트 실행
try {
    Get-Content $tempSqlFile | & mysql -u root -p
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Database and user created successfully!" -ForegroundColor Green
        
        # 연결 테스트
        Write-Host "Testing connection with smartcon_user..." -ForegroundColor Yellow
        $testSql = "SELECT DATABASE(), USER();"
        $testSql | mysql -u smartcon_user -psmartcon_pass smartcon_local
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Connection test successful!" -ForegroundColor Green
            
            # Spring Boot 테스트 실행
            Write-Host "Running Spring Boot connection test..." -ForegroundColor Yellow
            Set-Location -Path "backend"
            & mvn test -Dtest=LocalMariaDBConnectionTest -q
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "All tests passed! MariaDB setup complete." -ForegroundColor Green
            } else {
                Write-Host "Spring Boot test failed. Check application configuration." -ForegroundColor Red
            }
        } else {
            Write-Host "Connection test failed. Check user credentials." -ForegroundColor Red
        }
    } else {
        Write-Host "Failed to create database and user." -ForegroundColor Red
    }
} catch {
    Write-Host "Error executing SQL script: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    # 임시 파일 정리
    if (Test-Path $tempSqlFile) {
        Remove-Item $tempSqlFile
    }
}

Write-Host "Setup script completed." -ForegroundColor Green
Write-Host "If you encounter issues, please refer to docs/로컬_MariaDB_설치_가이드.md" -ForegroundColor Cyan