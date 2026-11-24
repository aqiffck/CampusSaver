package campussaver.service;

import campussaver.exception.RateFetchException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/** Currency conversion using data/rates.txt. Rates are 1 MYR in each currency, stored x10000. */
public class CurrencyService {
    private static final String FILE = "data/rates.txt";
    private String[] codes = new String[20];
    private long[] rates = new long[20];   // scaled by 10000, so 0.2100 is stored as 2100
    private int count = 0;

    /** Loads rates.txt, or creates default placeholder rates (admin should update them). */
    public void loadRates() {
        count = 0;
        File f = new File(FILE);
        if (!f.exists()) {
            addRate("MYR", 10000);
            addRate("USD", 2100);
            addRate("SGD", 2900);
            saveRates();
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length < 2) continue;
                long r = parseRate(p[1]);
                if (r > 0) addRate(p[0].trim().toUpperCase(), r);
            }
        } catch (IOException e) {
            // unreadable rates file: keep whatever was loaded; MYR is added below
        }
        if (indexOf("MYR") < 0) addRate("MYR", 10000);
    }

    /** Converts cents between currencies using integer maths. Unknown currency returns cents unchanged. */
    public long convert(long cents, String from, String to) {
        int a = indexOf(from);
        int b = indexOf(to);
        if (a < 0 || b < 0) return cents;
        return (cents * rates[b] + rates[a] / 2) / rates[a];
    }

    /** Admin updates one rate (e.g. "USD", "0.2150"). Returns false if invalid. */
    public boolean setRate(String code, String rateText) {
        long r = parseRate(rateText);
        if (r <= 0 || code == null) return false;
        String c = code.trim().toUpperCase();
        if (c.equals("MYR")) return false;  // base currency stays 1.0000
        int i = indexOf(c);
        if (i >= 0) rates[i] = r;
        else if (!addRate(c, r)) return false;
        saveRates();
        return true;
    }

    public String[] getCodes() {
        String[] out = new String[count];
        for (int i = 0; i < count; i++) out[i] = codes[i];
        return out;
    }

    public int getCount() { return count; }

    // ---------- F3: live rates (fallback = rates.txt) ----------
    private static final String RATES_URL = "https://open.er-api.com/v6/latest/MYR";
    private String lastRefreshMessage = "Rates not refreshed yet.";

    public String getLastRefreshMessage() { return lastRefreshMessage; }

    /** Tries live rates. Never throws: on any failure the current rates (from rates.txt) stay. */
    public boolean refreshLiveRates() {
        try {
            int updated = fetchLiveRates();
            lastRefreshMessage = "Live rates updated (" + updated + " currencies).";
            return true;
        } catch (RateFetchException e) {
            lastRefreshMessage = "Live rates unavailable (" + e.getMessage() + "). Using saved rates.txt.";
            return false;
        }
    }

    /** Updates the currencies already in the list from the live source and saves rates.txt. Returns how many changed. */
    public int fetchLiveRates() throws RateFetchException {
        String json = download().replaceAll("\\s", "");
        if (json.indexOf("\"result\":\"success\"") < 0) throw new RateFetchException("source reported an error");
        int start = json.indexOf("\"rates\"");
        start = start < 0 ? -1 : json.indexOf('{', start);
        int end = start < 0 ? -1 : json.indexOf('}', start);
        if (start < 0 || end < 0) throw new RateFetchException("unexpected reply");

        long[] fresh = new long[count];          // 0 = not found in the reply
        String[] pairs = json.substring(start + 1, end).split(",");
        for (int i = 0; i < pairs.length; i++) {
            String[] kv = pairs[i].split(":");
            if (kv.length != 2) continue;
            String code = kv[0].replace("\"", "").toUpperCase();
            int idx = indexOf(code);
            if (idx < 0 || code.equals("MYR")) continue;
            long r = parseLiveRate(kv[1]);
            if (r > 0) fresh[idx] = r;
        }
        int updated = 0;
        for (int i = 0; i < count; i++) {
            if (fresh[i] > 0) { rates[i] = fresh[i]; updated++; }
        }
        if (updated == 0) throw new RateFetchException("no usable rates in reply");
        saveRates();
        return updated;
    }

    private String download() throws RateFetchException {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(RATES_URL).openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("User-Agent", "CampusSaver/1.0");
            c.setConnectTimeout(8000);
            c.setReadTimeout(8000);
            int code = c.getResponseCode();
            if (code != 200) throw new RateFetchException("HTTP " + code);
            StringBuilder sb = new StringBuilder();
            try (InputStream is = c.getInputStream();
                 BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                String line;
                while ((line = r.readLine()) != null) sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RateFetchException("network error");
        }
    }

    /** "0.23617" -> 2362 (rounded to 4 decimals). Returns -1 if invalid, e.g. scientific notation. */
    private long parseLiveRate(String text) {
        String t = text.trim();
        if (!t.matches("\\d{1,6}(\\.\\d+)?")) return -1;
        int dot = t.indexOf('.');
        String whole = dot < 0 ? t : t.substring(0, dot);
        String frac = dot < 0 ? "" : t.substring(dot + 1);
        boolean roundUp = frac.length() > 4 && frac.charAt(4) >= '5';
        while (frac.length() < 4) frac = frac + "0";
        long r = Long.parseLong(whole) * 10000 + Long.parseLong(frac.substring(0, 4));
        return roundUp ? r + 1 : r;
    }

    private boolean addRate(String code, long rate) {
        if (count >= codes.length || code.length() != 3) return false;
        codes[count] = code;
        rates[count] = rate;
        count++;
        return true;
    }

    private int indexOf(String code) {
        if (code == null) return -1;
        for (int i = 0; i < count; i++) {
            if (codes[i].equalsIgnoreCase(code.trim())) return i;
        }
        return -1;
    }

    /** "0.2100" -> 2100. Returns -1 if invalid. */
    private long parseRate(String text) {
        if (text == null) return -1;
        String t = text.trim();
        if (!t.matches("\\d{1,5}(\\.\\d{1,4})?")) return -1;
        int dot = t.indexOf('.');
        String whole = dot < 0 ? t : t.substring(0, dot);
        String frac = dot < 0 ? "" : t.substring(dot + 1);
        while (frac.length() < 4) frac = frac + "0";
        return Long.parseLong(whole) * 10000 + Long.parseLong(frac);
    }

    private void saveRates() {
        new File("data").mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (int i = 0; i < count; i++) {
                long r = rates[i];
                String frac = String.valueOf(r % 10000);
                while (frac.length() < 4) frac = "0" + frac;
                pw.println(codes[i] + "|" + (r / 10000) + "." + frac);
            }
        } catch (IOException e) {
            // saving is best effort: the in-memory rates are still correct
        }
    }
}