import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.Timer;
import java.util.TimerTask;

public class Crawler {
	public static void main(String argv[])
			throws IOException, ParserConfigurationException, XPathExpressionException {
//	    	
//	    	URL url = new URL("https://www.khabaronline.ir/rss");
//	    	DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//	    	InputSource src = new InputSource();
//	    	src.setByteStream(url.openStream());
//
//	    	org.w3c.dom.Document doc = builder.parse(src);
//	    	String age = doc.getElementsByTagName("link").item(5).getTextContent();
//	    	System.out.println(age);

//	    	URL url = new URL("https://www.khabaronline.ir/news/1345210/%D8%B3%D9%81%D8%B1-%DA%86%D9%86%D8%AF-%D9%87%D8%B2%D8%A7%D8%B1-%D8%AF%D9%84%D8%A7%D8%B1%DB%8C-%D9%86%D9%85%D8%A7%DB%8C%D9%86%D8%AF%DA%AF%D8%A7%D9%86-%D8%A7%DB%8C%D8%B1%D8%A7%D9%86-%D8%A8%D9%87-%D9%85%D9%82%D8%B1-AFC-%D8%A8%D8%B1%D8%A7%DB%8C-%D8%AE%D8%AF%D8%B4%D9%87-%D8%AF%D8%A7%D8%B1-%D8%B4%D8%AF%D9%86");
//	    	URLConnection uc = url.openConnection();
//	    	uc.setRequestProperty("User-Agent", "Karayel's rss reader");
//	    	DocumentBuilder builder2 = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		// InputSource src2 = new InputSource();
		// src2.setByteStream(uc.openStream());
//	    	builder2.setErrorHandler(new ErrorHandler() {
//				
//				@Override
//				public void warning(SAXParseException exception) throws SAXException {
//					// TODO Auto-generated method stub
//					
//				}
//				
//				@Override
//				public void fatalError(SAXParseException exception) throws SAXException {
//					// TODO Auto-generated method stub
//					System.out.println("123");
//				}
//				
//				@Override
//				public void error(SAXParseException exception) throws SAXException {
//					// TODO Auto-generated method stub
//					
//				}
//			});

//	    	builder2.setEntityResolver(new EntityResolver() {
//	            @Override
//	            public InputSource resolveEntity(String publicId, String systemId)
//	                    throws SAXException, IOException {
//	            	System.out.println("23" + systemId);
//	                if (systemId.contains("foo.dtd")) {
//	                    return new InputSource(new StringReader(""));
//	                } else {
//	                    return null;
//	                }
//	            }
//	        });

//	    	Document doc2 = builder2.parse("https://www.khabaronline.ir/news/1345210/%D8%B3%D9%81%D8%B1-%DA%86%D9%86%D8%AF-%D9%87%D8%B2%D8%A7%D8%B1-%D8%AF%D9%84%D8%A7%D8%B1%DB%8C-%D9%86%D9%85%D8%A7%DB%8C%D9%86%D8%AF%DA%AF%D8%A7%D9%86-%D8%A7%DB%8C%D8%B1%D8%A7%D9%86-%D8%A8%D9%87-%D9%85%D9%82%D8%B1-AFC-%D8%A8%D8%B1%D8%A7%DB%8C-%D8%AE%D8%AF%D8%B4%D9%87-%D8%AF%D8%A7%D8%B1-%D8%B4%D8%AF%D9%86");
//	    	
//	    	XPathFactory xPathfactory = XPathFactory.newInstance();
//	    	XPath xpath = xPathfactory.newXPath();
//	    	XPathExpression expr = xpath.compile("//Type[@itemprop=\"headline\"]");
//	    	NodeList nl = (NodeList) expr.evaluate(doc2, XPathConstants.NODESET);
//	    	System.out.println(nl.item(0).getTextContent());
		
		
		
		//////////////////////////////////////////////
			
//		URL url = new URL("https://www.khabaronline.ir/news/1345210/%D8%B3%D9%81%D8%B1-%DA%86%D9%86%D8%AF-%D9%87%D8%B2%D8%A7%D8%B1-%D8%AF%D9%84%D8%A7%D8%B1%DB%8C-%D9%86%D9%85%D8%A7%DB%8C%D9%86%D8%AF%DA%AF%D8%A7%D9%86-%D8%A7%DB%8C%D8%B1%D8%A7%D9%86-%D8%A8%D9%87-%D9%85%D9%82%D8%B1-AFC-%D8%A8%D8%B1%D8%A7%DB%8C-%D8%AE%D8%AF%D8%B4%D9%87-%D8%AF%D8%A7%D8%B1-%D8%B4%D8%AF%D9%86");
//		Document doc = Jsoup.parse(url,5000);
//		Elements body = doc.getElementsByAttributeValue("itemprop", "articleBody");
//		Elements image = doc.getElementsByAttributeValue("itemprop", "image");
//		Elements title = doc.getElementsByAttributeValue("itemprop", "headline");
//		Elements summary = doc.getElementsByAttributeValue("itemprop", "description");
//		Elements date = doc.getElementsByAttributeValue("itemprop", "datePublished");
//
//		System.out.println(body);
//		System.out.println("---");
//		System.out.println(image.attr("content"));
//		System.out.println("---");
//		System.out.println(title.attr("content"));
//		System.out.println("---");
//		System.out.println(summary.attr("content"));
//		System.out.println("---");
//		System.out.println(date.attr("content"));
//		System.out.println("---");
		
		new Crawler("static/websits.txt");
	}

