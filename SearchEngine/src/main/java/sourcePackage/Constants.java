package sourcePackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public final class Constants {
    public static final String CONNECTION_STRING = "mongodb://localhost:27017";
    public static final String DATABASE_NAME = "SearchEngine";
    public static final String DOCUMENTS_COLLECTION = "Documents";
    public static final String INVERTED_INDEX_COLLECTION = "InvertedIndex";
    public static final String POPULARITY_COLLECTION = "Popularity";

    public static final String STOP_WORD_FILE_PATH = System.getProperty("user.dir") + "/src/main/stopwords.txt";

    public static final String STATE_CRAWLER_FILE_PATH = System.getProperty("user.dir") + "/src/main/state.txt";

    public static final String CRAWLER_FILE_PATH = System.getProperty("user.dir") + "/src/main/crawlerTest.txt";
    public static final String INDEXED_URLS_FILE_PATH = System.getProperty("user.dir") + "/src/main/indexedURLs.txt";

    public static final String DOWNLOADS_DIRECTORY = System.getProperty("user.dir") + "/src/main/Downloads/";

    public static final Integer SEED_URL_COUNT = 6;

    public static final ArrayList<String> TAGS_LIST = new ArrayList<String>(Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6", "p", "title", "li", "a", "div"));
    public static Integer untitledDocsCount = 0;

    public static Integer DOC_COUNT = 4;    // TODO: Change this to the number of documents in the database
}
