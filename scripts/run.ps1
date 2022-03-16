# It is assumed that the current working directory is the root, NOT the scripts subdirectory
$testPath = $args[0]
wsl.exe sh -c "export CLASSPATH=.:/usr/share/java/JLex.jar:/usr/share/java/cup.jar; ./jlc $testPath"
