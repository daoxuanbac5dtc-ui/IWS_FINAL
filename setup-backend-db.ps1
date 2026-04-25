param(
    [string]$DbPassword = "ltd24005",
    [string]$DbUser = "root",
    [string]$DbHost = "127.0.0.1",
    [int]$DbPort = 3306,
    [string]$DbName = "iws_dbs"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $repoRoot "iws_backend"
$sqlPath = Join-Path $backendDir "IWS_FINAL_MySQL.sql"

if (-not (Test-Path $sqlPath)) {
    throw "Cannot find SQL dump at $sqlPath"
}

function Get-MySqlExe {
    $candidates = @(
        "C:\Program Files\MySQL\MySQL Server 9.4\bin\mysql.exe",
        "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    )

    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    $mysqlCmd = Get-Command mysql.exe -ErrorAction SilentlyContinue
    if ($mysqlCmd) {
        return $mysqlCmd.Source
    }

    throw "mysql.exe not found. Install MySQL client or update this script."
}

$mysqlExe = Get-MySqlExe
$sql = Get-Content $sqlPath -Raw
$sql = $sql -replace "CREATE DATABASE IF NOT EXISTS PRO2113 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci", "CREATE DATABASE IF NOT EXISTS $DbName DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
$sql = $sql -replace "USE PRO2113", "USE $DbName"

$sql | & $mysqlExe "--default-character-set=utf8mb4" "-h" $DbHost "-P" $DbPort "-u" $DbUser "-p$DbPassword"

Write-Host "Imported database '$DbName' successfully."
