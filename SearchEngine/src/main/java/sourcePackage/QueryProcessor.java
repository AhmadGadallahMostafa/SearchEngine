package sourcePackage;

import ca.rmen.porterstemmer.PorterStemmer;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static java.lang.Math.log;
import static sourcePackage.Constants.*;

public class QueryProcessor {
    private static ArrayList<String> stemmedWords = new ArrayList<>();
    private static MongoDatabase DB;

    private static HashMap<String, Double> docsScores = new HashMap<>();
    // define constructor

    public static void main(String[] args)
    {
        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
        DB = mongoClient.getDatabase(DATABASE_NAME);
        // get the inverted index collection and read it in map
        MongoCollection<Document> invertedIndexCollection = DB.getCollection(INVERTED_INDEX_COLLECTION);
        Bson projection = Projections.include("_id", "url", "title", "text");
        FindIterable<Document> cursor = invertedIndexCollection.find().projection(projection);

        stemWords("disease death");         //TODO: change to query
        getWordsInfo();     // takes the words and their info from the inverted index collection and calcs score of each doc
        docsScores = sortScores();  // sorts the docs by score
        System.out.println(docsScores);
    }

    public static void stemWords(String sentence) {
        PorterStemmer stemmer = new PorterStemmer();
        String[] words = sentence.split(" ");
        for (String word : words) {
            stemmedWords.add(stemmer.stemWord(word));
        }
    }
    // now we need to get the words stemmed and their info from the db
    public static void getWordsInfo()
    {
        MongoCollection<Document> invertedIndexCollection = DB.getCollection(INVERTED_INDEX_COLLECTION);

        for (String word : stemmedWords)
        {   // get the word from the db
            FindIterable<Document> cursor = invertedIndexCollection.find(exists(word));
            Document doc = cursor.first();
            if (doc == null)
                continue;

            Document currWord = (Document) doc.get(word);
            // check if the word is in any doc if not then we divide by 1 (avoid division by 0)
            Integer docContainsWordCount = 1;
            if (currWord.keySet().size() != 0)
                docContainsWordCount = (currWord.keySet().size());
            // Calculate IDF of current Word in from words in query
            Double currWordIDF = log(DOC_COUNT / docContainsWordCount);
            for (String docID : currWord.keySet())
            {
                // Calculate TF of current Doc in current Word
                Document currDoc = (Document) currWord.get(docID);
                Integer calcTFWeight = calcWeightTF(currDoc);
                Double currDocTF = calcTFWeight / currDoc.getInteger("docLength").doubleValue();
                Double currDocScore = currWordIDF * currDocTF;
                // add the doc score to the doc in the map of scores
                if (docsScores.containsKey(docID))
                {
                    docsScores.put(docID, docsScores.get(docID) + currDocScore);
                }
                else
                {
                    docsScores.put(docID, currDocScore);
                }

            }
        }
    }

    public static HashMap<String, Double> sortScores()
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Double> > list =
                new LinkedList<Map.Entry<String, Double>>(docsScores.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Double> >() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;

    }

    public static Integer calcWeightTF(Document doc)        // TODO: Refactor code of headers
    {
        Integer hFreq = 0;
        Integer liFreq = 0;
        Integer aFreq = 0;
        Integer pFreq = 0;
        Integer divFreq = 0;
        Integer titleFreq = 0;
        if(doc.getInteger("p") != null)
        {
            pFreq = doc.getInteger("p");
        }

        if(doc.getInteger("li") != null)
        {
            liFreq = doc.getInteger("li");
        }

        if(doc.getInteger("a") != null)
        {
            aFreq = doc.getInteger("a");
        }

        if(doc.getInteger("div") != null)
        {
            divFreq = doc.getInteger("div");
        }

        if(doc.getInteger("title") != null)
        {
            titleFreq = doc.getInteger("title") * 3;
        }

        if (doc.getInteger("h1") != null)
        {
            hFreq += doc.getInteger("h1");
        }

        if (doc.getInteger("h2") != null)
        {
            hFreq += doc.getInteger("h2");
        }

        if (doc.getInteger("h3") != null)
        {
            hFreq += doc.getInteger("h3");
        }

        if (doc.getInteger("h4") != null)
        {
            hFreq += doc.getInteger("h4");
        }

        if (doc.getInteger("h5") != null)
        {
            hFreq += doc.getInteger("h5");
        }

        if (doc.getInteger("h6") != null)
        {
            hFreq += doc.getInteger("h6");
        }

        hFreq = hFreq * 2;
        return pFreq + hFreq + aFreq + liFreq + divFreq + titleFreq;
    }
}
