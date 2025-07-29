@echo off
echo Lancement de l'application Spring Boot Performance...
echo.

echo Verification de la version Java...
java -version 2>&1 | findstr "version"
if %ERRORLEVEL% NEQ 0 (
    echo Erreur: Java n'est pas installe ou n'est pas dans le PATH.
    echo Veuillez installer Java 21 ou superieur.
    goto :end
)

echo.
echo Tentative de lancement de l'application...

if exist "target\spring-boot-performance-0.0.1-SNAPSHOT.jar" (
    echo Execution du JAR...
    java -jar target\spring-boot-performance-0.0.1-SNAPSHOT.jar
) else (
    if exist "target\classes" (
        echo Execution a partir des classes compilees...
        java -cp target\classes org.isep.springbootperformance.SpringBootPerformanceApplication
    ) else (
        echo Compilation et execution avec Maven...
        if exist "mvnw.cmd" (
            call mvnw.cmd spring-boot:run
        ) else (
            mvn spring-boot:run
        )
    )
)

:end
echo.
echo Appuyez sur une touche pour fermer cette fenetre...
pause > nul