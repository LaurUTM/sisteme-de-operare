#!/bin/bash
# Compilează proiectul, face fișierul OSControlCenter.jar și îl pornește.
# Folosire:  ./run.sh
set -e
cd "$(dirname "$0")"

rm -rf out
mkdir -p out
javac -encoding UTF-8 -d out src/*.java
jar cfe OSControlCenter.jar OSControlCenter -C out .
echo "Gata: OSControlCenter.jar"
java -jar OSControlCenter.jar
