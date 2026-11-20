package campussaver.model;

public class ExpenseTransaction extends Transaction {
    private String category;
    private String merchant;
    private boolean isVerifiedAcademic;

    public ExpenseTransaction(String id, double amount, String date, String description, String category, String merchant, boolean isVerifiedAcademic) {
        super(id, amount, date, description);
        this.category = category;
        this.merchant = merchant;
        this.isVerifiedAcademic = isVerifiedAcademic;
    }

    public String getCategory() { return category; }
    public String getMerchant() { return merchant; }
    public boolean isVerifiedAcademic() { return isVerifiedAcademic; }

    @Override
    public String getSummary() {
        return String.format("[EXPENSE] %s | Category: %s | Merchant: %s | Amount: MYR %.2f | Date: %s",
                getId(), category, merchant, getAmount(), getDate());
    }

    @Override
    public String toFileFormat() {
        return String.format("EXPENSE,%s,%.2f,%s,%s,%s,%s,%b",
                getId(), getAmount(), getDate(), getDescription(), category, merchant, isVerifiedAcademic);
    }
}