package campussaver.web;

import campussaver.model.BudgetPlan;
import campussaver.model.StudentUser;
import campussaver.service.BudgetService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
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
                server.createContext("/api/budget", this::handleBudget);
        // more endpoints go here: /api/tips, /api/survival, ...
        server.createContext("/", this::serveStatic);
        server.start();
    } 

    private void handleBudget(HttpExchange ex) throws IOException {
        try {
            String q = ex.getRequestURI().getRawQuery();
            String living = param(q, "living", StudentUser.HOSTEL);
            if (StudentUser.COMMUTER.equalsIgnoreCase(living)) living = StudentUser.COMMUTER;
            else living = StudentUser.HOSTEL;
            boolean scholarship = "true".equalsIgnoreCase(param(q, "scholarship", "false"));
            long budget;
            try {
                budget = Long.parseLong(param(q, "budget", "100000"));
            } catch (NumberFormatException e) {
                budget = -1;
            }
            if (budget <= 0) {
                send(ex, 400, "application/json; charset=utf-8", "{\"error\":\"budget must be a positive number of cents\"}");
                return;
            }
            StudentUser u = new StudentUser("TEMP", param(q, "name", "Student"),
                    param(q, "currency", "MYR"), living, scholarship, budget);
            BudgetPlan p = BudgetService.buildFromAI(u);
            StringBuilder sb = new StringBuilder("{\"categories\":[");
            for (int i = 0; i < p.getCount(); i++) {
                if (i > 0) sb.append(",");
                sb.append("{\"name\":\"").append(esc(p.getCategory(i)))
                  .append("\",\"percent\":").append(p.getPercent(i)).append("}");
            }
            sb.append("]}");
            send(ex, 200, "application/json; charset=utf-8", sb.toString());
        } catch (Exception e) {
            send(ex, 500, "application/json; charset=utf-8", "{\"error\":\"server error\"}");
        }
    }

    /** Reads one value from a raw query string; returns def if missing or blank. */
    private static String param(String query, String key, String def) {
        if (query == null) return def;
        String[] pairs = query.split("&");
        for (int i = 0; i < pairs.length; i++) {
            int eq = pairs[i].indexOf('=');
            if (eq > 0 && pairs[i].substring(0, eq).equals(key)) {
                try {
                    String v = URLDecoder.decode(pairs[i].substring(eq + 1), "UTF-8").trim();
                    return v.isEmpty() ? def : v;
                } catch (Exception e) {
                    return def;
                }
            }
        }
        return def;
    }

    private static String esc(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') sb.append('\\').append(c);
            else if (c < 0x20) sb.append(' ');
            else sb.append(c);
        }
        return sb.toString();
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