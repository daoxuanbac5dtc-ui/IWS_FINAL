$ErrorActionPreference = "Continue"

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

Set-Location -Path "$PSScriptRoot\iws_frontend"

$backendScript = Join-Path $PSScriptRoot "run-backend.ps1"

if (-not (Test-BackendReady) -and (Test-Path $backendScript)) {
    Write-Host "Backend not detected. Starting backend first..." -ForegroundColor Yellow

    Start-Process powershell.exe -ArgumentList @(
        "-NoExit",
        "-ExecutionPolicy", "Bypass",
        "-File", "`"$backendScript`""
    )

    if (-not (Wait-BackendReady)) {
        Write-Host "Warning: backend is still not ready after 90 seconds. Frontend will still start." -ForegroundColor Yellow
    } else {
        Write-Host "Backend is ready on http://localhost:8080" -ForegroundColor Green
    }
}

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
