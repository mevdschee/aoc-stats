#!/bin/bash
javac -d bin src/Retrieve.java && java -cp bin Retrieve
javac -d bin src/Analyze.java && java -cp bin Analyze
javac -d bin src/Medals.java && java -cp bin Medals
scp output/scores.tsv php_mauritsvdschee.nl@server.nlware.com:public_html/scatterplot
scp output/medals.json php_mauritsvdschee.nl@server.nlware.com:public_html/scatterplot
javac -d bin src/Private.java -cp lib/json-20171018.jar && java -cp bin:lib/json-20171018.jar Private
javac -d bin src/PMedals.java && java -cp bin PMedals
scp input/361157.json php_mauritsvdschee.nl@server.nlware.com:public_html/timeline
scp output/pmedals.json php_mauritsvdschee.nl@server.nlware.com:public_html/timeline/medals.json
