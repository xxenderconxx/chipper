#!/bin/bash
shift 1
for file in "$@"; do
    echo APPING "$file"
    bin/flo2app.sh $file >& $file.app-out
done
