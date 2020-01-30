import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.DOMError;
//import org.w3c.dom.DOMErrorHandler;
//import org.w3c.dom.Document;
//import org.w3c.dom.NodeList;
//import org.xml.sax.EntityResolver;
//import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//import org.xml.sax.SAXException;
//import org.xml.sax.SAXParseException;
//import org.w3c.dom.Node;
//import org.w3c.dom.Element;
import java.io.File;
import com.sun.org.apache.xml.internal.security.utils.*;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.Timer;
import java.util.TimerTask;

public class Crawler {
	public static void main(String argv[])
			throws IOException, ParserConfigurationException, XPathExpressionException {
		
		new Crawler("static/websits.txt");
	}

	static int frontNum = 5;
	ArrayList<RssObject> RSSLinks;
	ArrayList<ArrayList<RssObject>> frontQueues = new ArrayList<ArrayList<RssObject>>();
	ArrayList<String> crawledLinks;
	ArrayList<String> hostsTable;
	ArrayList<ArrayList<String>> backQueues = new ArrayList<ArrayList<String>>();
	int heapIndex;
	int totalRow;
	int docNum;

	public Crawler(String path) throws IOException {

		
		RSSLinks = new ArrayList<RssObject>();
		crawledLinks = new ArrayList<String>();
		hostsTable = new ArrayList<String>();
		heapIndex = 0;
		totalRow = 0;
		docNum = 0;
		readFile(path);
		for (int i = 0; i < frontNum; i++) {
			frontQueues.add(new ArrayList<RssObject>());
		}
		prioritizer();
		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			
			@Override
			public void run() {
				System.out.println("priority: ");
				prioritizer();
			}
		};
		
		Timer timer2 = new Timer();
		TimerTask timerTask2 = new TimerTask() {
			
			@Override
			public void run() {
				System.out.println("____________write to file____________");
				docNum++;
				WriteToFile("result"+ docNum +".xls");
			}
		};
		
		while (!isAllQueuesEmpty()) {
			RssObject rssObject = FrontQueueSelector();
			ArrayList<String> extractedLinks = extractLinks(rssObject.RssLink);
			if (extractedLinks.size() <= 0)
				rssObject.interval += 1;
			else {
				rssObject.interval -= 1;
				if (rssObject.interval < 0)
					rssObject.interval = 0;
				rssObject.lastSeen = Instant.now();
				pushToBackQueue(extractedLinks);
			}
			RSSLinks.add(rssObject);
			
		}
		
		boolean firstTime = true;
		
