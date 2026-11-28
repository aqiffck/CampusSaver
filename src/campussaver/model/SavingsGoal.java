package campussaver.model;

/**
 * Represents a student's virtual savings goal (e.g., "New Laptop" or "Emergency Fund").
 * Tracks progress in cents and formats data for goals.txt.
 */
public class SavingsGoal {
    private String goalId;
    private String userId;
    private String name;
    private long targetCents;
    private long savedCents;

    /**
     * Creates a new savings goal starting at 0 cents saved.
     */
    public SavingsGoal(String goalId, String userId, String name, long targetCents) {
        this(goalId, userId, name, targetCents, 0L);
    }

    /**
     * Full constructor used when loading an existing goal from goals.txt.
     */
    public SavingsGoal(String goalId, String userId, String name, long targetCents, long savedCents) {
        if (targetCents <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than 0.");
        }
        this.goalId = goalId;
        this.userId = userId;
        // Replace pipe characters so goals.txt never breaks when saving/loading
        this.name = (name == null) ? "" : name.replace("|", "/");
        this.targetCents = targetCents;
        this.savedCents = Math.max(0L, savedCents);
    }

    public String getGoalId() {
        return goalId;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public long getTargetCents() {
        return targetCents;
    }

    public long getSavedCents() {
        return savedCents;
    }

    /**
     * Adds tracked savings (in cents) toward this goal.
     */
    public void deposit(long cents) {
        if (cents <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than 0.");
        }
        this.savedCents += cents;
    }

    /**
     * Returns completion percentage as a whole number from 0 to 100.
     */
    public int percentDone() {
        if (targetCents <= 0) {
            return 0;
        }
        long percent = (savedCents * 100L) / targetCents;
        return (int) Math.min(100L, percent);
    }

    /**
     * Formats this goal as a pipe-delimited line for data/goals.txt:
     * goalId|userId|name|targetCents|savedCents
     */
    public String toFileFormat() {
        return goalId + "|" + userId + "|" + name + "|" + targetCents + "|" + savedCents;
    }
}