#!/bin/bash
shift 1
for file in "$@"; do
    echo "CHIPPERING" $file
    bin/to-fir.sh $file
done