		System.out.println("_________________________________________________________________");
		while (true) {
			int index = heapSelect();
			if (index < 0)
				try {
					if(firstTime) {
						System.out.println("______________first time ended______________");
						timer.scheduleAtFixedRate( timerTask, 60*1000, 60*1000);
						WriteToFile("result.xls");
						firstTime = false;
						timer2.scheduleAtFixedRate(timerTask2, 5*60*1000, 5*60*1000);
					}
					Thread.sleep(1000);
					while (!isAllQueuesEmpty()) {
						RssObject rssObject = FrontQueueSelector();
						ArrayList<String> extractedLinks = extractLinks(rssObject.RssLink);
						if (extractedLinks.size() <= 0)
							rssObject.interval += 1;
						else {
							rssObject.interval -= 1;
							if (rssObject.interval < 0)
								rssObject.interval = 0;
							rssObject.lastSeen = Instant.now();
							pushToBackQueue(extractedLinks);
						}
						RSSLinks.add(rssObject);
						
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else {
				String url = backQueues.get(index).remove(0);
				parsUrl(url);//TODO
				if (backQueues.get(index).size() <= 0) {
					RssObject rssObject = FrontQueueSelector();
					if (rssObject != null) {
						ArrayList<String> extractedLinks = extractLinks(rssObject.RssLink);
						if (extractedLinks.size() <= 0)
							rssObject.interval += 1;
						else {
							rssObject.interval -= 1;
							if (rssObject.interval < 0)
								rssObject.interval = 0;
							rssObject.lastSeen = Instant.now();
						}
						RSSLinks.add(rssObject);
						pushToBackQueue(extractedLinks);
					}
					if (backQueues.get(index).size() <= 0) {
						backQueues.remove(index);
						hostsTable.remove(index);
						heapIndex--;
					}
				}
				heapIndex = (heapIndex + 1);
				if (heapIndex >= backQueues.size())
					heapIndex = 0;
			}
		}
		
	}

	private void readFile(String path) {
		File file = new File(path);
		Scanner scanner;
		try {
			scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				RSSLinks.add(new RssObject(scanner.nextLine(), 1));
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void prioritizer() {
//		for (RssObject rssObject : RSSLinks) {
		for (int j = 0; j < RSSLinks.size(); j++) {
			RssObject rssObject = RSSLinks.get(j);
			if (rssObject.lastSeen.plusSeconds(rssObject.interval*60).compareTo(Instant.now()) <= 0) {
				boolean t = true;
				for (int i = 0; i < frontNum; i++) {
					if (rssObject.interval < (i + 1) * 5) {
						frontQueues.get(i).add(rssObject);
						t = false;
						break;
					}
				}
				if (t)
					frontQueues.get(frontNum - 1).add(rssObject);
				RSSLinks.remove(rssObject);
				j--;
			}
		}

//		}
	}

	private RssObject FrontQueueSelector() {
		boolean t = true;
		RssObject rssObject = null;
		while (t && !isAllQueuesEmpty()) {
			int randomQueue = randomQueueSelect();
			if (frontQueues.get(randomQueue).size() == 0)
				continue;
			else {
				t = false;
				rssObject = frontQueues.get(randomQueue).remove(0);				
			}
		}
		return rssObject;
	}
	
	private ArrayList<String> extractLinks(String path) {
    	URL url;
    	ArrayList<String> links = new ArrayList<String>();
		try {
			url = new URL(path);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    	InputSource src = new InputSource();
	    	System.out.println(path);
	    	src.setByteStream(url.openStream());
	    	org.w3c.dom.Document doc = builder.parse(src);
	    	String link = "";
	    	for (int i = 1; i < doc.getElementsByTagName("link").getLength(); i++) {
		    	link = doc.getElementsByTagName("link").item(i).getTextContent();
		    	if (link == null)
		    		break;
		    	else {
//		    		System.out.println(link);
		    		if (!crawledLinks.contains(link)) {
		    			links.add(link);
		    			crawledLinks.add(link);
		    		}
		    	}
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return links;
    	
	}
	
	private void pushToBackQueue(ArrayList<String> links) {
		String hostName = extractHostName(links.get(0));
		int index = hostsTable.indexOf(hostName);
		if (index == -1) {
			index = hostsTable.size();
			hostsTable.add(hostName);
			backQueues.add(new ArrayList<String>());
		}
		ArrayList<String> currentBackQueue = backQueues.get(index);
		for (String string : links) {
			currentBackQueue.add(string);
		}
		
	}
	
	private String extractHostName(String url) {
		int index = 0;
		if (url.contains("https://"))
			url = url.substring(8);
		else if (url.contains("http://"))
			url = url.substring(7);
		index = url.indexOf("/");
		url = url.substring(0, index);
		return url;
	}

	private boolean isAllQueuesEmpty() {
		for (ArrayList<RssObject> arr : frontQueues) {
			if (arr.size() != 0)
				return false;
		}
		return true;
	}

	private int randomQueueSelect() {
		int range = frontNum * (frontNum + 1) / 2;
		int random = (int) (Math.random() * range + 1);
		int end = frontNum;
		for (int i = 0; i < frontNum; i++) {
			if (random <= end)
				return i;
			end = end + frontNum - i;
		}
		return 0;
	}

	private int heapSelect() {
		if (backQueues.size() <= 0) {
			return -1;
		}
		else {
			return heapIndex;
		}
	}
	
	private class RssObject {

		public RssObject(String RssLink, int interval) {
			this.RssLink = RssLink;
			this.interval = interval;
			this.lastSeen = Instant.now().minusSeconds(610);
		}

		String RssLink;
		int interval;
		Instant lastSeen;
	}
	
	private void parsUrl(String path) throws UnsupportedEncodingException {
		for (int i = 0; i < path.length(); i++) {
			if (path.charAt(i) > 255) {
				path = path.substring(0, i) + URLEncoder.encode(path.charAt(i)+"", "UTF-8") + path.substring(i+1);
			}
		}
		URL url;
		try {
			url = new URL(path);
			Document doc = Jsoup.parse(url,10000);
			String pd = "";
			String t = "";
			String u = "";
			String s = "";
			String c = "";
			String th = "";
			switch (extractHostName(path)) {
			case "www.khabaronline.ir":
			{
				Elements publish_date = doc.getElementsByAttributeValue("itemprop", "datePublished");
				Elements title = doc.getElementsByAttributeValue("itemprop", "headline");
				Elements summary = doc.getElementsByAttributeValue("itemprop", "description");
				Elements content = doc.getElementsByAttributeValue("itemprop", "articleBody");
				Elements thumbnail = doc.getElementsByAttributeValue("itemprop", "image");

				pd = (publish_date.attr("content"));
				
				t = (title.attr("content"));
				
				u = "www.khabaronline.ir";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("content"));
				
			}
			break;
				
			case "www.varzesh3.com":
			{
				Elements publish_date = doc.getElementsByAttributeValue("property", "og:article:published_time");
				Elements title = doc.getElementsByAttributeValue("property", "og:title");
				Elements summary = doc.getElementsByAttributeValue("property", "og:description");
				Elements content = doc.getElementsByAttributeValue("class", "col-xs-12 news-page--news-text text-justify");
				Elements thumbnail = doc.getElementsByAttributeValue("property", "og:image");
				
				pd = (publish_date.attr("content"));
				
				t = (title.attr("content"));
				
				u = "www.varzesh3.com";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("content"));
				
			}
			break;
				
			case "www.entekhab.ir":	
			{
				Elements publish_date = doc.getElementsByAttributeValue("property", "article:published_time");
				Elements title = doc.getElementsByAttributeValue("itemprop", "name");
				Elements summary = doc.getElementsByAttributeValue("itemprop", "description");
				Elements content = doc.getElementsByAttributeValue("class", "body col-xs-36");
				Elements thumbnail = doc.getElementsByAttributeValue("itemprop", "image");

				pd = (publish_date.attr("content"));
				
				t = (title.attr("content"));
				
				u = "www.entekhab.ir";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("content"));
				
			}
			break;
			
			case "www.tabnak.ir":	
			{
				Elements publish_date = doc.getElementsByAttributeValue("property", "article:published_time");
				Elements title = doc.getElementsByAttributeValue("itemprop", "name");
				Elements summary = doc.getElementsByAttributeValue("itemprop", "description");
				Elements content = doc.getElementsByAttributeValue("class", "body");
				Elements thumbnail = doc.getElementsByAttributeValue("itemprop", "image");

				pd = (publish_date.attr("content"));
				
				t = (title.attr("content"));
				
				u = "www.tabnak.ir";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("content"));
				
			}
			break;
			
			case "www.mashreghnews.ir":
			{
				Elements publish_date = doc.getElementsByAttributeValue("itemprop", "datePublished");
				Elements title = doc.getElementsByAttributeValue("itemprop", "headline");
				Elements summary = doc.getElementsByAttributeValue("itemprop", "description");
				Elements content = doc.getElementsByAttributeValue("itemprop", "articleBody");
				Elements thumbnail = doc.getElementsByAttributeValue("itemprop", "image");

				pd = (publish_date.attr("content"));
				
				t = (title.attr("content"));
				
				u = "www.mashreghnews.ir";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("content"));
				
			}
			break;
			
			case "tn.ai":
			{
				Elements publish_date = doc.getElementsByAttributeValue("type", "application/ld+json");
				Elements title = doc.getElementsByAttributeValue("property", "og:title");
				Elements summary = doc.getElementsByAttributeValue("property", "og:description");
				Elements content = doc.getElementsByAttributeValue("class", "story");
				Elements thumbnail = doc.getElementsByAttributeValue("property", "og:image");
				
				pd = (publish_date.attr("datePublished"));
				
				t = (title.attr("content"));
				
				u = "tn.ai";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("content"));
				
			}
			break;
			
			case "www.alef.ir":
			{
				Elements publish_date = doc.getElementsByAttributeValue("property", "og:article:published_time");
				Elements title = doc.getElementsByAttributeValue("property", "og:title");
				Elements summary = doc.getElementsByAttributeValue("property", "og:description");
				Elements content = doc.getElementsByAttributeValue("class", "post-content clearfix mb-3");
				Elements thumbnail = doc.getElementsByAttributeValue("property", "og:image");
				
				pd = (publish_date.attr("content"));
				
				t = (title.attr("content"));
				
				u = "www.alef.ir";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("content"));
				
			}
			break;

			case "www.farsnews.ir":	
			{
				Elements publish_date = doc.getElementsByAttributeValue("id", "StoryPublishDate");
				Elements title = doc.getElementsByAttributeValue("itemprop", "name");
				Elements summary = doc.getElementsByAttributeValue("itemprop", "description");
				Elements content = doc.getElementsByAttributeValue("class", "body");
				Elements thumbnail = doc.getElementsByAttributeValue("itemprop", "image");

				pd = (publish_date.attr("value"));
				
				t = (title.attr("content"));
				
				u = "www.farsnews.ir";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("content"));
				
			}
			break;
			
