@echo off
echo ================================================
echo   Sistema de Reservas Municipales - Frontend
echo ================================================
echo.
echo Compilando proyecto...
call mvn clean compile
echo.
echo Ejecutando aplicacion JavaFX...
call mvn javafx:run
pause
