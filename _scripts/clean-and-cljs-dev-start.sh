#!/usr/bin/env bash
set -e -x

# From https://stackoverflow.com/a/246128/2148181
SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

$SCRIPT_DIR/clean.sh
$SCRIPT_DIR/cljs-dev-start.sh
