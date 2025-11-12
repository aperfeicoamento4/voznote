@echo off
REM Gradle start up script for Windows (gerado manualmente)
set DIRNAME=%~dp0
set APP_HOME=%DIRNAME:~0,-1%

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

java -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
