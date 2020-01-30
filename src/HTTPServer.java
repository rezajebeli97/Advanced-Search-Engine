import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import jhazm.Normalizer;
import jhazm.tokenizer.WordTokenizer;

import javax.json.JsonObject;
import javax.script.*;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.jsoup.Jsoup;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class HTTPServer {

	public static void main(String[] args) throws IOException {
		Static.searchEngine = null;
		if (Static.rankedSearch) {
			Static.searchEngine = new RankedSearch();
		} else {
			Static.searchEngine = new Array();
		}

		HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/", new LandingPageHandler());
		server.createContext("/post", new PostHandler());
		server.createContext("/json", new JSONHandler());
		server.createContext("/favicon.ico", new IgnoreHandler());

		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		System.out.println("Server started on port 8080");

		Static.searchEngine.build(Static.mainFiles, new File(Static.stopWordsFile),
				new File(Static.hamsanSazFile), new File(Static.abbreviationFile), new File(Static.tarkibiPorkarbordFile));

	}
}

class LandingPageHandler implements HttpHandler {
	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		System.out.println(exchange.getRequestURI().toString());
		if (exchange.getRequestURI().toString().equals("/")) {
			System.out.println(requestMethod + " /");
			if (requestMethod.equalsIgnoreCase("GET")) {
				exchange.getResponseHeaders().set(Constants.CONTENTTYPE, Constants.TEXTHTML);
				exchange.sendResponseHeaders(200, 0);
				OutputStream responseBody = exchange.getResponseBody();
				responseBody.write(Constants.getIndexHTML(null));
				responseBody.close();
			} else {
				new NotImplementedHandler().handle(exchange);
			}
		} else {
			int articleNumber = new Integer(exchange.getRequestURI().toString().substring(1));
			String articleHTML = String.format("<!DOCTYPE html>"
					+ "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">" + "<html>" + "<head>"
					+ "  <title>%s</title>" + "  <style>" + "    .summary{" + "      color: gray;"
					+ "      font-size: 20px;" + "    }" + "    p{" + "      font-size: 20px;" + "    }" + "    td{"
					+ "      padding-right: 20px;" + "    }" + "    .content{" + "      padding-left: 200px;"
					+ "      padding-right: 200px;" + "    }" + "    table{" + "      padding-left: 200px;"
					+ "      padding-right: 200px;" + "    }" + "        .source {" + "            color: #006621;"
					+ "            text-decoration: none;" + "        }" + "        .date {"
					+ "            color: grey;" + "            font-size: 0.75rem;" + "        }" + "  </style>"
					+ "</head>" + "<body dir=\"rtl\">" + "<table>" + "  <tr>" + "    <td>" + "      <h1>%s</h1>"
					+ "      <p class=\"summary\">%s</p>" + "      <a href=\"%s\" class=\"source\">%s</a>"
					+ "                    <p class=\"date\">%s</p>" + "    </td>" + "    <td>"
					+ "      <img src=\"%s\" width=\"auto\" height=\"220\">" + "    </td>" + "  </tr>" + "</table>"
					+ "<br>" + "<br>" + "  <div class=\"content\">%s</div>" + "</body>" + "</html>",
					Static.getRowCell(articleNumber, 1), Static.getRowCell(articleNumber, 1),
					Static.getRowCell(articleNumber, 3), Static.getRowCell(articleNumber, 2),
					Static.getRowCell(articleNumber, 2), Static.getRowCell(articleNumber, 0),
					Static.getRowCell(articleNumber, 6), Static.getRowCell(articleNumber, 5));
			System.out.println("end");
			exchange.getResponseHeaders().set(Constants.CONTENTTYPE, Constants.TEXTHTML);
			exchange.sendResponseHeaders(200, 0);
			OutputStream responseBody = exchange.getResponseBody();
			responseBody.write(articleHTML.getBytes());
			responseBody.close();

		}
	}
}

class PostHandler implements HttpHandler {
	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		System.out.println(requestMethod + " /post");
		if (requestMethod.equalsIgnoreCase("POST")) {
			String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8")).lines()
					.collect(Collectors.joining("\n"));
			System.out.println(body);
			byte ptext[] = body.getBytes();
			body = new String(ptext, "UTF-8");
			String[] parts = body.split("=");
			String name = null;
			if (parts.length > 1) {
				name = parts[1];
			}
			System.out.println(name);
			exchange.getResponseHeaders().set(Constants.CONTENTTYPE, Constants.TEXTHTML);
			exchange.sendResponseHeaders(200, 0);
			OutputStream responseBody = exchange.getResponseBody();
			responseBody.write("<a href=\"https://www.w3schools.com\"> not supported yet </a>".getBytes());
			responseBody.close();
		} else {
			new NotImplementedHandler().handle(exchange);
		}
	}
}

