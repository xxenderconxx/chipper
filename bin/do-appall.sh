#!/bin/bash
shift 1
for file in "$@"; do
    bin/flo2app.sh $file
done
