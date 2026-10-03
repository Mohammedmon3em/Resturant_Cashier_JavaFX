@echo off
REM ============================================================
REM  Al-Muaid-SeaFood - Launch Script
REM  Double-click this file to start the application.
REM ============================================================

set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

set JAR_PATH=%~dp0target\seafood-pos-1.0.0.jar

if not exist "%JAR_PATH%" (
    echo [INFO] Application JAR not found. Attempting to build the project...
    
    REM Check if mvn is available on PATH
    where mvn >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        echo [INFO] Building with Maven from PATH...
        call mvn clean package -DskipTests
    ) else (
        REM Check custom Maven location
        set CUSTOM_MVN=%USERPROFILE%\.gemini\antigravity\scratch\maven\apache-maven-3.9.6\bin\mvn.cmd
        if exist "%CUSTOM_MVN%" (
            echo [INFO] Building with Maven from %CUSTOM_MVN%...
            call "%CUSTOM_MVN%" clean package -DskipTests
        ) else (
            echo [ERROR] Maven not found. Please install Maven or build the project before running.
            pause
            exit /b 1
        )
    )
)

if exist "%JAR_PATH%" (
    echo Starting Al-Muaid-SeaFood...
    java -jar "%JAR_PATH%"
) else (
    echo [ERROR] Build failed or JAR file is still missing.
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with error. Press any key to close...
    pause
)
