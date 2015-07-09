#!/bin/bash -f

if [ `uname` == "Linux" ] ; then
  flags="-lm -ldl"
  platform="linux"
elif [ `uname` == "Darwin" ] ; then
  flags=""
  platform="os-x"
else 
  flags=""
  platform="unknown"
fi

stanza -e src/chipper-syntax.stanza -s chipperc.s -flags OPTIMIZE
gcc -o chipperc chipperc.s `stanza -exepaths` src/loader.c $flags
chipperc -platform $platform -path `stanza -stanzadir` -install bin/chipper

