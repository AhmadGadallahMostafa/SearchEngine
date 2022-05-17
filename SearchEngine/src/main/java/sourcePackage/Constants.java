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

    public static final String STOP_WORD_FILE_PATH = System.getProperty("user.dir") + "/SearchEngine/src/main/stopwords.txt";
    public static final String INDEXED_URLS_FILE_PATH = System.getProperty("user.dir") + "/SearchEngine/src/main/indexedURLs.txt";

    public static final String DOWNLOADS_DIRECTORY = System.getProperty("user.dir") + "/SearchEngine/src/main/Downloads/";

    public static final ArrayList<String> TAGS_LIST = new ArrayList<String>(Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6", "p", "title", "li", "a", "div"));
    public static Integer untitledDocsCount = 0;
}
