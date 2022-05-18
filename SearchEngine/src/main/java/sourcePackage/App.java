package sourcePackage;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static sourcePackage.Constants.*;

public class App 
{
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws InterruptedException{
		// TODO Auto-generated method stub
		int numberOfCrawlers = 5;
		StringBuffer URLS = null;
		ArrayList<String> urlsList = null;
		int state = 0;
		try {
			// form an array list of urls to send to the class
			URLS = readLinks();
			urlsList = new ArrayList<String> (Arrays.asList(URLS.toString().split("\n")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// stop the thread if it couldn't connect to the database
			System.out.println("Couldnt read links from the database");
			Thread current = Thread.currentThread();
			current.stop();
		}
		
		try {
			state= readState();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Couldnt read state from the database");
			Thread current = Thread.currentThread();
			current.stop();
		}

		MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
		MongoDatabase searchEngineDb = mongoClient.getDatabase(DATABASE_NAME);
		MongoCollection<Document> crawledDocuments = searchEngineDb.getCollection(DOCUMENTS_COLLECTION);

		Thread [] crawlers = new Thread [numberOfCrawlers];
		Crawler.visitedPages = state;
		Crawler.urlsList = urlsList;
		for (int i = 0; i < urlsList.size(); i++) {
			org.bson.Document newDoc = new org.bson.Document("url", urlsList.get(i));
			Integer currId = i + 1;
			newDoc.append("_id", currId.toString());
			crawledDocuments.insertOne(newDoc);
		}
		for (int i = 0; i < numberOfCrawlers; i++) {
			crawlers[i] = new Thread (new Crawler(crawledDocuments));
			crawlers[i].start();
			
		}
		
		for (int i = 0; i < numberOfCrawlers; i++) {
			crawlers[i].join();
		}
		
		writeStateIntoDataBase(0);
	}
	
	static int readState () throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(STATE_CRAWLER_FILE_PATH));
		return scanner.nextInt();
	}
	
	
	static StringBuffer readLinks () throws IOException {
		File  file = new File(CRAWLER_FILE_PATH);
		StringBuffer URLS=new StringBuffer();
		try {
			BufferedReader br = new BufferedReader (new FileReader (file));
			String url;
			 
			while ((url = br.readLine()) != null) {
				URLS.append(url);
				URLS.append("\n");
			}
			return URLS;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	static void writeStateIntoDataBase (int value) {
		
		try {
			PrintWriter writeState = new PrintWriter (new File(STATE_CRAWLER_FILE_PATH));
			writeState.print(value);
			writeState.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Failed to save state");
		}
	}
}
