$ErrorActionPreference = "Continue"

$repoRoot = $PSScriptRoot
$backendScript = Join-Path $repoRoot "run-backend.ps1"
$frontendScript = Join-Path $repoRoot "run-frontend.ps1"

function Test-BackendReady {
    try {
        $response = Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:8080/auth/login" -Method Options -TimeoutSec 3
        return $response.StatusCode -ge 200 -and $response.StatusCode -lt 500
    } catch {
        return $false
    }
}

function Wait-BackendReady {
    param(
        [int]$TimeoutSeconds = 90
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)

    while ((Get-Date) -lt $deadline) {
        if (Test-BackendReady) {
            return $true
        }

        Start-Sleep -Seconds 2
    }

    return $false
}

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

if (-not (Test-BackendReady)) {
    Start-Process powershell.exe -ArgumentList @(
        "-NoExit",
        "-ExecutionPolicy", "Bypass",
        "-File", "`"$backendScript`""
    )

    Write-Host "Waiting for backend to become ready..." -ForegroundColor Yellow

    if (-not (Wait-BackendReady)) {
        Write-Host "Backend did not become ready within 90 seconds. Frontend was not started." -ForegroundColor Red
        Write-Host "Please inspect the backend window for the real startup error."
        Write-Host "`nPress any key to close this manager window..."
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        exit 1
    }
} else {
    Write-Host "Backend already running on http://localhost:8080" -ForegroundColor Green
}

Start-Process powershell.exe -ArgumentList @(
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-File", "`"$frontendScript`""
)

Write-Host "`nBackend and Frontend have been launched." -ForegroundColor Green
Write-Host "Please check the individual windows for status and errors."
Write-Host "`nPress any key to close this manager window..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