			case "www.yjc.ir":	
			{
				Elements publish_date = doc.getElementsByAttributeValue("property", "article:published_time");
				Elements title = doc.getElementsByAttributeValue("itemprop", "name");
				Elements summary = doc.getElementsByAttributeValue("itemprop", "description");
				Elements content = doc.getElementsByAttributeValue("class", "body");
				Elements thumbnail = doc.getElementsByAttributeValue("class", "image_btn");

				pd = (publish_date.attr("content"));
				
				t = (title.attr("content"));
				
				u = "www.yjc.ir";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("src"));
				
			}
			break;
			
			case "www.isna.ir":	
			{
				Elements publish_date = doc.getElementsByAttributeValue("itemprop", "datePublished");
				Elements title = doc.getElementsByAttributeValue("itemprop", "name");
				Elements summary = doc.getElementsByAttributeValue("itemprop", "description");
				Elements content = doc.getElementsByAttributeValue("itemprop", "articleBody");
				Elements thumbnail = doc.getElementsByAttributeValue("itemprop", "image");

				pd = (publish_date.attr("content"));
				
				t = (title.attr("content"));
				
				u = "www.isna.ir";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("content"));
				
			}
			break;
			
			case "90tv.ir":
			{
//				Elements publish_date = doc.getElementsByAttributeValue("property", "og:article:published_time");
				Elements title = doc.getElementsByAttributeValue("property", "og:title");
				Elements summary = doc.getElementsByAttributeValue("property", "og:description");
				Elements content = doc.getElementsByAttributeValue("class", "c-news__body");
				Elements thumbnail = doc.getElementsByAttributeValue("class", "img-fluid");
				
//				pd = (publish_date.attr("content"));
				
				t = (title.attr("content"));
				
				u = "90tv.ir";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("src"));
				
			}
			break;
			
			case "www.mehrnews.com":	
			{
				Elements publish_date = doc.getElementsByAttributeValue("itemprop", "datePublished");
				Elements title = doc.getElementsByAttributeValue("itemprop", "name");
				Elements summary = doc.getElementsByAttributeValue("itemprop", "description");
				Elements content = doc.getElementsByAttributeValue("itemprop", "articleBody");
				Elements thumbnail = doc.getElementsByAttributeValue("itemprop", "image");

				pd = (publish_date.attr("content"));
				
				t = (title.attr("content"));
				
				u = "www.mehrnews.com";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("content"));
				
			}
			break;
			
			case "www.irna.ir":	
			{
				Elements publish_date = doc.getElementsByAttributeValue("itemprop", "datePublished");
				Elements title = doc.getElementsByAttributeValue("itemprop", "name");
				Elements summary = doc.getElementsByAttributeValue("itemprop", "description");
				Elements content = doc.getElementsByAttributeValue("itemprop", "articleBody");
				Elements thumbnail = doc.getElementsByAttributeValue("itemprop", "image");

				pd = (publish_date.attr("content"));
				
				t = (title.attr("content"));
				
				u = "www.irna.ir";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("content"));
				
			}
			break;
			
			case "www.asriran.com":	
			{
				Elements publish_date = doc.getElementsByAttributeValue("property", "article:published_time");
				Elements title = doc.getElementsByAttributeValue("itemprop", "name");
				Elements summary = doc.getElementsByAttributeValue("itemprop", "description");
				Elements content = doc.getElementsByAttributeValue("class", "body");
				Elements thumbnail = doc.getElementsByAttributeValue("class", "image_btn");

				pd = (publish_date.attr("content"));
				
				t = (title.attr("content"));
				
				u = "www.shafaf.ir";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("src"));
				
			}
			break;
			
			case "www.shafaf.ir":	
			{
				Elements publish_date = doc.getElementsByAttributeValue("property", "article:published_time");
				Elements title = doc.getElementsByAttributeValue("itemprop", "name");
				Elements summary = doc.getElementsByAttributeValue("itemprop", "description");
				Elements content = doc.getElementsByAttributeValue("class", "body");
				Elements thumbnail = doc.getElementsByAttributeValue("itemprop", "image");

				pd = (publish_date.attr("content"));
				
				t = (title.attr("content"));
				
				u = "";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("content"));
				
			}
			break;
			
			case "fararu.com":	
			{
				Elements publish_date = doc.getElementsByAttributeValue("property", "article:published_time");
				Elements title = doc.getElementsByAttributeValue("itemprop", "name");
				Elements summary = doc.getElementsByAttributeValue("itemprop", "description");
				Elements content = doc.getElementsByAttributeValue("class", "body dir-rtl");
				Elements thumbnail = doc.getElementsByAttributeValue("itemprop", "image");

				pd = (publish_date.attr("content"));
				
				t = (title.attr("content"));
				
				u = "fararu.com";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("content"));
				
			}
			break;
			
			case "www.rajanews.com":
			{
				Elements publish_date = doc.getElementsByAttributeValue("name", "dcterms.date");
				Elements title = doc.getElementsByAttributeValue("property", "og:title");
				Elements summary = doc.getElementsByAttributeValue("property", "og:description");
				Elements content = doc.getElementsByAttributeValue("class", "body");
				Elements thumbnail = doc.getElementsByAttributeValue("property", "og:image");
				
				pd = (publish_date.attr("content"));
				
				t = (title.attr("content"));
				
				u = "www.rajanews.com";
				
				s = (summary.attr("content"));
				
				c = (content).toString();
				
				th = (thumbnail.attr("content"));
				
			}
			break;
			
			default:
				break;
			}
			
			addToExcel(totalRow, 0, pd);
			addToExcel(totalRow, 1, t);
			addToExcel(totalRow, 2, u);
			addToExcel(totalRow, 3, s);
			addToExcel(totalRow, 4, c);
			addToExcel(totalRow, 5, th);
			totalRow++;
			System.out.println(totalRow);
			

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	HSSFWorkbook myWorkBook = new HSSFWorkbook();
	HSSFSheet mySheet = myWorkBook.createSheet();
	
	private void addToExcel(int row, int col, String value) {
		if (value.length() > 30000) {
			value = value.substring(0, 30000);
		}
		HSSFRow myRow = mySheet.getRow(row);

	    if (myRow == null)
	        myRow = mySheet.createRow(row);

	    HSSFCell myCell = myRow.createCell(col);
	    myCell.setCellValue(value);
	}
	
	private void WriteToFile(String dest) {
		try {
	        FileOutputStream out = new FileOutputStream(dest);
	        myWorkBook.write(out);
	        out.close();
			totalRow=0;
			myWorkBook = new HSSFWorkbook();
			mySheet = myWorkBook.createSheet();
	        System.out.println("writed");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
