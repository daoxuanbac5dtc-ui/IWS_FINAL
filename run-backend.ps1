param(
    [string]$DbPassword = "",
    [string]$DbUser = "root",
    [string]$DbHost = "127.0.0.1",
    [int]$DbPort = 3306,
    [string]$DbName = "iws_dbs"
)

# Load .env file if exists
if (Test-Path ".env") {
    Get-Content ".env" | Foreach-Object {
        if ($_ -match "^\s*([^#][^=]+)\s*=\s*(.*)") {
            [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim())
        }
    }
    
    if (-not $PSBoundParameters.ContainsKey('DbPassword') -and $env:DB_PASSWORD) {
        $DbPassword = $env:DB_PASSWORD
    }
}

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $repoRoot "iws_backend"

function Get-JavaHome {
    $candidates = @()

    # 1. Respect existing JAVA_HOME if set
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
        return $env:JAVA_HOME
    }

    # 2. Well-known install locations (ordered preference)
    $candidates += @(
        "C:\Program Files\Java\jdk-21",
        "C:\Program Files\Java\jdk-17",
        "C:\Program Files\Java\jdk-20",
        "C:\Program Files\Java\jdk-19",
        "C:\Program Files\Java\jdk-18"
    )

    # 3. Eclipse Adoptium (Temurin) — any JDK 17+
    $adoptium = Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match "^jdk-(1[7-9]|2[0-9])" } |
        Sort-Object Name -Descending |
        Select-Object -ExpandProperty FullName
    $candidates += $adoptium

    # 4. Return first candidate where java.exe actually exists
    foreach ($candidate in ($candidates | Where-Object { $_ } | Select-Object -Unique)) {
        if (Test-Path (Join-Path $candidate "bin\java.exe")) {
            return $candidate
        }
    }

    throw "JDK 17+ not found. Please install JDK 17 or JDK 21 from https://adoptium.net or https://www.oracle.com/java/technologies/downloads/"
}

$env:JAVA_HOME = Get-JavaHome
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://${DbHost}:${DbPort}/${DbName}"
$env:SPRING_DATASOURCE_USERNAME = $DbUser
$env:SPRING_DATASOURCE_PASSWORD = $DbPassword

Set-Location $backendDir

# Increase JVM memory to avoid OutOfMemory during compilation
$env:MAVEN_OPTS = "-Xmx512m -XX:+UseSerialGC"
$env:JAVA_TOOL_OPTIONS = "-Xmx512m"

Write-Host "Using JAVA_HOME: $env:JAVA_HOME"
Write-Host "Starting backend on http://127.0.0.1:8080"

& ".\mvnw.cmd" "spring-boot:run"
