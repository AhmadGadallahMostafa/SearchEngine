package sourcePackage;

import com.mongodb.client.*;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ca.rmen.porterstemmer.PorterStemmer;
import java.io.*;

import java.util.*;

import static sourcePackage.Constants.*;


public class Indexer {
    public static HashSet<String> stopWords = new HashSet<>();
    public static ArrayList<String> indexedURLs = new ArrayList<>();
    public static HashMap<String, HashMap<String, HashMap<String, Integer>>> invertedIndex = new HashMap<>();
    public static void main(String[] args) throws IOException, InterruptedException {
        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
        MongoDatabase searchEngineDb = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> crawledDocuments = searchEngineDb.getCollection(DOCUMENTS_COLLECTION);
        // Read the indexed URLs file
       readIndexedURLs();
        // Iterate over the documents collection
        FindIterable<Document> cursor = crawledDocuments.find();
        for (Document doc : cursor) {
            String url = doc.getString("url");
            String id = doc.getString("_id");
            if(indexedURLs.contains(url))
                continue;
            String title = downloadDocument(url);
            indexedURLs.add(url);
            generateIndex(title, id);
        }
        writeIndexedURLs();
        writeToDB(invertedIndex, searchEngineDb);

        mongoClient.close();
    }

    public static String downloadDocument(String url) throws IOException, InterruptedException {
        // download document from url as html
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        String title = doc.title();
        title = title.replaceAll("[^a-zA-Z0-9]", "");
        if (title.equals("")) {
            title = "No Title" + untitledDocsCount;
            untitledDocsCount++;
        }
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
        return title;
    }

    public static void generateIndex(String title, String id) throws IOException {
        // read file and remove stop words
        File htmlFile = new File(DOWNLOADS_DIRECTORY + title + ".html");
        org.jsoup.nodes.Document doc = Jsoup.parse(htmlFile, "UTF-8");
        // looping over elements of the html doc and extracting words then stemming it then putting it the inverted index map
        Elements elements = doc.getAllElements();
        PorterStemmer stemmer = new PorterStemmer();
        for (Element element: elements)
        {
            // We only want to index the words in the finalList tags
            if (!TAGS_LIST.contains(element.tagName()))
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
                if (invertedIndex.containsKey(stemmedWord))
                {
                    // get the first map
                    HashMap<String, HashMap<String, Integer>> firstMap = invertedIndex.get(stemmedWord);
                    // check if the document title is in the inner map
                    if (firstMap.containsKey(id))
                    {
                        // check on the tag
                        HashMap<String, Integer> secondMap = firstMap.get(id);
                        secondMap.put("localFreq", secondMap.get("localFreq") + 1);
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
                        secondMap.put("localFreq", 1);
                        secondMap.put("docLength", doc.body().text().split(" ").length);
                        secondMap.put(element.tagName(), 1);
                        firstMap.put(id, secondMap);
                    }
                }
                else
                {
                    // add the word to the map
                    HashMap<String, HashMap<String, Integer>> firstMap = new HashMap<>();
                    HashMap<String, Integer> secondMap = new HashMap<>();
                    secondMap.put(element.tagName(), 1);
                    secondMap.put("localFreq", 1);
                    secondMap.put("docLength", doc.body().text().split(" ").length);
                    firstMap.put(id, secondMap);
                    invertedIndex.put(stemmedWord, firstMap);
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
    public static void readIndexedURLs()
    {
        // read the indexed urls from the file
        File file = new File(INDEXED_URLS_FILE_PATH);
        try (
                BufferedReader reader = new BufferedReader(new FileReader(file));
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                indexedURLs.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeIndexedURLs()
    {
        // write the indexed urls to the file
        File file = new File(INDEXED_URLS_FILE_PATH);
        try (
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        ) {
            for (String url : indexedURLs) {
                writer.write(url);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeToDB(HashMap<String, HashMap<String, HashMap<String, Integer>>> invertedIndex, MongoDatabase mongoDatabase) throws IOException, InterruptedException {
        // write the inverted index to the database
        ArrayList<String> words = new ArrayList<>();
        for (String word : invertedIndex.keySet()) {
            words.add(word);
        }
        Thread t0 = new Thread(new DBAccessIndexer(mongoDatabase, invertedIndex, words));
        Thread t1 = new Thread(new DBAccessIndexer(mongoDatabase, invertedIndex, words));
        Thread t2 = new Thread(new DBAccessIndexer(mongoDatabase, invertedIndex, words));
        Thread t3 = new Thread(new DBAccessIndexer(mongoDatabase, invertedIndex, words));

        // Set the name of each thread
        t0.setName("0");
        t1.setName("1");
        t2.setName("2");
        t3.setName("3");
        // time of thread execution
        long startTime = System.currentTimeMillis();
        t0.start();
        t1.start();
        t2.start();
        t3.start();
        t0.join();
        t1.join();
        t2.join();
        t3.join();
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken to write to DB: " + (endTime - startTime) + " ms");
    }
}
