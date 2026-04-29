$ErrorActionPreference = "Continue"

$repoRoot = $PSScriptRoot
$backendScript = Join-Path $repoRoot "run-backend.ps1"
$frontendScript = Join-Path $repoRoot "run-frontend.ps1"

Write-Host "Checking scripts..." -ForegroundColor Cyan

if (-not (Test-Path $backendScript)) {
    Write-Host "Error: Cannot find $backendScript" -ForegroundColor Red
} else {
    Write-Host "Found backend script." -ForegroundColor Green
}

if (-not (Test-Path $frontendScript)) {
    Write-Host "Error: Cannot find $frontendScript" -ForegroundColor Red
} else {
    Write-Host "Found frontend script." -ForegroundColor Green
}

Write-Host "`nLaunching Backend and Frontend in new windows..." -ForegroundColor Cyan

Start-Process powershell.exe -ArgumentList @(
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-File", "`"$backendScript`""
)

Start-Sleep -Seconds 1

Start-Process powershell.exe -ArgumentList @(
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-File", "`"$frontendScript`""
)

Write-Host "`nBackend and Frontend have been launched." -ForegroundColor Green
Write-Host "Please check the individual windows for status and errors."
Write-Host "`nPress any key to close this manager window..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
