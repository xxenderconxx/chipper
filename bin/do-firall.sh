#!/bin/bash
shift 1
for file in "$@"; do
    bin/chipper.sh $file
    generated/$file > generated/$file.fir
done
