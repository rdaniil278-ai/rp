@echo off
chcp 65001 >nul
cls
echo Извлечение ВСЕХ Java и XML layout файлов из проекта Android Studio...

set OUTPUT_FILE=ProjectCode_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%.txt
set OUTPUT_FILE=%OUTPUT_FILE: =0%

REM Проверка что мы в корне проекта Android Studio
if not exist "*.kts" (
    echo ОШИБКА: Запустите batник в корне проекта Android Studio ^(где *.iml^)!
    pause
    exit /b 1
)

echo Создание файла %OUTPUT_FILE%...

> "%OUTPUT_FILE%" echo =============================================
>> "%OUTPUT_FILE%" echo       ВЕСЬ КОД ПРОЕКТА ANDROID STUDIO
>> "%OUTPUT_FILE%" echo =============================================
>> "%OUTPUT_FILE%" echo Создан: %date% %time%
>> "%OUTPUT_FILE%" echo =============================================
>> "%OUTPUT_FILE%" echo.

REM === JAVA ФАЙЛЫ ===
echo   + Поиск Java файлов...
if exist "app\src\main\java\com\example" (
    echo     Из com/example/... (ваш путь)
    for /r "app\src\main\java\com\example" %%f in (*.java) do (
        call :ProcessFile "%%f" "JAVA"
    )
) else if exist "app\src\main\java" (
    echo     Из app/src/main/java/...
    for /r "app\src\main\java" %%f in (*.java) do (
        call :ProcessFile "%%f" "JAVA"
    )
)

REM === ВСЕ XML ИЗ LAYOUT ===
echo   + Поиск ВСЕХ XML layout файлов...
if exist "app\src\main\res\layout" (
    echo     Из D:\AndroidProject\app\src\main\res\layout\...
    for /r "app\src\main\res\layout" %%f in (*.xml) do (
        call :ProcessFile "%%f" "XML LAYOUT"
    )
)

REM === AndroidManifest.xml ===
if exist "app\src\main\AndroidManifest.xml" (
    call :ProcessFile "app\src\main\AndroidManifest.xml" "MANIFEST"
)

echo.
echo Готово! Файл: %OUTPUT_FILE%
echo Размер: ~%~z1 байт
pause
goto :eof

:ProcessFile
echo. >> "%OUTPUT_FILE%"
echo ====================================== >> "%OUTPUT_FILE%"
echo ТИП: %~2 >> "%OUTPUT_FILE%"
echo ФАЙЛ: %~1 >> "%OUTPUT_FILE%"
echo ====================================== >> "%OUTPUT_FILE%"
echo. >> "%OUTPUT_FILE%"
type "%~1" >> "%OUTPUT_FILE%"
echo. >> "%OUTPUT_FILE%"
echo -------------------------------------- >> "%OUTPUT_FILE%"
echo.
goto :eof
