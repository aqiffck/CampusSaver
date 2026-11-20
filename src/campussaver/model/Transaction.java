package campussaver.model;

public abstract class Transaction {
    private final String id;
private final double amount;
private final String date;
private final String description;

    public Transaction(String id, double amount, String date, String description) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    public String getId() { return id; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getDescription() { return description; }

    public abstract String getSummary();
    public abstract String toFileFormat();
}