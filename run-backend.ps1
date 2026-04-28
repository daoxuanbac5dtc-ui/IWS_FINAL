$ErrorActionPreference = "Stop"
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.3.9-hotspot"
Set-Location -Path "$PSScriptRoot\iws_backend"
.\mvnw.cmd spring-boot:run
