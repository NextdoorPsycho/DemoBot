@echo off
setlocal enabledelayedexpansion

if not exist config\bot.toml (
  copy config\bot.toml.example config\bot.toml >nul
  echo Created config\bot.toml from example.
  echo Set [runtime].discord_token before running.
  exit /b 1
)

call gradlew.bat clean shadowJar
if errorlevel 1 exit /b 1

java -jar build\libs\ArcaneDemoBot-2.0.0.jar
