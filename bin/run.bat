@echo off
setlocal

echo.
echo [信息] 使用Jar命令运行Web工程。
echo.

where java >nul 2>nul
if errorlevel 1 (
    echo [错误] 未找到 java 命令，请先安装 JDK 并配置 JAVA_HOME 和 PATH。
    goto end
)

cd /d %~dp0
cd /d ..\aifc-admin\target

if not exist aifc-admin.jar (
    echo [错误] 未找到 aifc-admin.jar，请先执行 bin\package.bat 完成打包。
    goto end
)

set "JAVA_OPTS=-Xms256m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m"

java %JAVA_OPTS% -jar aifc-admin.jar

:end
cd /d %~dp0
pause
endlocal
