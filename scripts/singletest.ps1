# It is assumed that the current working directory is the root, NOT the scripts subdirectory
$testPath = $args[0]
$fileName = "test"
wsl.exe sh -c "
export CLASSPATH=.:./lib/JLex.jar:./lib/cup.jar;
./jlc $testPath -o tmp/$fileName.ll || exit;
llc -filetype=obj lib/runtime.ll -o tmp/runtime.o || exit;
cd tmp;
llc -filetype=obj $fileName.ll -o $fileName.o || exit;
clang $fileName.o runtime.o -o $fileName || exit;
timeout 3 ./$fileName || >&2 echo Failure or interrupt.
"
Write-Output "Done."
