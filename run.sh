# if [ $# -eq 15 ]
#   then
#   	java -classpath .:stanford-corenlp-3.5.2.jar:stanford-corenlp-3.5.2-models.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar CommandLine "$1" "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9" "$10" "$11" "$12"
#   else
#   java -classpath .:stanford-corenlp-3.5.2.jar:stanford-corenlp-3.5.2-models.jar Program "$1" "$2"
# fi

java -classpath .:stanford-corenlp-3.5.2.jar:stanford-corenlp-3.5.2-models.jar:lucene-analyzers-common-5.3.1.jar:lucene-core-5.3.1.jar:lucene-queryparser-5.3.1.jar CommandLine $*