@echo off
setlocal

echo.
echo [信息] 打包Web工程，生成war/jar包文件。
echo.

where mvn >nul 2>nul
if errorlevel 1 (
    echo [错误] 未找到 mvn 命令，请先安装 Maven 并配置 PATH。
    goto end
)

%~d0
cd /d %~dp0

cd /d ..
call mvn clean package -pl aifc-admin -am -Dmaven.test.skip=true

:end
pause
endlocal
