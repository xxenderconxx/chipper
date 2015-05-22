#!/bin/bash -f

stanza -e src/chipper-syntax.stanza -o chipperc -flags OPTIMIZE
chipperc -platform os-x -path ~/bar/stanza-382 -install chipper
