# It is assumed that the current working directory is the root, NOT the scripts subdirectory
$testPath = $args[0]
$fileName = "test"
wsl.exe sh -c "
export CLASSPATH=.:./lib/JLex.jar:./lib/cup.jar;
cat $testPath | ./jlc > tmp/$fileName.ll;
llc -filetype=obj lib/runtime.ll -o tmp/runtime.o || exit;
cd tmp;
llc -filetype=obj $fileName.ll -o $fileName.o || exit;
clang $fileName.o runtime.o -o $fileName || exit;
timeout 1 ./$fileName;
if [ `$? != 0 ]; then
    >&2 echo Failure or interrupt.
fi
"
Write-Host "Done." -ForegroundColor Green
