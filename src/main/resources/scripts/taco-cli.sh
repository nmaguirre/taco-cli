#!/opt/local/bin/bash

java -cp taco-cli-0.0.1-SNAPSHOT-jar-with-dependencies.jar:$2 ui.VerifyCLI $@ > /dev/null 2> /dev/null

cat verification-result.txt
