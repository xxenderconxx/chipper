#!/bin/bash

(cd generated; $HOME/bar/firrtl/utils/bin/firrtl -i $1.fir -o $1.flo -X flo) # -p c # tkwTgc
(cd generated; $HOME/bar/chisel3/bin/filter < $1.flo > tmp; mv tmp $1.flo)

