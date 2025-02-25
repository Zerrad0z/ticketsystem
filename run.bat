@echo off
setlocal

REM Set fixed password for Oracle
set DB_PASSWORD=password

REM Check project structure
if not exist "backend" (
    echo Error: Expected project structure with 'backend' directory not found.
    exit /b 1
)
if not exist "swing-ui" (
    echo Error: Expected project structure with 'swing-ui' directory not found.
    exit /b 1
)

REM Build the parent project
echo Building the entire project...
call mvn clean package -DskipTests

REM Check if the build was successful
if %ERRORLEVEL% neq 0 (
    echo Build failed. Please check the error messages above.
    exit /b 1
)

REM Start the Docker containers
echo Starting Docker containers...
docker-compose up -d

REM Wait for the backend to start
echo Waiting for the backend to start up (45 seconds)...
timeout /t 45 /nobreak > nul

REM Run the Swing client
echo Starting Swing client...
java -jar swing-ui\target\swing-client-1.0-SNAPSHOT.jar

endlocal