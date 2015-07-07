#!/bin/bash

bin/chipper.sh $1 >& generated/$1.stz-out
generated/$1 > generated/$1.fir
