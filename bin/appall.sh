#!/bin/bash
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
$DIR/do-appall.sh `$DIR/files.sh`

