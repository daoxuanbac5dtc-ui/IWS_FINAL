$ErrorActionPreference = "Continue"

# Use the found JDK 21 path
$customJavaPath = "C:\Program Files\Java\jdk-21"
if (Test-Path $customJavaPath) {
    $env:JAVA_HOME = $customJavaPath
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
    Write-Host "Using Java from: $env:JAVA_HOME" -ForegroundColor Green
} else {
    Write-Host "Warning: JDK 21 path not found at $customJavaPath. Trying system default..." -ForegroundColor Yellow
}

Set-Location -Path "$PSScriptRoot\iws_backend"

Write-Host "Starting Backend..." -ForegroundColor Cyan
.\mvnw.cmd spring-boot:run

if ($LASTEXITCODE -ne 0) {
    Write-Host "`nBackend failed to start with exit code $LASTEXITCODE" -ForegroundColor Red
    Write-Host "Please ensure Java 17 or higher is installed and configured."
}

Write-Host "`nPress any key to close this window..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
