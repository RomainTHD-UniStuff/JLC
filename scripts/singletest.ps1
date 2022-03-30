# It is assumed that the current working directory is the root, NOT the scripts subdirectory
$testPath = $args[0]
$fileName = "test"
wsl.exe sh -c "
export CLASSPATH=.:./lib/JLex.jar:./lib/cup.jar;
cp lib/runtime.ll tmp/$fileName.ll;
cat $testPath | ./jlc >> tmp/$fileName.ll;
cd tmp;
llc -filetype=obj $fileName.ll -o $fileName.o || exit;
clang $fileName.o -o $fileName || exit;
timeout 1 ./$fileName;
if [ $? != 0 ]; then
    >&2 echo Failure or interrupt.
fi
"
Write-Host "Done." -ForegroundColor Green
