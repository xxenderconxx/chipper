#!/bin/bash -f

# stanza -e src/chipper-syntax.stanza -o chipperc -flags OPTIMIZE
# chipperc -platform os-x -path ~/bar/stanza -install chipper

stanza -e src/chipper-syntax.stanza -s chipperc.s -flags OPTIMIZE
gcc -o chipperc chipperc.s `stanza -exepaths` src/loader.c
chipperc -platform os-x -path `stanza -stanzadir` -install chipper

