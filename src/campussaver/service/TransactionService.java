package campussaver.service;

import campussaver.exception.LowRunwayException;
import campussaver.model.ExpenseTransaction;
import campussaver.model.SavingsGoal;
import campussaver.model.SavingsTransaction;
import campussaver.model.StudentUser;
import campussaver.model.Transaction;
import campussaver.util.Money;

/**
 * Core engine for expenses, virtual spare-change round-ups, and savings goals.
 * Uses plain arrays and counters only (no Java Collections).
 */
public class TransactionService {
    private Transaction[] transactions = new Transaction[200];
    private int transactionCount = 0;
    private int idSequence = 1;

    private SavingsGoal[] goals = new SavingsGoal[50];
    private int goalCount = 0;
    private int goalSequence = 1;

    /**
     * Records an expense for a student. Saves the expense first, then throws
     * LowRunwayException if remaining budget is <= 0 or below 15% of budget.
     */
    public ExpenseTransaction addExpense(StudentUser u, long cents, String date,
                                         String desc, String category, boolean academic)
            throws LowRunwayException {
        if (u == null) {
            throw new IllegalArgumentException("Student user cannot be null.");
        }
        if (cents <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than 0.");
        }

        String safeDesc = (desc == null) ? "" : desc.replace("|", "/").trim();
        String safeCategory = (category == null) ? "Other" : category.replace("|", "/").trim();

        ensureTransactionCapacity();
        String id = String.format("T%04d", idSequence++);
        ExpenseTransaction expense = new ExpenseTransaction(
                id, u.getUserId(), cents, date, safeDesc, safeCategory, academic
        );

        // Save first so a LowRunwayException warning never loses data
        transactions[transactionCount++] = expense;

        long budget = u.getMonthlyBudgetCents();
        long remaining = budget - getTotalExpenses(u.getUserId());

        if (remaining <= 0 || (remaining * 100L < budget * 15L)) {
            throw new LowRunwayException(
                    "Low runway alert: remaining budget is " + Money.format(remaining)
                            + " out of " + Money.format(budget) + "."
            );
        }

        return expense;
    }

    /**
     * Locked signature: calculates spare-change round-up and deposits into the
     * student's first savings goal (or "General Bucket" if none exist).
     * Returns null if the expense is already an exact Ringgit.
     */
    public SavingsTransaction addRoundUpSaving(StudentUser u, long expenseCents,
                                               String date, String sourceId) {
        SavingsGoal defaultGoal = findFirstGoalForUser(u == null ? null : u.getUserId());
        String goalId = (defaultGoal == null) ? null : defaultGoal.getGoalId();
        return addRoundUpSaving(u, expenseCents, date, sourceId, goalId);
    }

    /**
     * Overload that deposits the round-up into a specific chosen goal by goalId.
     */
    public SavingsTransaction addRoundUpSaving(StudentUser u, long expenseCents,
                                               String date, String sourceId, String goalId) {
        if (u == null) {
            throw new IllegalArgumentException("Student user cannot be null.");
        }
        long roundUpCents = Money.roundUpToRinggit(expenseCents);
        if (roundUpCents <= 0) {
            return null;
        }

        SavingsGoal targetGoal = findGoalById(goalId, u.getUserId());
        String goalName = "General Bucket";
        if (targetGoal != null) {
            targetGoal.deposit(roundUpCents);
            goalName = targetGoal.getName();
        }

        ensureTransactionCapacity();
        String id = String.format("T%04d", idSequence++);
        String desc = "Spare-change round-up (tracks savings)";
        SavingsTransaction saving = new SavingsTransaction(
                id, u.getUserId(), roundUpCents, date, desc, sourceId, goalName
        );
        transactions[transactionCount++] = saving;
        return saving;
    }

    /**
     * Sums all expense amounts (in cents) for a specific user.
     */
    public long getTotalExpenses(String userId) {
        if (userId == null) return 0L;
        long total = 0L;
        for (int i = 0; i < transactionCount; i++) {
            Transaction t = transactions[i];
            if (t instanceof ExpenseTransaction && userId.equalsIgnoreCase(t.getUserId())) {
                ExpenseTransaction e = (ExpenseTransaction) t;
                total += e.getCents();
            }
        }
        return total;
    }

    /**
     * Sums all verified academic expense amounts (in cents) for a specific user.
     */
    public long getTotalAcademic(String userId) {
        if (userId == null) return 0L;
        long total = 0L;
        for (int i = 0; i < transactionCount; i++) {
            Transaction t = transactions[i];
            if (t instanceof ExpenseTransaction && userId.equalsIgnoreCase(t.getUserId())) {
                ExpenseTransaction e = (ExpenseTransaction) t;
                if (e.isVerifiedAcademic()) {
                    total += e.getCents();
                }
            }
        }
        return total;
    }

