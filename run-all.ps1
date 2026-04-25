$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendScript = Join-Path $repoRoot "run-backend.ps1"
$frontendScript = Join-Path $repoRoot "run-frontend.ps1"

if (-not (Test-Path $backendScript)) {
    throw "Cannot find $backendScript"
}

if (-not (Test-Path $frontendScript)) {
    throw "Cannot find $frontendScript"
}

Start-Process powershell.exe -ArgumentList @(
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-File", $backendScript
)

Start-Sleep -Seconds 2

Start-Process powershell.exe -ArgumentList @(
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-File", $frontendScript
)

Write-Host "Opened backend and frontend in two PowerShell windows."
Write-Host "Frontend link will appear in the frontend window."
