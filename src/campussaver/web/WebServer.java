package campussaver.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

/** Tiny local web server: serves files from web/ and the /api/... endpoints. */
public class WebServer {

    private static final int PORT = 8080;
    private static final String WEB_DIR = "web";
    private HttpServer server;

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
        server.createContext("/api/ping", ex -> {
            try {
                send(ex, 200, "application/json; charset=utf-8", "{\"status\":\"ok\"}");
            } catch (Exception e) {
                send(ex, 500, "application/json; charset=utf-8", "{\"error\":\"server error\"}");
            }
        });
        // more endpoints go here: /api/budget, /api/tips, /api/survival, ...
        server.createContext("/", this::serveStatic);
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void serveStatic(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            File root = new File(WEB_DIR).getCanonicalFile();
            File f = new File(WEB_DIR + path).getCanonicalFile();
            if (path.contains("..") || !f.getPath().startsWith(root.getPath()) || !f.isFile()) {
                send(ex, 404, "text/plain; charset=utf-8", "Not found");
                return;
            }
            byte[] data = Files.readAllBytes(f.toPath());
            ex.getResponseHeaders().set("Content-Type", mime(path));
            ex.sendResponseHeaders(200, data.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(data);
            }
        } catch (Exception e) {
            send(ex, 500, "text/plain; charset=utf-8", "Server error");
        }
    }

    private static void send(HttpExchange ex, int code, String type, String body) throws IOException {
        byte[] data = body.getBytes("UTF-8");
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(code, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }

    private static String mime(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".json")) return "application/json; charset=utf-8";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    public static void main(String[] args) throws IOException {
        new WebServer().start();
        System.out.println("CampusSaver running at http://localhost:" + PORT);
    }
}