import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import javax.xml.bind.SchemaOutputResolver;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CommandLine {



    public static char programType='z';
    public static String wikiFolder="wiki";
    public static String questionsFile="questions.txt";
   // public static int modelType=1;   // 1 stands for tfidf ; 2 stands for BM25
   // public static int analyzerType = 1;  //

    public static boolean isLemmatized=false;
    public static boolean isTuned=false;
    public enum Configuration{
        ONE,
        TWO,
        BEST
    }
   static Configuration configs = Configuration.BEST;


   public enum ScoringFunction{
       TFIDF,
       BM25
   }
    static ScoringFunction scoreFunction = ScoringFunction.TFIDF;

    public enum AnalyzerType {
        ENGLISH,
        STANDARD,
        WHITE
    }
    static AnalyzerType analyzerType = AnalyzerType.ENGLISH;

    public enum Operation {
        INDEX,
        SEARCH
    }
    static Operation operation = Operation.INDEX;


    protected StanfordCoreNLP pipeline;

    // CONSTRUCTOR
    public CommandLine() {
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        this.pipeline = new StanfordCoreNLP(props);
    }

    // CORE NLP LEMMATIZARION FUNCTION.  ACCEPTS STRING -> ARRAYLIST OF LEMMATIZED WORDS
    public ArrayList<String> lemmatize(String documentText)
    {
        ArrayList<String> lemmas = new ArrayList<String>();
        // Create an empty Annotation just with the given text
        Annotation document = new Annotation(documentText);
        // run all Annotators on this text
        this.pipeline.annotate(document);
        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the
                // list of lemmas
                lemmas.add(token.get(CoreAnnotations.LemmaAnnotation.class));
            }
        }
        return lemmas;
    }

    // CONVERT ARRAYLIST OF WORDS TO A STRING
    public static StringBuilder getStringFromList(ArrayList<String> arrayList){
        StringBuilder stringedList = new StringBuilder();
        for(int i=0;i<arrayList.size();i++)
        {
            stringedList.append(arrayList.get(i));
            stringedList.append(" ");
        }
        stringedList.append(" ");
        return stringedList;
    }

    // FUNCTION TO CHECK IF A GIVEN WORD IS A STOP WORD
    public static boolean isStopWord(String word)
    {
        String[] stopWords = {"a", "about", "above", "above", "across", "after", "afterwards", "again", "against", "all", "almost", "alone", "along", "already", "also","although","always","am","among", "amongst", "amoungst", "amount",  "an", "and", "another", "any","anyhow","anyone","anything","anyway", "anywhere", "are", "around", "as",  "at", "back","be","became", "because","become","becomes", "becoming", "been", "before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "bill", "both", "bottom","but", "by", "call", "can", "cannot", "cant", "co", "con", "could", "couldnt", "cry", "de", "describe", "detail", "do", "done", "down", "due", "during", "each", "eg", "eight", "either", "eleven","else", "elsewhere", "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything", "everywhere", "except", "few", "fifteen", "fify", "fill", "find", "fire", "first", "five", "for", "former", "formerly", "forty", "found", "four", "from", "front", "full", "further", "get", "give", "go", "had", "has", "hasnt", "have", "he", "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "him", "himself", "his", "how", "however", "hundred", "ie", "if", "in", "inc", "indeed", "interest", "into", "is", "it", "its", "itself", "keep", "last", "latter", "latterly", "least", "less", "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill", "mine", "more", "moreover", "most", "mostly", "move", "much", "must", "my", "myself", "name", "namely", "neither", "never", "nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now", "nowhere", "of", "off", "often", "on", "once", "one", "only", "onto", "or", "other", "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own","part", "per", "perhaps", "please", "put", "rather", "re", "same", "see", "seem", "seemed", "seeming", "seems", "serious", "several", "she", "should", "show", "side", "since", "sincere", "six", "sixty", "so", "some", "somehow", "someone", "something", "sometime", "sometimes", "somewhere", "still", "such", "system", "take", "ten", "than", "that", "the", "their", "them", "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", "therein", "thereupon", "these", "they", "thickv", "thin", "third", "this", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "top", "toward", "towards", "twelve", "twenty", "two", "un", "under", "until", "up", "upon", "us", "very", "via", "was", "we", "well", "were", "what", "whatever", "when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose", "why", "will", "with", "within", "without", "would", "yet", "you", "your", "yours", "yourself", "yourselves", "the"};

        for(int i=0;i<stopWords.length;i++)
        {
            if(word.equals(stopWords[i]))
                return true;
        }
        return false;
    }

    // FUNCTION TO CHECK IF THE TOPHIT DOC HAS A WORD THAT IS PRESENT IN THE QUERY/QUESTION
    public static Boolean isPresent(String topHit, String question)
    {
        topHit= topHit.toLowerCase();
        String[] topHitWords = topHit.split("\\s+");
        String[] questionWords = question.split("\\s+");

        for(int m=0;m<topHitWords.length;m++)
        {
            if(isStopWord(topHitWords[m]))
                continue;
            for(int n=0;n<questionWords.length;n++)
            {
                if(topHitWords[m].equals(questionWords[n]))
                {
//                    System.out.println(questionWords[n]);
//                    System.out.println(topHitWords[m]);
                    return true;
                }
            }

        }

        return false;
    }

    // INDEXER FUNCTIONS
    // FUNCTION CALLED WHEN INDEX OPERATION IS CHOSEN
    private static void IndexWiki(String indexPath) {
        boolean create = true;
        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");

            Directory dir = FSDirectory.open(Paths.get(indexPath));

            Analyzer analyzer;
            if(analyzerType.toString().equals("STANDARD")) {
                analyzer = new StandardAnalyzer();
            }
            else if(analyzerType.toString().equals("ENGLISH")) {
                analyzer = new EnglishAnalyzer();
            }
            else if(analyzerType.toString().equals("WHITE")){
                analyzer = new WhitespaceAnalyzer();
            }
            else
                analyzer = new EnglishAnalyzer();

            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            final Path docDir = Paths.get("../");
            if (create) {
                // Create a new index in the directory, removing any
                // previously indexed documents:
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            } else {
                // Add new documents to an existing index:
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocs(writer, docDir);

            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    // FUNCTION TO GET ALL DOCUMENTS PRESENT IN THE FOLDER CONTAINING THE WIKI DOCUMENTS AND INDEX THEM ONE BY ONE
    private static void indexDocs(IndexWriter writer, Path docDir)throws IOException {

        File folder;
        if(wikiFolder!=null)
            folder = new File(wikiFolder);
        else
        {
            wikiFolder = "wiki";
            folder = new File(wikiFolder);
        }
        // read all the files present inside the folder
        File[] listOfFiles = folder.listFiles();
        // System.out.println(Arrays.toString(listOfFiles));
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                indexOneFile(writer, listOfFiles[i]);

            }
        }

    }

    // FUNCTION THAT PARSES THE CONTENT IN EACH DOC AND SENDS TITLE AND CONTENTS FOR INDEXING
    private static void indexOneFile(IndexWriter writer, File listOfFile) throws IOException {
        System.out.println("Indexing File " + listOfFile.getName());

        // String file = "sampledoc.txt";
        String file = listOfFile.getName();
        if(file.contains("DS_Store")){
            return;
        }
        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream(wikiFolder+"/"+file);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream, StandardCharsets.UTF_8));

        String strLine;

        String wikiTitle="";
        String wikiTitleFinal="";
        StringBuilder wikiContentFinal = new StringBuilder();
        Boolean wikiContentPrevFinish=false;
        StringBuilder wikiContent= new StringBuilder();
        Boolean firstTime=true;
        //Read File Line By Line
        while ((strLine = br.readLine()) != null)   {
//            System.out.println (strLine);
            CommandLine pr = new CommandLine();
            if(strLine.contains("CATEGORIES:"))
            {
                if(configs.toString().equals("ONE"))
                    continue;
            }
            if(strLine.contains("==") || strLine.startsWith("#"))
                continue;
            Pattern p = Pattern.compile("\\[\\[(.+?)\\]\\]");
            Matcher m = p.matcher(strLine);

            if(m.find()) {
//                wikiTitle = m.group(1);
                wikiContentPrevFinish = true;
                wikiContentFinal= new StringBuilder();
                wikiTitleFinal = m.group(1);
                if(wikiContentPrevFinish && !firstTime) {

                    //System.out.println("Title: "+wikiTitle);
                    //System.out.println("Content: "+wikiContent);
                    addDoc(writer, wikiTitle, wikiContent.toString());
                    wikiTitle = m.group(1);
                    wikiContent= new StringBuilder();
                }
                if(firstTime) {
                    firstTime = false;
                    wikiTitle = m.group(1);
                    //wikiContent.append(strLine);
                }
            }
            else
            {
                strLine.replaceAll("[\\-\\+!\\.\\^:,]"," ");

                if(isLemmatized)
                {
//                    LEMMATIZE WIKI CONTENT
                    strLine= getStringFromList(pr.lemmatize(strLine)).toString();
                }

                wikiContentFinal.append((strLine));
                wikiContentFinal.append(" ");
                wikiContent.append(strLine);
                wikiContent.append(" ");
            }

        }
        // add the last document present in each file
        addDoc(writer, wikiTitleFinal, wikiContentFinal.toString());

    }

    // FUNCTION TO ADD TITLE AND CONTENTS
    private static void addDoc(IndexWriter w, String title, String content) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("contents", content, Field.Store.YES));

        // use a string field for isbn because we don't want it tokenized
        doc.add(new StringField("title", title, Field.Store.YES));
        w.addDocument(doc);
    }

    // SEARCH FUNCTIONS
    // FUNCTION TO SEARCH INDEXED DOCS BASED ON THE QUESTIONS
    private static void SearchIndexedDocs(String indexPath) throws IOException, ParseException {

        Date start = new Date();

        //Searcher code
        String field = "contents";
        String queries = null;
        int repeat = 0;
        boolean raw = false;
        String queryString = null;
        int hitsPerPage = 10;

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));

        IndexSearcher searcher = new IndexSearcher(reader);
        if(scoreFunction.toString().equals("BM25"))
        {
            if(isTuned)
                searcher.setSimilarity(new BM25Similarity(1.5f,0.1f));
            else
                searcher.setSimilarity(new BM25Similarity());
        }

        Analyzer analyzer;
        if(analyzerType.toString().equals("STANDARD")) {
            analyzer = new StandardAnalyzer();
        }
        else if(analyzerType.toString().equals("ENGLISH")) {
            analyzer = new EnglishAnalyzer();
        }
        else if(analyzerType.toString().equals("WHITE")){
            analyzer = new WhitespaceAnalyzer();
        }
        else
            analyzer = new EnglishAnalyzer();

        BufferedReader in = null;
        if (queries != null) {
            in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
        } else {
            in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        }

        String file="";
        if(questionsFile!=null)
            file = questionsFile;
        else
            file = "questions.txt";

        FileInputStream fstream = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream, StandardCharsets.UTF_8));

        String strLine;
        String category="";
        String quest="";
        String ans="";
        String result="";
        int hitCount=0;
        int lineNumber=0;
        int totalCount=0;
        QueryParser parser = new QueryParser(field, analyzer);
