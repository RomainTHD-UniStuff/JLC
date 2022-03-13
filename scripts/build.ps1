# It is assumed that the current working directory is the root, NOT the scripts subdirectory
wsl.exe sh -c "export CLASSPATH=.:/usr/share/java/JLex.jar:/usr/share/java/cup.jar; make"
Write-Host "Done." -ForegroundColor Green
