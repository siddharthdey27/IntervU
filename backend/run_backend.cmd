@echo off
set "JAVA_HOME=C:\Users\Siddharth\.antigravity-ide\extensions\redhat.java-1.55.0-win32-x64\jre\21.0.11-win32-x86_64"
echo Loading environment variables from .env...
powershell -Command "Get-Content ..\.env | ForEach-Object { if ($_ -match '^\s*([^#=]+)=(.*)$') { [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process') } }; & '%~dp0..\.tools\apache-maven-3.9.16\bin\mvn.cmd' spring-boot:run"