//        QueryParser parser = (QueryParser.escape());
        while((strLine = br.readLine()) != null)   {

            if(lineNumber%4==0)
            {
                category=strLine;
                lineNumber++;
            }
            else if(lineNumber%4==1)
            {
                CommandLine pr = new CommandLine();
                ArrayList<String> lemmatizedQuest = new ArrayList<String>();
                quest=strLine;
                quest = quest.replaceAll("[\\-\\+!\\.\\^:,]"," ");
                if(configs.toString().equals("TWO") || configs.toString().equals("BEST"))
                {
                    quest = quest + " " + (category.replaceAll("[\\-\\+!\\.\\^:,]"," ")).toLowerCase();
                }

                if(isLemmatized)
                {
                    // LEMMATIZING THE QUERY
                    lemmatizedQuest = pr.lemmatize(quest);
                    quest = getStringFromList(lemmatizedQuest).toString();
                }

                Query query = parser.parse(quest);
               // System.out.println("Searching for: " + query.toString(field));
                result = getTopHit(searcher,query,quest);
                //doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);
                lineNumber++;
            }
            else if(lineNumber%4==2)
            {
                ans=strLine;
                System.out.println("Expected answer: "+ans);
                System.out.println("Result answer: "+result);
//                System.out.println(result);
                if(result.contains(ans) || ans.contains(result))
                {
                    System.out.println("CORRECT ANSWER");
                    hitCount++;
                }
                totalCount++;
                lineNumber++;
            }
            else if(lineNumber%4==3)
            {
                lineNumber++;
                // break;
            }

        }
        System.out.println("Total number of Hits: "+hitCount+" out of "+totalCount);

    }

    // FUNCTION TO GET THE TOPHIT DOCUMENT FOR THE GIVEN QUERY
    public static String getTopHit(IndexSearcher searcher, Query query, String quest) throws IOException {

        TopDocs results = searcher.search(query, 5);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        // System.out.println(numTotalHits + " total matching documents");

        if(hits.length>0) {
            Document doc;
            for(int k=0;k<hits.length;k++) {
                doc = searcher.doc(hits[k].doc);

                String title = doc.get("title");
                if (title != null) {
                    //System.out.println("   Title: " + doc.get("title"));
                    if(configs.toString().equals("BEST"))
                    {
                        if(quest.contains(doc.get("title").toString()) || doc.get("title").toString().contains(quest) || quest.indexOf(doc.get("title").toString()) > -1 || doc.get("title").toString().indexOf(quest)>-1 || isPresent(doc.get("title").toString(),quest))
                            continue;
                        else
                            return (doc.get("title").toString());
                    }
                    else
                        return (doc.get("title").toString());

                } else
                    continue;
            }
            return "no match";
        }
        else return "NO HITS";
    }

    public static int parseInput(String args[])
    {

        for(int i=0;i<args.length;i++)
        {
            if (args[i].charAt(0)=='-')
            {
                switch (args[i].charAt(1))
                {
                    case 'w':
                        wikiFolder=args[i+1];
                        break;

                    case 'q':
                        questionsFile=args[i+1];
                        break;

                    case 'l':
                        isLemmatized=true;
                        break;

                    case 'f':
                        if(args[i+1].equals("TFIDF") || args[i+1].equals("tfidf"))
                        {
                            scoreFunction= ScoringFunction.TFIDF;
                        }
                        else if(args[i+1].equals("bm25") || args[i+1].equals("BM25"))
                        {
                            scoreFunction= ScoringFunction.BM25;
                        }
                        break;

                    case 'c':
                        if(args[i+1].equals("1"))
                            configs = Configuration.ONE;
                        else if(args[i+1].equals("2"))
                            configs = Configuration.TWO;
                        else
                            configs = Configuration.BEST;
                        break;

                    case 'a':
                        if(args[i+1].equals("english") || args[i+1].equals("ENGLISH"))
                            analyzerType = AnalyzerType.ENGLISH;
                        else if(args[i+1].equals("standard") || args[i+1].equals("STANDARD"))
                            analyzerType= AnalyzerType.STANDARD;
                        else if(args[i+1].equals("white") || args[i+1].equals("WHITE"))
                            analyzerType = AnalyzerType.WHITE;
                        break;

                    case 'i':
                        operation = Operation.INDEX;
                        break;

                    case 's':
                        operation = Operation.SEARCH;
                        break;

                    case 't':
                        isTuned = true;
                        break;

                    default:
                        return 0;

                }
            }
        }
        return 1;
    }  // END OF parseInput



    public static void main(String[] args) throws IOException, ParseException
    {

        // this is your print stream, store the reference
        PrintStream err = System.err;

        // now make all writes to the System.err stream silent
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));


        if (args.length<1 || (parseInput(args)!=1) ) {
            System.out.println("Invalid options. Please see README.txt for list of options.");
            return;
        }

        System.out.println("Wiki folder: "+wikiFolder);
        System.out.println("questions file: "+questionsFile);
        System.out.println("operation: "+operation);
        System.out.println("scoring function: "+scoreFunction);
        System.out.println("analyzer: "+analyzerType);
        System.out.println("Lemmatization: "+isLemmatized);
        System.out.println("Current config: "+configs.toString());
        System.out.println();


//        String indexPath = "../index_EnglishAnalyzer1";
//        String indexPath = "../index2";
        String indexPath = "index"+"_"+analyzerType.toString()+"_"+configs.toString();

        if(operation.toString().equals("INDEX"))
            IndexWiki(indexPath);
        else if(operation.toString().equals("SEARCH"))
            SearchIndexedDocs(indexPath);


        System.setErr(err);
    }
}  // END OF CLASS
