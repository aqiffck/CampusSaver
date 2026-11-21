package campussaver.util;

/** Money helper. All amounts are stored as long cents; never use double for money. */
public class Money {

    /** 1234 -> "RM 12.34" */
    public static String format(long cents) {
        long abs = Math.abs(cents);
        long sen = abs % 100;
        return "RM " + (cents < 0 ? "-" : "") + (abs / 100) + "." + (sen < 10 ? "0" : "") + sen;
    }

    /** "12.5" -> 1250. Returns -1 if invalid (letters, negative, more than 2 decimals, too large). */
    public static long parse(String text) {
        if (text == null) return -1;
        String t = text.trim();
        if (!t.matches("\\d{1,7}(\\.\\d{1,2})?")) return -1;
        int dot = t.indexOf('.');
        if (dot < 0) return Long.parseLong(t) * 100;
        long whole = Long.parseLong(t.substring(0, dot));
        String frac = t.substring(dot + 1);
        if (frac.length() == 1) frac = frac + "0";
        return whole * 100 + Long.parseLong(frac);
    }

    /** Extra cents needed to reach the next whole Ringgit (0 if already whole). 740 -> 60. */
    public static long roundUpToRinggit(long cents) {
        long r = cents % 100;
        return r == 0 ? 0 : 100 - r;
    }
}