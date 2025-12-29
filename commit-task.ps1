# PowerShell 스크립트: 작업 완료 시 자동 커밋 및 푸시
param(
    [Parameter(Mandatory=$true)]
    [string]$TaskNumber,
    
    [Parameter(Mandatory=$true)]
    [string]$Description
)

# 변경사항 스테이징
Write-Host "변경사항을 스테이징합니다..." -ForegroundColor Green
git add .

# 커밋 메시지 생성
$commitMessage = "feat: Task $TaskNumber 완료 - $Description"

# 커밋 실행
Write-Host "커밋을 생성합니다: $commitMessage" -ForegroundColor Green
git commit -m $commitMessage

# GitHub에 푸시
Write-Host "GitHub에 푸시합니다..." -ForegroundColor Green
git push origin main

Write-Host "작업이 완료되었습니다!" -ForegroundColor Yellow