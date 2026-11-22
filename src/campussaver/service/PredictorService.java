package campussaver.service;

import campussaver.model.StudentUser;

/**
 * F2 (spending predictor) and F5 (cash runway). Pure Java: no AI, no Scanner,
 * no printing. Every amount is a long in cents. The AI is only asked for tips
 * or a survival plan (in AIService) after these numbers are calculated.
 */
public class PredictorService {

    /** A runway below this many days triggers the survival plan (F5). */
    public static final int LOW_RUNWAY_DAYS = 14;

    /** Days in a budget month used for the projection. */
    private static final int DAYS_IN_MONTH = 30;

    /** Returned when there is no spending yet, so runway is effectively unlimited. */
    private static final int MAX_RUNWAY_DAYS = 999;

    /**
     * Days the remaining budget lasts at the current daily spending rate.
     * Returns 0 if the budget is used up, and MAX_RUNWAY_DAYS if nothing is spent
     * yet or daysPassed is not valid (never divides by zero).
     */
    public int remainingRunwayDays(StudentUser u, long spentCents, int daysPassed) {
        long remaining = budgetOf(u) - spentCents;
        if (remaining <= 0) {
            return 0;
        }
        if (spentCents <= 0 || daysPassed <= 0) {
            return MAX_RUNWAY_DAYS;
        }
        long days = remaining * daysPassed / spentCents;
        if (days > MAX_RUNWAY_DAYS) {
            return MAX_RUNWAY_DAYS;
        }
        return (int) days;
    }

    /**
     * True if the projected month-end spending is above the budget.
     * With no budget set (0 or less) it returns false, because nothing can be exceeded.
     */
    public boolean isOverspending(StudentUser u, long spentCents, int daysPassed) {
        long budget = budgetOf(u);
        if (budget <= 0) {
            return false;
        }
        return projectedMonthEnd(spentCents, daysPassed) > budget;
    }

    /**
     * Projected total spending at month end: spent so far, scaled from
     * daysPassed to a 30-day month. If daysPassed is not valid, returns spentCents.
     */
    public long projectedMonthEnd(long spentCents, int daysPassed) {
        if (spentCents <= 0) {
            return 0;
        }
        if (daysPassed <= 0) {
            return spentCents;
        }
        if (daysPassed >= DAYS_IN_MONTH) {
            return spentCents;
        }
        return spentCents * DAYS_IN_MONTH / daysPassed;
    }

    /** F5 check: true when the runway is under LOW_RUNWAY_DAYS and the survival plan should show. */
    public boolean isLowRunway(StudentUser u, long spentCents, int daysPassed) {
        return remainingRunwayDays(u, spentCents, daysPassed) < LOW_RUNWAY_DAYS;
    }

    /** The only place that reads the budget from the user. Change the getter name here if needed. */
    private long budgetOf(StudentUser u) {
        if (u == null) {
            return 0;
        }
                return u.getMonthlyBudgetCents();
    }
}