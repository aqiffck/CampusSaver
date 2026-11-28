package campussaver.model;

import campussaver.util.Money;

/**
 * Represents a virtual spare-change savings record linked to an expense and a goal.
 * Tracks savings in cents; overrides getSummary() and toFileFormat() for polymorphism.
 */
public class SavingsTransaction extends Transaction {
    private final String sourceId;
    private final String goalName;

    public SavingsTransaction(String id, String userId, long cents, String date,
                              String description, String sourceId, String goalName) {
        super(id, userId, cents, date, description);
        this.sourceId = clean(sourceId);
        this.goalName = clean(goalName);
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getGoalName() {
        return goalName;
    }

    /**
     * One-line summary for menus/UI. Uses "tracks savings" per team rules.
     */
    @Override
    public String getSummary() {
        String goalText = goalName.isEmpty() ? "General Bucket" : goalName;
        String sourceText = sourceId.isEmpty() ? "" : " (from " + sourceId + ")";
        return "[" + getId() + "] " + getDate() + " | SAVING | "
                + Money.format(getCents()) + " | Tracks savings -> "
                + goalText + sourceText + " - " + getDescription();
    }

    /**
     * Full line for data/transactions.txt:
     * id|type|userId|cents|date|desc|category|isAcademic|goalName
     * Stores sourceId in the category slot and false for isAcademic.
     */
    @Override
    public String toFileFormat() {
        return getId() + "|SAVINGS|" + getUserId() + "|" + getCents() + "|"
                + getDate() + "|" + getDescription() + "|" + sourceId
                + "|false|" + goalName;
    }
}