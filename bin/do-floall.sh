#!/bin/bash
shift 1
for file in "$@"; do
    bin/fir2flo.sh $file
done
