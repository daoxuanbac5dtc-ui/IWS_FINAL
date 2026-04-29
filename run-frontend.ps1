$ErrorActionPreference = "Continue"

Set-Location -Path "$PSScriptRoot\iws_frontend"

if (-not (Test-Path "node_modules")) {
    Write-Host "node_modules not found. Running npm install..." -ForegroundColor Cyan
    npm install
}

Write-Host "Starting Frontend..." -ForegroundColor Cyan
npm run dev

if ($LASTEXITCODE -ne 0) {
    Write-Host "`nFrontend failed to start with exit code $LASTEXITCODE" -ForegroundColor Red
    Write-Host "Please ensure Node.js is installed and 'npm install' ran successfully."
}

Write-Host "`nPress any key to close this window..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
