package sourcePackage;

import ca.rmen.porterstemmer.PorterStemmer;
import com.mongodb.DB;
import com.mongodb.client.*;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static com.mongodb.client.model.Filters.eq;
import static sourcePackage.Constants.*;

public class QueryProcessor {
    private static ArrayList<String> stemmedWords = new ArrayList<>();
    private static MongoDatabase DB;
    private static HashMap<String, HashMap<String, HashMap<String, Integer>>> currentWords;
    // define constructor

    public static void stemWords(String sentence) {
        PorterStemmer stemmer = new PorterStemmer();
        String[] words = sentence.split(" ");
        for (String word : words) {
            stemmedWords.add(stemmer.stemWord(word));
        }
    }
    // now we need to get the words stemmed and thier info from the db
    public static void getWordsInfo()
    {
        MongoCollection<Document> invertedIndex = DB.getCollection(INVERTED_INDEX_COLLECTION);
        // gett collection as has
        FindIterable<Document> iterDoc = invertedIndex.find();
        Iterator it = iterDoc.iterator();
        ArrayList<Document> docs = new ArrayList<>();
        while (it.hasNext())
            docs.add((Document) it.next());
        for (String word : stemmedWords)
        {
        }
    }
    public static void main(String[] args)
    {
        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
        DB = mongoClient.getDatabase(DATABASE_NAME);
        // get the inverted index
        stemWords("I have a disease");
        getWordsInfo();
    }
}
