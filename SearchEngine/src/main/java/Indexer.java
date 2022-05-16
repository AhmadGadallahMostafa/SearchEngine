import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.ConnectionString;
import com.mongodb.ServerAddress;
import com.mongodb.MongoCredential;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.mongodb.client.MongoDatabase;
import org.jsoup.Jsoup;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.net.UnknownHostException;



public class Indexer {
    public static final String CONNECTION_STRING = "mongodb+srv://admin:1234567890@searchenginedb.alail.mongodb.net/?retryWrites=true&w=majority";
    public static final String DATABASE_NAME = "SearchEngineDB";
    public static final String DOCUMENTS_COLLECTION = "Documents";

    public static void main(String[] args) throws IOException, InterruptedException {
        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
        MongoDatabase SearchEngineDb = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> collection = SearchEngineDb.getCollection(DOCUMENTS_COLLECTION);
        // access element at collection
        downloadDocument("https://www.google.com", "google");
        removeStopWords("google");

    }

    public static void downloadDocument(String url, String title) throws IOException, InterruptedException {
        // download document from url as html
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET() // GET is default
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        URL url1 = new URL(url);
        File file = new File( title + ".html");
        // write to a file.html
        try(
                BufferedReader reader = new BufferedReader(new InputStreamReader(url1.openStream()));
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
            }
        }
    }

    public static void removeStopWords(String title) throws IOException {
        // remove stop words from the file
        File file = new File(title + ".html");
        // read file into a string
        StringBuilder fileContents = new StringBuilder((int)file.length());
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileContents.append(line);
                fileContents.append(System.lineSeparator());
            }
        }
        // remove stop words from the string
        
    }
}
