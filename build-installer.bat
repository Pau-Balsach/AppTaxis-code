@echo off
setlocal EnableDelayedExpansion
set "APP_NAME=AplicacioTaxis"
set "APP_VERSION=1.1.3"
set "MAIN_CLASS=main.Launcher"
set "BUILD_DIR=%~dp0build-output"
set "DIST_DIR=%~dp0dist"
set "JDK21_HOME=C:\jdk21"
echo.
echo === AppTaxis - Generador de instalador ===
echo.
echo Directorio: %~dp0
echo.
if not exist "%~dp0pom.xml" (
    echo ERROR: No se encuentra pom.xml en este directorio.
    echo Ejecuta el script desde la raiz del proyecto.
    pause
    exit /b 1
)
echo pom.xml encontrado OK
echo.
if not exist "%~dp0config.properties" (
    echo ERROR: No se encuentra config.properties en la raiz del proyecto.
    pause
    exit /b 1
)
echo config.properties encontrado OK
echo.
echo [1/4] Verificando Java...
java -version
if errorlevel 1 (
    echo ERROR: Java no encontrado en el PATH.
    pause
    exit /b 1
)
echo.
echo [2/4] Verificando Maven...
call mvn -version
if errorlevel 1 (
    echo ERROR: Maven no encontrado en el PATH.
    pause
    exit /b 1
)
echo.
echo [3/4] Compilando con Maven...
call mvn clean package
if errorlevel 1 (
    echo ERROR: La compilacion ha fallado. Lee los mensajes de arriba.
    pause
    exit /b 1
)
echo.
echo [4/4] Buscando JAR en target...
set "JAR_PATH="
for /f "delims=" %%f in ('dir /b /s "%~dp0target\*jar-with-dependencies.jar" 2^>nul') do set "JAR_PATH=%%f"
if "!JAR_PATH!"=="" (
    echo ERROR: No se encontro el jar-with-dependencies en target.
    dir /b "%~dp0target\" 2>nul
    pause
    exit /b 1
)
echo JAR: !JAR_PATH!
echo.
echo [5/5] Generando instalador con jpackage...
jpackage --version
if errorlevel 1 (
    echo ERROR: jpackage no encontrado.
    pause
    exit /b 1
)
if not exist "%JDK21_HOME%" (
    echo ERROR: No se encuentra JDK 21 en %JDK21_HOME%
    echo Descomprime el JDK 21 en C:\jdk21 e intentalo de nuevo.
    pause
    exit /b 1
)
echo JDK 21 encontrado OK
echo.
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
if exist "%DIST_DIR%"  rmdir /s /q "%DIST_DIR%"
mkdir "%BUILD_DIR%\input"
mkdir "%BUILD_DIR%\config"
copy /y "!JAR_PATH!" "%BUILD_DIR%\input\app.jar" >nul
copy /y "%~dp0config.properties" "%BUILD_DIR%\config\config.properties" >nul
echo config.properties copiado OK
set "ICO_PATH="
for /f "delims=" %%f in ('dir /b "%~dp0*.ico" 2^>nul') do (
    if "!ICO_PATH!"=="" set "ICO_PATH=%~dp0%%f"
)
if not "!ICO_PATH!"=="" (
    echo Icono encontrado: !ICO_PATH!
    set "ICON_ARGS=--icon "!ICO_PATH!""
) else (
    echo Icono: ninguno encontrado, se usara el por defecto.
    set "ICON_ARGS="
)
echo.
jpackage --type exe --name "%APP_NAME%" --app-version "%APP_VERSION%" --input "%BUILD_DIR%\input" --dest "%DIST_DIR%" --main-jar app.jar --main-class %MAIN_CLASS% --app-content "%BUILD_DIR%\config" --runtime-image "%JDK21_HOME%" --java-options "-Dapp.dir=$APPDIR" --java-options "-Dfile.encoding=UTF-8" --java-options "--enable-native-access=ALL-UNNAMED" --win-dir-chooser --win-shortcut --win-menu --win-upgrade-uuid "a1b2c3d4-e5f6-7890-abcd-ef1234567890" --vendor "AppTaxis" --description "Aplicacion de gestion de taxistas" !ICON_ARGS!
if errorlevel 1 (
    echo ERROR: jpackage ha fallado.
    echo Causa habitual: WiX Toolset 3.x no instalado.
    echo Descargalo en: https://wixtoolset.org
    rmdir /s /q "%BUILD_DIR%" 2>nul
    pause
    exit /b 1
)
rmdir /s /q "%BUILD_DIR%"
echo.
echo === LISTO ===
echo Instalador generado en: %DIST_DIR%\%APP_NAME%-%APP_VERSION%.exe
echo.
pause
endlocal