package sourcePackage;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static sourcePackage.Constants.INVERTED_INDEX_COLLECTION;

public class DBAccess implements Runnable {

    private MongoDatabase DB;
    private HashMap<String, HashMap<String, HashMap<String, Integer>>> invertedIndex;

    private ArrayList<String> words = new ArrayList<>();

    public DBAccess(MongoDatabase db, HashMap<String, HashMap<String, HashMap<String, Integer>>> invertedIndex, ArrayList<String> words) {
        this.DB = db;
        this.invertedIndex = invertedIndex;
        this.words = words;

    }

    public void run()
    {   MongoCollection<Document> invertedIndexCollection = DB.getCollection(INVERTED_INDEX_COLLECTION);
        Integer sizeInvertedIndex = invertedIndex.size();
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
        }
    }

    public synchronized static void insertDocument (Document document, MongoCollection<Document> collection)
    {
        collection.insertOne(document);
    }
}
