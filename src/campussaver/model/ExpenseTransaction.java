package campussaver.model;

public class ExpenseTransaction extends Transaction {
    private final String category;
private final boolean isVerifiedAcademic;

    public ExpenseTransaction(String id, double amount, String date, String description, String category, boolean isVerifiedAcademic) {
        super(id, amount, date, description);
        this.category = category;
        this.isVerifiedAcademic = isVerifiedAcademic;
    }

    public String getCategory() { return category; }
    public boolean isVerifiedAcademic() { return isVerifiedAcademic; }

    @Override
    public String getSummary() {
        return "[" + category + "] " + getDescription() + ": MYR " + getAmount();
    }

    @Override
    public String toFileFormat() {
        return getId() + "," + getAmount() + "," + getDate() + "," + getDescription() + "," + category + "," + isVerifiedAcademic;
    }
}