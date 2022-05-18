package sourcePackage;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;

import static sourcePackage.Constants.INVERTED_INDEX_COLLECTION;

public class DBAccessIndexer implements Runnable {

    private MongoDatabase DB;
    private HashMap<String, HashMap<String, HashMap<String, Integer>>> invertedIndex;
    private HashMap<String, ArrayList<String>> popularityMap;

    private ArrayList<String> words = new ArrayList<>();
    private ArrayList<String> ids = new ArrayList<>();

    public DBAccessIndexer(MongoDatabase db, HashMap<String, HashMap<String, HashMap<String, Integer>>> invertedIndex, HashMap<String, ArrayList<String>> popularityMap, ArrayList<String> words, ArrayList<String> ids) {
        this.DB = db;
        this.invertedIndex = invertedIndex;
        this.words = words;
        this.popularityMap = popularityMap;
        this.ids = ids;
    }

    public void run()
    {   MongoCollection<Document> invertedIndexCollection = DB.getCollection(INVERTED_INDEX_COLLECTION);
        MongoCollection<Document> popularityCollection = DB.getCollection("Popularity");
        Integer sizeInvertedIndex = invertedIndex.size();
        Integer sizePopularityMap = popularityMap.size();
        if (Thread.currentThread().getName().equals("0"))
        {
            for (int i = 0; i < sizeInvertedIndex/4; i++)
            {
                HashMap<String, HashMap<String, Integer>> firstMap = invertedIndex.get(words.get(i));
                // add the word and its value to a doc
                Document document = new Document();
                document.append(words.get(i), firstMap);
                // insert the doc to the collection
                insertDocument(document, invertedIndexCollection);
            }
            for (int i = 0; i < sizePopularityMap/4; i++)
            {
                ArrayList<String> firstList = popularityMap.get(ids.get(i));
                // add the word and its value to a doc
                Document document = new Document();
                document.append(ids.get(i), firstList);
                // insert the doc to the collection
                insertDocument(document, popularityCollection);
            }
        }
        if (Thread.currentThread().getName().equals("1"))
        {
            for (int i = sizeInvertedIndex/4; i < sizeInvertedIndex/2; i++)
            {

                HashMap<String, HashMap<String, Integer>> firstMap = invertedIndex.get(words.get(i));
                // add the word and its value to a doc
                Document document = new Document();
                document.append(words.get(i), firstMap);
                // insert the doc to the collection
                insertDocument(document, invertedIndexCollection);
            }
            for (int i = sizePopularityMap/4; i < sizePopularityMap/2; i++)
            {
                ArrayList<String> firstList = popularityMap.get(ids.get(i));
                // add the word and its value to a doc
                Document document = new Document();
                document.append(ids.get(i), firstList);
                // insert the doc to the collection
                insertDocument(document, popularityCollection);
            }
        }
        if (Thread.currentThread().getName().equals("2"))
        {
            for (int i = sizeInvertedIndex/2; i < sizeInvertedIndex/(4/3); i++)
            {

                HashMap<String, HashMap<String, Integer>> firstMap = invertedIndex.get(words.get(i));
                // add the word and its value to a doc
                Document document = new Document();
                document.append(words.get(i), firstMap);
                // insert the doc to the collection
                insertDocument(document, invertedIndexCollection);
            }
            for (int i = sizePopularityMap/2; i < sizePopularityMap/(4/3); i++)
            {
                ArrayList<String> firstList = popularityMap.get(ids.get(i));
                // add the word and its value to a doc
                Document document = new Document();
                document.append(ids.get(i), firstList);
                // insert the doc to the collection
                insertDocument(document, popularityCollection);
            }
        }
        if (Thread.currentThread().getName().equals("3"))
        {
            for (int i = sizeInvertedIndex/(4/3); i < sizeInvertedIndex; i++)
            {

                HashMap<String, HashMap<String, Integer>> firstMap = invertedIndex.get(words.get(i));
                // add the word and its value to a doc
                Document document = new Document();
                document.append(words.get(i), firstMap);
                // insert the doc to the collection
                insertDocument(document, invertedIndexCollection);
            }
            for (int i = sizePopularityMap/(4/3); i < sizePopularityMap; i++)
            {
                ArrayList<String> firstList = popularityMap.get(ids.get(i));
                // add the word and its value to a doc
                Document document = new Document();
                document.append(ids.get(i), firstList);
                // insert the doc to the collection
                insertDocument(document, popularityCollection);
            }
        }
    }

    public synchronized static void insertDocument (Document document, MongoCollection<Document> collection)
    {
        collection.insertOne(document);
    }
}
