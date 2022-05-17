import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.mongodb.client.MongoDatabase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ca.rmen.porterstemmer.PorterStemmer;
import java.io.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class Indexer {
    public static final String CONNECTION_STRING = "mongodb+srv://admin:1234567890@searchenginedb.alail.mongodb.net/?retryWrites=true&w=majority";
    public static final String DATABASE_NAME = "SearchEngineDB";
    public static final String DOCUMENTS_COLLECTION = "Documents";

    public static final String STOP_WORD_FILE_PATH = System.getProperty("user.dir") + "/src/main/stopwords.txt";

    public static final String DOWNLOADS_DIRECTORY = System.getProperty("user.dir") + "/src/main/Downloads/";

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(STOP_WORD_FILE_PATH);
        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
        MongoDatabase SearchEngineDb = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> collection = SearchEngineDb.getCollection(DOCUMENTS_COLLECTION);
        // access element at collection
        downloadDocument("https://www.webcotube.com/", "webco");
        List<String> tokens = removeStopWords("webco");  // Tokens has each word in the document tokenized and without stop words
        System.out.println(tokens);
        tokens = stemmingTokens(tokens);    // Tokens stemmed
        System.out.println(tokens);
    }

    public static void downloadDocument(String url, String title) throws IOException, InterruptedException {
        // download document from url as html
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        File file = new File(DOWNLOADS_DIRECTORY + title + ".html");

        // write to a file.html
        PrintWriter writer = new PrintWriter(file,"UTF-8");

        // Code to remove all the inline html attributes and only keep tags
        Elements el = doc.getAllElements();
        for (Element e : el) {
            List<String> attToRemove = new ArrayList<>();
            Attributes at = e.attributes();
            for (Attribute a : at) {
                // transfer it into a list -
                // to be sure ALL data-attributes will be removed!!!
                attToRemove.add(a.getKey());
            }

            for(String att : attToRemove) {
                e.removeAttr(att);
            }
        }

        // write to file the html without attributes
        writer.write(doc.body().toString());
        writer.flush();
        writer.close();
    }

    public static List<String> removeStopWords(String title) throws IOException {
        // populate stop words in hash set
        HashSet<String> stopWords = new HashSet<>();
        File file = new File(STOP_WORD_FILE_PATH);
        try (
                BufferedReader reader = new BufferedReader(new FileReader(file));
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line.toLowerCase());      // We always compare stop words in lower case
            }
        }

        // read file and remove stop words
        File file1 = new File(DOWNLOADS_DIRECTORY + title + ".html");
        org.jsoup.nodes.Document doc = Jsoup.parse(file1, "UTF-8");     // Get the text only from the html file
        // tokenize the text into string list and remove stop words
        List<String> tokens = new ArrayList<>();
        String text = doc.body().text();
        String[] words = text.split(" ");
        for (String word : words) {
            if (!stopWords.contains(word.toLowerCase())) {
                tokens.add(word);
            }
        }

        return tokens;
    }

    public static List<String> stemmingTokens(List<String> tokens) {
        // stemming tokens using porter stemmer
        List<String> stemmedTokens = new ArrayList<>();
        PorterStemmer stemmer = new PorterStemmer();
        for (String token : tokens) {
            stemmedTokens.add(stemmer.stemWord(token.toLowerCase()));
        }
        return stemmedTokens;
    }

}
