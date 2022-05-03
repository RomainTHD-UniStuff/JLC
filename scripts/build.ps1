# It is assumed that the current working directory is the root, NOT the scripts subdirectory
wsl.exe sh -c "
export CLASSPATH=.:./lib/JLex.jar:./lib/cup.jar;
make;
"
Write-Output "Done."
