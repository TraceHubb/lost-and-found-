@rem
@rem Minimal Gradle Wrapper startup script for Windows.
@rem
@echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0

set DEFAULT_JVM_OPTS=

set WRAPPER_JAR=%DIRNAME%gradle\wrapper\gradle-wrapper.jar
set CLASSPATH=%WRAPPER_JAR%

if not exist "%WRAPPER_JAR%" (
  echo.
  echo ERROR: Gradle wrapper jar not found at:
  echo   %WRAPPER_JAR%
  echo.
  echo Fix: ensure gradle\wrapper\gradle-wrapper.jar exists.
  exit /b 1
)

java %DEFAULT_JVM_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

endlocal
