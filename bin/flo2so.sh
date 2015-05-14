#/bin/bash -f

cd generated
flo-llvm --vcdtmp $1.flo
gen-harness $1 > lib$1.cpp
g++ -c -fPIC lib$1.cpp -o lib$1.o
g++ -shared -o lib$1.so -fPIC lib$1.o $1.o

