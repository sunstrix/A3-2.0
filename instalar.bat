@echo off
setlocal enabledelayedexpansion

:: Arquivo de Log
set LOGFILE=log_instalacao.txt
echo ========================================= > %LOGFILE%
echo Log de Instalacao - A3-2.0 >> %LOGFILE%
echo Data: %date% %time% >> %LOGFILE%
echo ========================================= >> %LOGFILE%

echo Iniciando verificacao e instalacao de dependencias...
echo Verificando e instalando dependencias... (Consulte %LOGFILE% para detalhes)

:: Verificar Winget (Gerenciador de pacotes nativo do Windows 10/11)
where winget >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRO] Winget nao encontrado. Instalacao automatica indisponivel. >> %LOGFILE%
    echo Winget nao encontrado. Por favor, instale as dependencias manualmente.
    pause
    exit /b 1
)

:: 1. Git
echo [1/3] Verificando Git...
where git >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Git ja esta instalado. >> %LOGFILE%
    echo [OK] Git ja esta instalado.
) else (
    echo [INSTALANDO] Git... >> %LOGFILE%
    echo [INSTALANDO] Git...
    winget install --id Git.Git -e --accept-source-agreements --accept-package-agreements >> %LOGFILE% 2>&1
    if !errorlevel! equ 0 (
        echo [SUCESSO] Git instalado com sucesso. >> %LOGFILE%
    ) else (
        echo [FALHA] Erro ao instalar Git. >> %LOGFILE%
    )
)

:: 2. Java 21 (Eclipse Temurin)
echo [2/3] Verificando Java 21...
where java >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Java ja esta instalado. >> %LOGFILE%
    echo [OK] Java ja esta instalado.
) else (
    echo [INSTALANDO] Java 21 (Eclipse Temurin)... >> %LOGFILE%
    echo [INSTALANDO] Java 21...
    winget install --id EclipseAdoptium.Temurin.21.JDK -e --accept-source-agreements --accept-package-agreements >> %LOGFILE% 2>&1
    if !errorlevel! equ 0 (
        echo [SUCESSO] Java 21 instalado com sucesso. >> %LOGFILE%
    ) else (
        echo [FALHA] Erro ao instalar Java 21. >> %LOGFILE%
    )
)

:: 3. Maven
echo [3/3] Verificando Maven...
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Maven ja esta instalado. >> %LOGFILE%
    echo [OK] Maven ja esta instalado.
) else (
    echo [INSTALANDO] Maven... >> %LOGFILE%
    echo [INSTALANDO] Maven...
    winget install --id Apache.Maven -e --accept-source-agreements --accept-package-agreements >> %LOGFILE% 2>&1
    if !errorlevel! equ 0 (
        echo [SUCESSO] Maven instalado com sucesso. >> %LOGFILE%
    ) else (
        echo [FALHA] Erro ao instalar Maven. >> %LOGFILE%
    )
)

echo.
echo =========================================
echo Processo finalizado!
echo Verifique o arquivo %LOGFILE% para mais detalhes.
echo =========================================
echo.
echo LEMBRE-SE: Reinicie o seu terminal (ou computador) para que as variaveis de ambiente (PATH) sejam atualizadas!
pause