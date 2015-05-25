#/bin/bash -f

cd generated
flo-llvm --vcdtmp $1.flo
flo-llvm-release $1.flo --harness > $1-harness.cpp
g++ -o $1 $1-harness.cpp $1.o

