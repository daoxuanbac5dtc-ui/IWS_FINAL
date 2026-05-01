$ErrorActionPreference = "Continue"

function Get-JavaMajorVersion {
    param([string]$VersionText)

    if (-not $VersionText) {
        return $null
    }

    if ($VersionText.StartsWith("1.")) {
        return [int]$VersionText.Split(".")[1]
    }

    return [int]$VersionText.Split(".")[0]
}

function Get-JavaCandidate {
    param([string]$JavaHome)

    if (-not $JavaHome) {
        return $null
    }

    $javaExe = Join-Path $JavaHome "bin\java.exe"
    $javacExe = Join-Path $JavaHome "bin\javac.exe"
    $releaseFile = Join-Path $JavaHome "release"

    if (-not (Test-Path $javaExe) -or -not (Test-Path $javacExe) -or -not (Test-Path $releaseFile)) {
        return $null
    }

    try {
        $versionLine = Get-Content $releaseFile | Where-Object { $_ -like 'JAVA_VERSION=*' } | Select-Object -First 1

        if (-not $versionLine) {
            return $null
        }

        $rawVersion = $versionLine.Split("=", 2)[1].Trim('"')
        $majorVersion = Get-JavaMajorVersion $rawVersion

        return [pscustomobject]@{
            JavaHome     = $JavaHome
            RawVersion   = $rawVersion
            MajorVersion = $majorVersion
        }
    } catch {
        return $null
    }
}

function Resolve-JavaRuntime {
    $candidateHomes = New-Object System.Collections.Generic.List[string]

    if ($env:JAVA_HOME) {
        $candidateHomes.Add($env:JAVA_HOME)
    }

    try {
        $javaCommand = Get-Command java -ErrorAction Stop
        $candidateHomes.Add((Split-Path (Split-Path $javaCommand.Source -Parent) -Parent))
    } catch {
    }

    $searchRoots = @(
        "C:\Program Files\Java",
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Microsoft",
        "C:\Program Files\Amazon Corretto"
    )

    foreach ($root in $searchRoots) {
        if (-not (Test-Path $root)) {
            continue
        }

        Get-ChildItem $root -Directory -ErrorAction SilentlyContinue |
            Sort-Object FullName -Descending |
            ForEach-Object { $candidateHomes.Add($_.FullName) }
    }

    return $candidateHomes |
        Where-Object { $_ } |
        Select-Object -Unique |
        ForEach-Object { Get-JavaCandidate $_ } |
        Where-Object { $_ -and $_.MajorVersion -ge 17 } |
        Sort-Object MajorVersion, JavaHome -Descending |
        Select-Object -First 1
}

function Test-BackendReady {
    try {
        $response = Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:8080/auth/login" -Method Options -TimeoutSec 3
        return $response.StatusCode -ge 200 -and $response.StatusCode -lt 500
    } catch {
        return $false
    }
}

if (Test-BackendReady) {
    Write-Host "Backend is already running on http://localhost:8080" -ForegroundColor Green
    return
}

$javaRuntime = Resolve-JavaRuntime

if (-not $javaRuntime) {
    Write-Host "Error: Java 17 or newer was not found. Install JDK 17+ before starting the backend." -ForegroundColor Red
    exit 1
}

$env:JAVA_HOME = $javaRuntime.JavaHome
$env:Path = "$($javaRuntime.JavaHome)\bin;$env:Path"

Write-Host "Using Java $($javaRuntime.RawVersion) from: $($javaRuntime.JavaHome)" -ForegroundColor Green

Set-Location -Path "$PSScriptRoot\iws_backend"

Write-Host "Starting Backend..." -ForegroundColor Cyan
.\mvnw.cmd spring-boot:run

if ($LASTEXITCODE -ne 0) {
    Write-Host "`nBackend failed to start with exit code $LASTEXITCODE" -ForegroundColor Red
    Write-Host "Please ensure Java 17 or higher is installed and configured."
}

Write-Host "`nPress any key to close this window..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
