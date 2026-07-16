@echo off
title MoneyMate Premium Launcher
echo ===================================================
echo     Dang khoi dong MoneyMate Premium App...
echo ===================================================
.\maven\bin\mvn compile exec:java
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Ung dung gap loi khi khoi chay.
    pause
)