	static int frontNum = 5;
	ArrayList<RssObject> RSSLinks;
	ArrayList<ArrayList<RssObject>> frontQueues = new ArrayList<ArrayList<RssObject>>();
	ArrayList<String> crawledLinks;
	ArrayList<String> hostsTable;
	ArrayList<ArrayList<String>> backQueues = new ArrayList<ArrayList<String>>();
	int heapIndex;

	public Crawler(String path) throws IOException {

		RSSLinks = new ArrayList<RssObject>();
		crawledLinks = new ArrayList<String>();
		hostsTable = new ArrayList<String>();
		heapIndex = 0;
		readFile(path);
		for (int i = 0; i < frontNum; i++) {
			frontQueues.add(new ArrayList<RssObject>());
		}
		prioritizer();
		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			
			@Override
			public void run() {
				prioritizer();
			}
		};
		timer.scheduleAtFixedRate( timerTask, 60*1000, 60*1000);
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
		while (true) {
			int index = heapSelect();
			if (index < 0)
				try {
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
				RSSLinks.add(new RssObject(scanner.nextLine(), 10));
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
	    	src.setByteStream(url.openStream());
	    	org.w3c.dom.Document doc = builder.parse(src);
	    	String link = "";
	    	for (int i = 1; i < doc.getElementsByTagName("link").getLength(); i++) {
		    	link = doc.getElementsByTagName("link").item(i).getTextContent();
		    	if (link == null)
		    		break;
		    	else {
		    		System.out.println(link);
		    		if (!crawledLinks.contains(link)) {
		    			links.add(link);
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
	
	private void parsUrl(String path) throws IOException {
		switch (extractHostName(path)) {
		case "www.khabaronline.ir":
			URL url = new URL(path);
			
			Document doc = Jsoup.parse(url,10000);
			Elements body = doc.getElementsByAttributeValue("itemprop", "articleBody");
			Elements image = doc.getElementsByAttributeValue("itemprop", "image");
			Elements title = doc.getElementsByAttributeValue("itemprop", "headline");
			Elements summary = doc.getElementsByAttributeValue("itemprop", "description");
			Elements date = doc.getElementsByAttributeValue("itemprop", "datePublished");

			System.out.println(body);
			System.out.println("---");
			System.out.println(image.attr("content"));
			System.out.println("---");
			System.out.println(title.attr("content"));
			System.out.println("---");
			System.out.println(summary.attr("content"));
			System.out.println("---");
			System.out.println(date.attr("content"));
			System.out.println("---");
			break;

		default:
			break;
		}

	}

}
