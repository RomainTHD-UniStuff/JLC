# It is assumed that the current working directory is the root, NOT the scripts subdirectory
$testPath = $args[0]
./scripts/build.ps1
./scripts/singletest.ps1 $testPath
