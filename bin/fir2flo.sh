#!/bin/bash

cd generated
$HOME/bar/firrtl/utils/bin/firrtl -i $1.fir -o $1.flo -x X # -p c # tkwTgc
$HOME/bar/chisel3/bin/filter < $1.flo > tmp; mv tmp $1.flo
