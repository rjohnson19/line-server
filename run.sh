#!/usr/bin/env bash
: ${1?"Usage: $0 /path/to/file.txt"}
java -jar target/line-server.jar --lineserver.file.path=$1