# It is assumed that the current working directory is the root, NOT the scripts subdirectory
$filename = $args[0]
wsl.exe sh -c "export CLASSPATH=.:/usr/share/java/JLex.jar:/usr/share/java/cup.jar; make all; mv submission.tar.gz $filename; cd ./tester; python3 ./testing.py ../$filename --sh"
Write-Host "Done." -ForegroundColor Green
