param(
    [string]$BindHost = "127.0.0.1",
    [int]$Port = 5173,
    [switch]$SkipInstall
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontendDir = Join-Path $repoRoot "iws_frontend"

Set-Location $frontendDir

if (-not $SkipInstall -and -not (Test-Path "node_modules")) {
    npm install
}

Write-Host "Starting frontend on http://$BindHost`:$Port"

& npm.cmd "run" "dev" "--" "--host" $BindHost "--port" $Port