    /**
     * Sums all tracked savings amounts (in cents) for a specific user.
     */
    public long getTotalSavings(String userId) {
        if (userId == null) return 0L;
        long total = 0L;
        for (int i = 0; i < transactionCount; i++) {
            Transaction t = transactions[i];
            if (t instanceof SavingsTransaction && userId.equalsIgnoreCase(t.getUserId())) {
                SavingsTransaction s = (SavingsTransaction) t;
                total += s.getCents();
            }
        }
        return total;
    }

    /**
     * Case-insensitive search matching description or ID.
     * Writes matches into the caller's results array and returns the match count.
     */
    public int searchByKeyword(String keyword, Transaction[] results) {
        if (keyword == null || keyword.trim().isEmpty() || results == null || results.length == 0) {
            return 0;
        }
        String needle = keyword.trim().toLowerCase();
        int matchCount = 0;

        for (int i = 0; i < transactionCount; i++) {
            Transaction t = transactions[i];
            if (t == null) continue;

            String desc = (t.getDescription() == null) ? "" : t.getDescription().toLowerCase();
            String id = (t.getId() == null) ? "" : t.getId().toLowerCase();

            if (desc.contains(needle) || id.contains(needle)) {
                if (matchCount < results.length) {
                    results[matchCount] = t;
                }
                matchCount++;
            }
        }
        return Math.min(matchCount, results.length);
    }

    /**
     * Deletes a transaction by ID, shifts remaining elements left, sets the
     * last slot to null, and decrements transactionCount.
     */
    public boolean deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        String target = id.trim();
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i] != null && transactions[i].getId().equalsIgnoreCase(target)) {
                for (int j = i; j < transactionCount - 1; j++) {
                    transactions[j] = transactions[j + 1];
                }
                transactions[transactionCount - 1] = null;
                transactionCount--;
                return true;
            }
        }
        return false;
    }

    /**
     * Creates and stores a new SavingsGoal. Rejects targetCents <= 0.
     */
    public SavingsGoal addGoal(String userId, String name, long targetCents) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Goal name cannot be empty.");
        }
        if (targetCents <= 0) {
            throw new IllegalArgumentException("Goal target must be greater than 0.");
        }

        ensureGoalCapacity();
        String goalId = String.format("G%03d", goalSequence++);
        SavingsGoal goal = new SavingsGoal(goalId, userId.trim(), name.trim(), targetCents);
        goals[goalCount++] = goal;
        return goal;
    }

    public Transaction[] getTransactions() {
        return transactions;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    /**
     * Sets loaded count after FileService reads transactions.txt and updates
     * idSequence to count + 1 so new IDs never collide after a restart.
     */
    public void setLoadedCount(int count) {
        if (count < 0) {
            this.transactionCount = 0;
        } else if (count > transactions.length) {
            this.transactionCount = transactions.length;
        } else {
            this.transactionCount = count;
        }
        this.idSequence = this.transactionCount + 1;
    }

    public SavingsGoal[] getGoals() {
        return goals;
    }

    public int getGoalCount() {
        return goalCount;
    }

    public void setLoadedGoalCount(int count) {
        if (count < 0) {
            this.goalCount = 0;
        } else if (count > goals.length) {
            this.goalCount = goals.length;
        } else {
            this.goalCount = count;
        }
        this.goalSequence = this.goalCount + 1;
    }

    // --- Private Helpers ---

    private SavingsGoal findFirstGoalForUser(String userId) {
        if (userId == null) return null;
        for (int i = 0; i < goalCount; i++) {
            if (goals[i] != null && userId.equalsIgnoreCase(goals[i].getUserId())) {
                return goals[i];
            }
        }
        return null;
    }

    private SavingsGoal findGoalById(String goalId, String userId) {
        if (goalId == null || userId == null) return null;
        for (int i = 0; i < goalCount; i++) {
            if (goals[i] != null
                    && goalId.equalsIgnoreCase(goals[i].getGoalId())
                    && userId.equalsIgnoreCase(goals[i].getUserId())) {
                return goals[i];
            }
        }
        return null;
    }

    /** Doubles the transactions array manually using a for loop when full. */
    private void ensureTransactionCapacity() {
        if (transactionCount < transactions.length) {
            return;
        }
        Transaction[] bigger = new Transaction[transactions.length * 2];
        for (int i = 0; i < transactionCount; i++) {
            bigger[i] = transactions[i];
        }
        transactions = bigger;
    }

    /** Doubles the goals array manually using a for loop when full. */
    private void ensureGoalCapacity() {
        if (goalCount < goals.length) {
            return;
        }
        SavingsGoal[] bigger = new SavingsGoal[goals.length * 2];
        for (int i = 0; i < goalCount; i++) {
            bigger[i] = goals[i];
        }
        goals = bigger;
    }
}