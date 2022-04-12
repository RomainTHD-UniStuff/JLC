export CLASSPATH=.:./lib/JLex.jar:./lib/cup.jar
make all || exit
mv submission.tar.gz partA-1.tar.gz
cd ./tester || exit
export CLASSPATH=.:./lib/JLex.jar:./lib/cup.jar:../lib/JLex.jar:../lib/cup.jar
python3 ./testing.py ../partA-1.tar.gz --llvm -x objects1
