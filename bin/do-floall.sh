#!/bin/bash
shift 1
for file in "$@"; do
    echo FLOING $file
    bin/fir2flo.sh $file >& $file.flo-out
done
