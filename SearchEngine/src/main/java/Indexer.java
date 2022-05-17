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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class Indexer {
    public static final String CONNECTION_STRING = "mongodb+srv://admin:1234567890@searchenginedb.alail.mongodb.net/?retryWrites=true&w=majority";
    public static final String DATABASE_NAME = "SearchEngineDB";
    public static final String DOCUMENTS_COLLECTION = "Documents";

    public static final String STOP_WORD_FILE_PATH = System.getProperty("user.dir") + "/src/main/stopwords.txt";

    public static final String DOWNLOADS_DIRECTORY = System.getProperty("user.dir") + "/src/main/Downloads/";
    public static HashSet<String> stopWords = new HashSet<>();
    public static HashMap<String, HashMap<String, HashMap<String, Integer>>> InvertedIndex = new HashMap<>();
    public static final ArrayList<String> finalList = new ArrayList<String>(Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6", "p", "title", "li", "a", "div"));

    public static void main(String[] args) throws IOException, InterruptedException {
        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
        MongoDatabase SearchEngineDb = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> collection = SearchEngineDb.getCollection(DOCUMENTS_COLLECTION);
        // access element at collection
        downloadDocument("https://www.geeksforgeeks.org/c-sharp-class-and-object/#:~:text=A%20class%20is%20a%20user,derived%20classes%20and%20base%20classes.", "geeksforgeeks");
        downloadDocument("https://www.w3schools.com/cs/cs_classes.php", "w3s");
        stopWords = populateStopWords();
        removeStopWords("geeksforgeeks");  // Tokens has each word in the document tokenized and without stop words
        removeStopWords("w3s");
        System.out.println(InvertedIndex);
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
            // check if element is a script or style tag remove it
            if (e.tagName().equals("script") || e.tagName().equals("style")) {
                e.remove();
                continue;
            }
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

    public static void removeStopWords(String title) throws IOException {
        // read file and remove stop words
        File htmlFile = new File(DOWNLOADS_DIRECTORY + title + ".html");
        org.jsoup.nodes.Document doc = Jsoup.parse(htmlFile, "UTF-8");
        // looping over elements of the html doc and extracting words then stemming it then putting it the inverted index map
        Elements elements = doc.getAllElements();
        PorterStemmer stemmer = new PorterStemmer();
        for (Element element: elements)
        {
            // We only want to index the words in the finalList tags
            if (!finalList.contains(element.tagName()))
                continue;
            // to avoid getting nested word of elements
            String text = element.ownText();
            if (text.isEmpty())
                continue;

            // split the text into words in a seperate list
            String[] words = text.split(" ");
            for (String word : words)
            {
                String stemmedWord = stemmer.stemWord(word);
                // if the word is a stop word then skip it
                if (stopWords.contains(stemmedWord) || !word.matches("[a-zA-Z0-9]+"))
                    continue;
                // check on the map if this is a new word
                if (InvertedIndex.containsKey(stemmedWord))
                {
                    // get the first map
                    HashMap<String, HashMap<String, Integer>> firstMap = InvertedIndex.get(stemmedWord);
                    // check if the document title is in the inner map
                    if (firstMap.containsKey(title))
                    {
                        // check on the tag
                        HashMap<String, Integer> secondMap = firstMap.get(title);
                        // check if the tag has this word
                        if (secondMap.containsKey(element.tagName()))
                            secondMap.put(element.tagName(), secondMap.get(element.tagName()) + 1);
                        else
                            secondMap.put(element.tagName(), 1);                            // add the word to the tag
                    }
                    else
                    {
                        // add the document title to the map
                        HashMap<String, Integer> secondMap = new HashMap<>();
                        secondMap.put(element.tagName(), 1);
                        firstMap.put(title, secondMap);
                    }
                }
                else
                {
                    // add the word to the map
                    HashMap<String, HashMap<String, Integer>> firstMap = new HashMap<>();
                    HashMap<String, Integer> secondMap = new HashMap<>();
                    secondMap.put(element.tagName(), 1);
                    firstMap.put(title, secondMap);
                    InvertedIndex.put(stemmedWord, firstMap);
                }
            }
        }
    }

    public static HashSet<String> populateStopWords()
    {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopWords;
    }
}
