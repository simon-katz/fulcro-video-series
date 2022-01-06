#!/usr/bin/env bash
set -e -x

[ ! -d "node_modules" ] && npm install
npx shadow-cljs watch main
