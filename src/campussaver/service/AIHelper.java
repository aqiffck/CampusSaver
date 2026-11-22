package campussaver.service;

import campussaver.exception.AIServiceException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The only class that talks to the AI (Google Gemini).
 * Reads config.txt, sends one prompt with HttpURLConnection, retries once on
 * temporary errors, returns the reply text and logs every reply to data/ai_log.txt.
 * Any failure becomes AIServiceException so callers can show a fallback.
 * JSON is built and parsed by hand with indexOf/substring (no libraries).
 */
public class AIHelper {

    private static final String CONFIG_FILE = "config.txt";
    private static final String LOG_FILE = "data/ai_log.txt";
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final int MAX_ATTEMPTS = 2;

    /** Sends the prompt and returns the AI reply text. */
    public static String ask(String prompt) throws AIServiceException {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new AIServiceException("Prompt is empty");
        }
        String[] cfg = readConfig();
        String key = cfg[0];
        String model = cfg[1];
        if (key.isEmpty() || model.isEmpty()) {
            throw new AIServiceException("AI key or model missing in config.txt");
        }

        String body = "{\"contents\":[{\"parts\":[{\"text\":\""
                + escapeJson(prompt) + "\"}]}]}";
        String lastError = "unknown error";

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                URL url = new URL(BASE_URL + model + ":generateContent");
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-Type", "application/json");
                c.setRequestProperty("x-goog-api-key", key);
                c.setDoOutput(true);
                c.setConnectTimeout(10000);
                c.setReadTimeout(20000);
                try (OutputStream os = c.getOutputStream()) {
                    os.write(body.getBytes("UTF-8"));
                }

                int code = c.getResponseCode();
                String response = readAll(code < 400 ? c.getInputStream() : c.getErrorStream());

                if (code == 200) {
                    String text = extractText(response);
                    if (text == null || text.trim().isEmpty()) {
                        throw new AIServiceException("AI returned an empty reply");
                    }
                    logReply(text);
                    return text;
                }
                lastError = "HTTP " + code;
                if (!isRetryable(code)) {
                    break;
                }
            } catch (IOException e) {
                lastError = "network error";
            }
            if (attempt < MAX_ATTEMPTS) {
                pause(2000);
            }
        }
        throw new AIServiceException("AI unavailable (" + lastError + ")");
    }

    /** True for temporary errors worth one retry. */
    private static boolean isRetryable(int code) {
        return code == 429 || code == 500 || code == 502 || code == 503 || code == 504;
    }

    private static void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Returns {key, model}. Missing file or lines give empty strings. */
    private static String[] readConfig() {
        String key = "";
        String model = "";
        try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int i = line.indexOf('=');
                if (i < 0) {
                    continue;
                }
                String k = line.substring(0, i).trim();
                String v = line.substring(i + 1).trim();
                if (k.equals("API_KEY")) {
                    key = v;
                } else if (k.equals("API_MODEL")) {
                    model = v;
                }
            }
        } catch (IOException e) {
            // no config file: caller treats empty values as "AI not configured"
        }
        return new String[] { key, model };
    }

    private static String readAll(InputStream is) throws IOException {
        if (is == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    /** Escapes text so it can sit inside a JSON string. */
    private static String escapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '"') {
                sb.append("\\\"");
            } else if (ch == '\\') {
                sb.append("\\\\");
            } else if (ch == '\n') {
                sb.append("\\n");
            } else if (ch == '\r') {
                sb.append("\\r");
            } else if (ch == '\t') {
                sb.append("\\t");
            } else if (ch < 0x20) {
                sb.append(String.format("\\u%04x", (int) ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /** Finds the first "text" value in the reply and unescapes it. Returns null if not found. */
    private static String extractText(String json) {
        int p = json.indexOf("\"text\"");
        if (p < 0) {
            return null;
        }
        p = json.indexOf(':', p);
        if (p < 0) {
            return null;
        }
        p = json.indexOf('"', p);
        if (p < 0) {
            return null;
        }
        p++;
        StringBuilder sb = new StringBuilder();
        while (p < json.length()) {
            char ch = json.charAt(p);
            if (ch == '"') {
                return sb.toString();
            }
            if (ch == '\\' && p + 1 < json.length()) {
                char n = json.charAt(p + 1);
                if (n == 'n') {
                    sb.append('\n');
                } else if (n == 't') {
                    sb.append('\t');
                } else if (n == 'r') {
                    sb.append('\r');
                } else if (n == 'u' && p + 6 <= json.length()) {
                    try {
                        sb.append((char) Integer.parseInt(json.substring(p + 2, p + 6), 16));
                        p += 6;
                        continue;
                    } catch (NumberFormatException e) {
                        sb.append('?');
                    }
                } else {
                    sb.append(n); // handles \" \\ \/
                }
                p += 2;
                continue;
            }
            sb.append(ch);
            p++;
        }
        return null;
    }

    /** Appends one line to data/ai_log.txt. Logging problems never stop the app. */
    private static void logReply(String reply) {
        try {
            new File("data").mkdirs();
            String clean = reply.replace('\n', ' ').replace('\r', ' ').replace('|', '/');
            try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                pw.println(java.time.LocalDateTime.now() + "|" + clean);
            }
        } catch (IOException e) {
            // ignore: logging is best effort
        }
    }
}