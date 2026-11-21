package campussaver.model;

import campussaver.util.Money;

/** A spending record. */
public class ExpenseTransaction extends Transaction {
    private final String category;
    private final boolean isVerifiedAcademic;

    public ExpenseTransaction(String id, String userId, long cents, String date,
                              String description, String category, boolean isVerifiedAcademic) {
        super(id, userId, cents, date, description);
        this.category = clean(category);
        this.isVerifiedAcademic = isVerifiedAcademic;
    }

    public String getCategory() { return category; }
    public boolean isVerifiedAcademic() { return isVerifiedAcademic; }

    @Override
    public String getSummary() {
        return getId() + " [" + category + "] " + getDescription() + ": " + Money.format(getCents());
    }

    @Override
    public String toFileFormat() {
        return getId() + "|EXPENSE|" + getUserId() + "|" + getCents() + "|" + getDate() + "|"
                + getDescription() + "|" + category + "|" + isVerifiedAcademic + "|";
    }
}