# It is assumed that the current working directory is the root, NOT the scripts subdirectory
$testPath = $args[0]
$fileName = "test"
wsl.exe sh -c "
export CLASSPATH=.:./lib/JLex.jar:./lib/cup.jar;
cp lib/runtime.ll tmp/$fileName.ll;
cat $testPath | ./jlc >> tmp/$fileName.ll;
cd tmp;
llc -filetype=obj $fileName.ll -o $fileName.o || exit;
clang _test.o -o $fileName || exit;
./$fileName;
"
Write-Host "Done." -ForegroundColor Green
