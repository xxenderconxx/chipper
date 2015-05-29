#/bin/bash -f

cd generated
flo-llvm $1.flo #  --vcdtmp
flo-llvm-release $1.flo --harness > $1-harness.cpp
g++ -o $1 $1-harness.cpp $1.o