class JSONHandler implements HttpHandler {
	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		System.out.println(requestMethod + " /json");
		if (requestMethod.equalsIgnoreCase("POST")) {
			String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody())).lines()
					.collect(Collectors.joining("\n"));
			body = body.substring(9, body.length() - 2).replace("\\", "");
			System.out.println(body);
			////////////////////////////// enter your code here
			PostingList pstL = Static.searchEngine.search(body);
			String outputHtml = new String();
			int count = 0;
			if(pstL == null) {
				pstL = new PostingList("", new ArrayList<Article>());
			}
			ArrayList<String> tableResult = new ArrayList<String>();
			for (Article a : pstL.articles) {
				count++;
				System.out.println(a.articleNumber);

				String text = Static.getRowCell(a.articleNumber, 5);
				text = Jsoup.parse(text).text();

				text = text.replaceAll("\\p{Punct}", "");

				text = text.replaceAll("،", "");
				Normalizer normalizer = new Normalizer();
				text = normalizer.run(text);

				// converting tarkibiPorkarbord words into it's common shape
				for (String s : Static.tarkibiPorkarbord) {
					text = text.replaceAll(s, s.replace(" ", ""));
				}

				WordTokenizer tokenizer = new WordTokenizer();
				List<String> tokens = tokenizer.tokenize(text);

				String partString = "";
				for (int i = 0; i < 5 && i < a.positions.size(); i++) {
					partString += "...";
					for (int j = -3; j < 4; j++) {
						int pos = a.positions.get(i) + j;
						if (pos < 0 || pos >= tokens.size())
							continue;
						if (j == 0)
							partString += "<b>";
						partString += tokens.get(pos) + " ";
						if (j == 0)
							partString += "</b>";
					}
					partString += "...";
				}
				String res = String.format(
						"<table>" + "            <tr>" + "                <td>"
								+ "                    <img src=\"%s\" alt=\"image\" height=\"135\" width=\"135\">"
								+ "                </td>" + "                <td>" + "                    <div>"
								+ "                        <a href=\"%s\" class=\"url\">%s</a>"
								+ "                    </div>" + "                    <div class=\"urls\">"
								+ "                        <a href=\"%s\" class=\"source\">%s</a>"
								+ "                    </div>" + "                    <p>%s</p>"
								+ "                    <p class=\"date\">%s</p>" + "                </td>"
								+ "            </tr>" + "        </table>",
						Static.getRowCell(a.articleNumber, 6), "http://localhost:8080/" + a.articleNumber,
						Static.getRowCell(a.articleNumber, 1), "http://localhost:8080/" + a.articleNumber,
						Static.getRowCell(a.articleNumber, 2), partString, Static.getRowCell(a.articleNumber, 0));

//				outputHtml += res;
				tableResult.add(res.replace("\"","\\\""));

			}
			String numberOfCounts = "<h2>"+count+" articeles found</h2>";
			outputHtml = String.format("{\"header\":\"%s\", \"tables\":[\"%s\"]}",
					numberOfCounts, String.join("\",\"", tableResult));
			//////////////////////////////////////
			exchange.getResponseHeaders().set(Constants.CONTENTTYPE, Constants.APPLICATIONJSON);
			exchange.sendResponseHeaders(200, 0);
			OutputStream responseBody = exchange.getResponseBody();
			responseBody.write(outputHtml.getBytes());
			responseBody.close();
		} else {
			new NotImplementedHandler().handle(exchange);
		}
	}

	@SuppressWarnings("unchecked")
	private String addPerson(String requestBody) {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		try {
			Map<String, Object> jsonObject = (Map<String, Object>) engine
					.eval("JSON.parse(\"" + requestBody.replace("\"", "\\\"") + "\")");
			DataStore.getInstance().addPerson(jsonObject);
			return "{ \"friends\": " + DataStore.getInstance().getPeople().size() + "}";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	@SuppressWarnings("unchecked")
	private String toJSON(String requestBody) {
		
		return "";
	}
}



class NotImplementedHandler implements HttpHandler {
	public void handle(HttpExchange exchange) throws IOException {
		exchange.sendResponseHeaders(501, -1);
		exchange.close();
	}
}

class IgnoreHandler implements HttpHandler {
	public void handle(HttpExchange exchange) throws IOException {
		exchange.sendResponseHeaders(204, -1);
		exchange.close();
	}
}

class Constants {
	static final String TEXTHTML = "text/html";
	static final String APPLICATIONJSON = "application/json";
	static final String CONTENTTYPE = "Content-Type";

	static byte[] getIndexHTML(String name) throws IOException {
		if (name == null) {
			name = "Anonymous";
		}
		String content = new String(Files.readAllBytes(Paths.get("./static/index.html")));
		content = content.replace("{name}", name);
		return content.getBytes();
	}
}

class DataStore {
	private static DataStore instance = null;

	private DataStore() {
	}

	static DataStore getInstance() {
		if (instance == null) {
			instance = new DataStore();
		}
		return instance;
	}

	private List<Map<String, Object>> people = new ArrayList<>();

	void addPerson(Map<String, Object> person) {
		people.add(person);
	}

	List<Map<String, Object>> getPeople() {
		return this.people;
	}
}