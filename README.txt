-----------------------------------
INSTRUCTIONS TO COMPILE AND RUN
-----------------------------------

-----------------------------------
JAR's NEEDED:
lucene-queryparser-5.3.1.jar
lucene-analyzers-common-5.3.1.jar
lucene-core-5.3.1.jar
stanford-corenlp-3.5.2-models.jar
stanford-corenlp-3.5.2.jar
----------------------------------
TO COMPILE PROGRAM
sh compile.sh
----------------------------------
TO CLEAN PROGRAM
sh clean.sh
----------------------------------


TO RUN PROGRAM
sh run.sh <arguments>

options:

-i  								: For performing Indexing
-s  								: For performing search 
-w <wiki-docs-folder>				: specify the path of the folder containing the wiki documents. (Note:This is a compulsory field when performing indexing. Also the wiki folder should contain only the wiki documents and nothing else)
-q <questions file>					: specify questions file name after '-q'.  Example: -q "questions.txt"
-f <scoring-function>				: specify type of scoring function (tfidf or bm25) after '-f'. Example: -f "bm25"
-a <analyzer-type>					: specify analyzer type(standard, english, white) after '-a'. Example: -a "STANDARD"
-c <config-type>					: specify type of config(1 or 2) after '-c'. Example: -c 1
-l  								: specify to enable lemmatization




----------------------------------
MY CONFIGURATIONS:
Config 1 : naive indexing and searching
Config 2 : Indexing categories and searching taking categories into account
Config BEST : Configuration using English Analyzer with improved retrieval
----------------------------------


SAMPLE QUERIES:
----------------------------------
INDEXING:

To perform indexing reading files from say folder "wikipedia" using standard analyzer
sh run.sh -i -w "wikipedia" -a "standard"

To perfrom indexing using config 1 and white analyzer
sh run.sh -i -w "wikipedia" -a "white" -c 1

To perform indexing with my BEST Configuration
sh run.sh -i -w "wikipedia" -a "english" 

To performing indexing with lemmatization using standard analyzer
sh run.sh -i -w "wikipedia" -a "standard" -l


SEARCHING:

Note: You should use the same analyzer you chose while indexing.
Note: By default (without specifying) all operations are performed wiht tfidf scoring for my BEST configuration

To perform search based on questions present in questions.txt with English Analyzer
sh run.sh -s -q "questions.txt" -a "english"

To perform search based on questions present in questions.txt with Standard Analyzer that is lemmatized
sh run.sh -s -q "questions.txt" -a "standard" -l

To perform search based on questions present in questions.txt for english analyzer with bm25 scoring
sh run.sh -s -q "questions" -f "bm25" -a "english"

To perform search based on question present in questions.txt for white analyzer with bm25 scoring and for config 2
sh run.sh -s -q "questions.txt" -c 2 -a "white" -f "bm25"
----------------------------------


BASIC QUERIES:
----------------------------------
To index with my best configuration:
sh run.sh -i -w "wikipedia" -a "english"
or
sh run.sh -i -w "wikipedia"
Note: By default it is English Analyzer

To perform search on by Best Configuration:
sh run.sh -s -q "questions.txt"
or 
sh run.sh -s -q "questions.txt" -a "english"

To perform BM25 scoring on my BEST config while searching:
sh run.sh -s -q "questions.txt" -f "bm25"
-----------------------------------

IMPROVED RETRIEVAL BM25 TUNING
-----------------------------------
Note: perfrom it on the best configuration.
Note: -t enables tuned BM25

To perform search with tuned bm25 on the 20 questions testing set:
sh run.sh -s -q "questions20.txt" -f "bm25" -t

To perform search with tuned bm25 on the 80 questions set:
sh run.sh -s -q "questions80.txt" -f "bm25" -t
------------------------------------


Thank you
Shivkanth
shivkanthb@cs.arizona.edu




