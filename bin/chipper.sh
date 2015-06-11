#/bin/bash -f

chipper -i tests/$1.stanza -s generated/$1.s
gcc -o generated/$1 generated/$1.s /Users/jrb/bar/stanza/runtime/runtime.c /Users/jrb/bar/stanza/runtime/main.c src/loader.c

# chipper -i tests/$1.stanza -o generated/$1
