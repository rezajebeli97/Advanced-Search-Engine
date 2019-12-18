import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.script.*;
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
    	Static.array = new Array();
    	
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new LandingPageHandler());
        server.createContext("/post", new PostHandler());
        server.createContext("/json", new JSONHandler());
        server.createContext("/favicon.ico", new IgnoreHandler());

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Server started on port 8080" );
        
        Static.array.build(new File("News/IR-F19-Project01-Input.xls"), new File("News/stopWords.txt"), null, null);
    }
}

class LandingPageHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
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
    }
}

class PostHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        System.out.println(requestMethod + " /post");
        if (requestMethod.equalsIgnoreCase("POST")) {
            String body = new BufferedReader(
                    new InputStreamReader(
                            exchange.getRequestBody(),"UTF-8"
                    )
            ).lines().collect(Collectors.joining("\n"));
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
            String body = new BufferedReader(
                    new InputStreamReader(
                            exchange.getRequestBody()
                    )
            ).lines().collect(Collectors.joining("\n"));
            body = body.substring(9, body.length()-2).replace("\\", "");
            System.out.println(body);
            //////////////////////////////enter your code here
            PostingList pstL = Static.array.search(body);
            for (Article a : pstL.articles) {
				System.out.println(a.articleNumber);
			}
            //////////////////////////////////////
            exchange.getResponseHeaders().set(Constants.CONTENTTYPE, Constants.APPLICATIONJSON);
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();
            responseBody.write("<a href=\"https://www.w3schools.com\"> not supported yet </a>".getBytes());
            responseBody.close();
        } else {
            new NotImplementedHandler().handle(exchange);
        }
    }

    @SuppressWarnings("unchecked")
    private String addPerson(String requestBody) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            Map<String, Object> jsonObject = (Map<String, Object>)
                    engine.eval("JSON.parse(\"" + requestBody.replace("\"", "\\\"") + "\")");
            DataStore.getInstance().addPerson(jsonObject);
            return "{ \"friends\": " + DataStore.getInstance().getPeople().size() + "}";
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        String content = new String(
                Files.readAllBytes(Paths.get("./static/index.html"))
        );
        content = content.replace("{name}", name);
        return content.getBytes();
    }
}

class DataStore {
    private static DataStore instance = null;
    private DataStore() {}
    static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }
    private List<Map<String, Object>> people = new ArrayList<>();
    void addPerson(Map<String, Object> person) { people.add(person); }
    List<Map<String, Object>> getPeople() { return this.people; }
}