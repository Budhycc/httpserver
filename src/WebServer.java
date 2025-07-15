package latihan;

import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDate;

public class WebServer {

    private final Main main;
    private final int port;
    private final Database db = new Database();
    private boolean started = false;
    private HttpServer server;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    public WebServer(Main main, int port) {
        this.main = main;
        this.port = port;
    }

    public void initialize() throws IOException {
        HttpHandler staticHandler = exchange -> {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            var path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path += "index.html";
            }
            path = path.substring(1);
            try (var in = getClass().getResourceAsStream(path)) {
                var content = in.readAllBytes();
                exchange.sendResponseHeaders(200, content.length);
                var out = exchange.getResponseBody();
                out.write(content);
                out.close();
            } catch (Exception exception) {
                main.log(exception.getMessage());
            }
        };

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", staticHandler);
        server.createContext("/api/categories", this::handleCategories);
        server.createContext("/api/transactions", this::handleTransactions);
        server.createContext("/api/reports", this::handleReports);
    }

    private void handleReports(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        var query = exchange.getRequestURI().getQuery();
        var params = query.split("=");
        if (params.length != 2 || !params[0].equals("type")) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        var type = params[1];
        Object reportData;
        switch (type) {
            case "daily" -> reportData = db.getDailyReport();
            case "weekly" -> reportData = db.getWeeklyReport();
            case "monthly" -> reportData = db.getMonthlyReport();
            case "yearly" -> reportData = db.getYearlyReport();
            default -> {
                exchange.sendResponseHeaders(400, -1);
                return;
            }
        }

        var json = gson.toJson(reportData);
        sendResponse(exchange, 200, json);
    }

    private void handleCategories(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            var categories = db.getAllCategories();
            var json = gson.toJson(categories);
            sendResponse(exchange, 200, json);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleTransactions(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET" -> {
                var transactions = db.getAllTransactions();
                var json = gson.toJson(transactions);
                sendResponse(exchange, 200, json);
            }
            case "POST" -> {
                var transaction = gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), Transaction.class);
                var newTransaction = db.addTransaction(transaction);
                var json = gson.toJson(newTransaction);
                sendResponse(exchange, 201, json);
            }
            case "PUT" -> {
                var transaction = gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), Transaction.class);
                var updatedTransaction = db.updateTransaction(transaction);
                var json = gson.toJson(updatedTransaction);
                sendResponse(exchange, 200, json);
            }
            case "DELETE" -> {
                var id = Integer.parseInt(exchange.getRequestURI().getPath().substring(exchange.getRequestURI().getPath().lastIndexOf('/') + 1));
                db.deleteTransaction(id);
                exchange.sendResponseHeaders(204, -1);
            }
            default -> exchange.sendResponseHeaders(405, -1);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        var out = exchange.getResponseBody();
        out.write(response.getBytes());
        out.close();
    }

    public void start() {
        server.start();
        started = true;
    }

    public void stop() {
        server.stop(3);
        started = false;
    }

    public boolean isStarted() {
        return started;
    }
}
