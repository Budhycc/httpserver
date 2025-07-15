package latihan;

import com.sun.net.httpserver.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WebServer {

    private final Main main;
    private final int port;
    private final Database db = new Database();
    private boolean started = false;
    private HttpServer server;

    public WebServer(Main main, int port) {
        this.main = main;
        this.port = port;
    }

    public void initialize() throws IOException {
        HttpHandler viewHandler = exchange -> {
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
                var content = Objects.requireNonNull(in).readAllBytes();
                exchange.sendResponseHeaders(200, content.length);
                var out = exchange.getResponseBody();
                out.write(content);
                out.close();
            } catch (Exception exception) {
                main.log(exception.getMessage());
            }
        };

        HttpHandler staticHandler = exchange -> {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            var path = Path.of(exchange.getRequestURI().getPath().substring(1));
            if (!Files.exists(path)) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            var content = Files.readAllBytes(path);
            var type = Files.probeContentType(path);
            exchange.getResponseHeaders().set("Content-Type", type);
            exchange.sendResponseHeaders(200, content.length);
            var out = exchange.getResponseBody();
            out.write(content);
            out.close();
        };

        HttpHandler getHandler = exchange -> {
            var dataSiswa = db.getAllSiswa();
            var content = listToJsonArray(dataSiswa).getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, content.length);

            var out = exchange.getResponseBody();
            out.write(content);
            out.close();
        };

        HttpHandler postHandler = exchange -> {
            var body = new String(exchange.getRequestBody().readAllBytes());
            var map = new HashMap<String, String>();
            for (var field : body.split("&")) {
                String[] pair = field.split("=");
                map.put(pair[0], URLDecoder.decode(pair[1], Charset.defaultCharset()));
            }
            String[] keys = {"nis", "nama"};
            for (var key : keys) {
                if (!map.containsKey(key)) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }
            }
            var siswa = db.addSiswa(map);
            if (siswa == null) {
                exchange.sendResponseHeaders(500, -1);
                return;
            }
            var content = mapToJsonObject(siswa).getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, content.length);
            var out = exchange.getResponseBody();
            out.write(content);
            out.close();
        };

        HttpHandler putHandler = exchange -> {
            var path = exchange.getRequestURI().getPath();
            var nis = path.substring(path.lastIndexOf('/') + 1);
            var body = new String(exchange.getRequestBody().readAllBytes());
            var map = new HashMap<String, String>();
            for (var field : body.split("&")) {
                String[] pair = field.split("=");
                map.put(pair[0], URLDecoder.decode(pair[1], Charset.defaultCharset()));
            }
            var siswa = db.updateSiswa(nis, map);
            if (siswa == null) {
                exchange.sendResponseHeaders(500, -1);
                return;
            }
            var content = mapToJsonObject(siswa).getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, content.length);
            var out = exchange.getResponseBody();
            out.write(content);
            out.close();
        };

        HttpHandler deleteHandler = exchange -> {
            var path = exchange.getRequestURI().getPath();
            var nis = path.substring(path.lastIndexOf('/') + 1);
            db.deleteSiswa(nis);
            exchange.sendResponseHeaders(204, -1);
        };

        HttpContext[] contexts = new HttpContext[3];
        server = HttpServer.create(new InetSocketAddress(port), 0);
        contexts[0] = server.createContext("/", viewHandler);
        contexts[1] = server.createContext("/static", staticHandler);
        contexts[2] = server.createContext("/api/data-siswa", exchange -> {
            switch (exchange.getRequestMethod()) {
                case "GET" -> getHandler.handle(exchange);
                case "POST" -> postHandler.handle(exchange);
                case "PUT" -> putHandler.handle(exchange);
                case "DELETE" -> deleteHandler.handle(exchange);
                default -> exchange.sendResponseHeaders(405, -1);
            }
        });
        for (var context : contexts) {
            context.getFilters().add(Filter.afterHandler("Session Log", this::logSession));
        }
    }

    private void logSession(HttpExchange exchange) {
        var method = exchange.getRequestMethod();
        var path = exchange.getRequestURI().getPath();
        var code = exchange.getResponseCode();
        main.log(String.format("%s %s %s", method, path, code));
    }

    private String listToJsonArray(List<Map<String, String>> list) {
        var sb = new StringBuilder();
        sb.append("[");
        for (var i = 0; i < list.size(); i++) {
            sb.append(mapToJsonObject(list.get(i)));
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String mapToJsonObject(Map<String, String> map) {
        var sb = new StringBuilder();
        sb.append("{");
        var keys = map.keySet().toArray(new String[0]);
        for (var i = 0; i < keys.length; i++) {

            var value = map.get(keys[i]);
            sb.append(String.format("\"%s\":\"%s\"", keys[i], value));
            if (i < keys.length - 1) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
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
