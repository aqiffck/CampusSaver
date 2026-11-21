package campussaver.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
            System.out.println("Could not read rates file: " + e.getMessage());
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
            System.out.println("Could not save rates file: " + e.getMessage());
        }
    }
}