package sourcePackage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import crawlercommons.filters.basic.*;

import static sourcePackage.Constants.*;

public class Crawler implements Runnable {

	private static int MAXPAGES = 5000;

	private MongoCollection<org.bson.Document> crawledDocuments;
	static int visitedPages = -1;
	private int currentPage;
	private static int crawledPages = 0;

	private int CURRENT_ID = SEED_URL_COUNT;

	static ArrayList<String> urlsList;
	private BasicURLNormalizer normalizer;
	private String normalizedString;
	Crawler (MongoCollection<org.bson.Document> crawledDocuments){
		normalizer = new BasicURLNormalizer();
		this.crawledDocuments = crawledDocuments;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			
			// crawl
			incVisitedPages();	
			// fetch URL from database
			String URL = readLinkFromList();
			// pick URL
			Document document = pickURL(URL);
			if (document == null) {
				// couldn't get document from this URL
				// skip this URL
				continue;
			}
			boolean check = writeHyperLinks(document, URL);
			if (check) {
				break;
			}
		}
	}
	
	boolean writeHyperLinks (Document document, String currentURL) {
		
		if (crawledPages >= MAXPAGES) {
			System.out.println("Crawler reached limit terminate program");
			return true;
		}
		
		Elements hyperLinks = document.select("a[href]");
		ArrayList<String> robotData = getRobotData (currentURL);
		// get the link as string from each hyperlink
		for (Element link :hyperLinks) {
			String url = link.attr("href");
			try {
				// check the string retrieved is url
				URL UR = new URL(url);
				UR.toURI();
				
				// check if the crawler reached maximum or not
				if (checkNormalizationURL(url, currentURL)) {
					if (checkRobotData(url, robotData)) {
						if (checkCrawledPages()) {
							// limit reached terminate the crawler	
							return true;
						}
						writeIntoDataBase(normalizedString);
					}
				}
				
			} catch (MalformedURLException | URISyntaxException e) {
				// TODO Auto-generated catch block
				continue;
			}
		}
		writeStateIntoDataBase(currentPage + 1);
		return false;
	}
	
	boolean checkRobotData (String url, ArrayList<String> robotData) {
		
		if (robotData == null) {
			return true;
		}
		if (robotData.contains(url)) {
			return false;
		}
		return true;
	}
	
	ArrayList<String> getRobotData (String link) {
		
		ArrayList<String> robotData = new ArrayList<String>();
		try {
			URL url= new URL(link);
			String protocol = url.getProtocol();
			String host = url.getHost();
			
			String robotLink =  protocol + "://"+ host + "/robots.txt";
			URL newURL = new URL (robotLink);
			
			try {
				BufferedReader in = new BufferedReader (new InputStreamReader (newURL.openStream()));
				String line = null;
				String disLine = null;
				while ((line = in.readLine())!= null) {
					if (line.contains("User-agent: ")) {
						line = line.replaceFirst("User-agent: ", "");
						if (line.equals( "*")) {
							while((disLine = in.readLine())!= null) {
								if (disLine.contains("Disallow: ")){
									disLine = disLine.replaceFirst("Disallow: ", "");
									robotData.add(protocol + "://"+ host + disLine);
								}
							}
							if (robotData.isEmpty()) {
								System.out.println("no robot data to retrieve");
								return null;
							}else {
								System.out.println("retrieved robot data");
								return robotData;
							}
							
						}
					}
				}
				System.out.println("no robot data to retrieve");
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Couldn't open a stream with url/robot.txt");
				return null;
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println("robot.txt is not found");
			return null;
		}
	}
	
	boolean checkNormalizationURL (String url, String currentURL) {
		
		if (currentURL.equals(url) || currentURL.equals(url + "/") || currentURL.equals(url + "#") || url.endsWith(".pdf")|| url.endsWith(".zip") || url.contains("mailto") || url.endsWith(".PDF") || url.endsWith(".ZIP")) {
			
			return false;
		}
		
		if (urlsList.contains(url)) {
			return false;
		}
		
		String normalizedURL = normalizer.filter(url);
		if (urlsList.contains(normalizedURL)) {
			return false;
		}
		normalizedString = normalizedURL;
		return true;
	}
	
	synchronized void writeIntoDataBase(String URL) {
		// TODO: PASS THE CURRENT PAGE TO THE DATABASE
		BufferedWriter output;
		try {
			output = new BufferedWriter(new FileWriter(CRAWLER_FILE_PATH, true));
			output.newLine();
			output.append(URL);
			urlsList.add(URL);
			output.close();

			// insert into database
			org.bson.Document newDoc = new org.bson.Document("url", URL);
			Integer currId = urlsList.size() + 1;
			newDoc.append("_id", currId.toString());
			crawledDocuments.insertOne(newDoc);
		} catch (IOException e) {
			System.out.println("Error in adding link to database");
		}
		
	}
	
	Document pickURL (String URL) {
		
		Connection connection = Jsoup.connect(URL);
		try {	
			Document document = connection.get();
			return document;
		} catch (IOException e) {
			System.out.println("Failed to retrieve the document from url: "+ URL);
			return null;
		}
	}
	
	synchronized boolean checkCrawledPages() {
		
		if (crawledPages >= MAXPAGES) {
			System.out.println("Crawler reached limit terminate program");
			return true;
		}
		System.out.println("crawled pages equal " + crawledPages);
		crawledPages++;
		return false;
	}
	
	synchronized void incVisitedPages() {

		visitedPages++;
		currentPage = visitedPages;	
	}
	
	String readLinkFromList()   {
		
		// TODO: check if there is data to read 
		return urlsList.get(currentPage);
	}
	
	synchronized void writeStateIntoDataBase (int value) {
		
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
