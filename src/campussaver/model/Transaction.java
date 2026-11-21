package campussaver.model;

/** Abstract parent of every money record. Amount is stored in cents. */
public abstract class Transaction {
    private final String id;
    private final String userId;
    private final long cents;
    private final String date;
    private final String description;

    public Transaction(String id, String userId, long cents, String date, String description) {
        this.id = id;
        this.userId = userId;
        this.cents = cents;
        this.date = date;
        this.description = clean(description);
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public long getCents() { return cents; }
    public String getDate() { return date; }
    public String getDescription() { return description; }

    /** Removes the file delimiter and line breaks so .txt files never break. */
    protected static String clean(String text) {
        if (text == null) return "";
        return text.replace("|", "/").replace("\n", " ").replace("\r", " ").trim();
    }

    /** One-line text for the menu. Each subclass formats its own. */
    public abstract String getSummary();

    /** Full line for transactions.txt:
     *  id|type|userId|cents|date|desc|category|isAcademic|goalName */
    public abstract String toFileFormat();
}