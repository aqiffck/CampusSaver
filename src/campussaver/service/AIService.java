package campussaver.service;

import campussaver.exception.AIServiceException;
import campussaver.model.ExpenseTransaction;
import campussaver.model.StudentUser;
import campussaver.model.Transaction;

/** Spending analysis. Java calculates every number (cents); AI wording is added by Member 4. */
public class AIService {
    public static final int LOW_RUNWAY_DAYS = 14;

    public double calculateAcademicRatio(Transaction[] transactions, int count) throws AIServiceException {
        if (transactions == null || count <= 0) {
            throw new AIServiceException("Insufficient data: transaction history is empty.");
        }
        long totalSpent = 0;
        long academicSpent = 0;
        for (int i = 0; i < count; i++) {
            if (transactions[i] instanceof ExpenseTransaction) {
                ExpenseTransaction t = (ExpenseTransaction) transactions[i];
                totalSpent += t.getCents();
                if (t.isVerifiedAcademic()) academicSpent += t.getCents();
            }
        }
        if (totalSpent == 0) {
            throw new AIServiceException("Cannot compute spending ratio when total expenses are zero.");
        }
        return academicSpent * 100.0 / totalSpent;
    }

    public int calculateRemainingRunwayDays(StudentUser user, Transaction[] transactions,
                                            int count, int daysPassedInMonth) throws AIServiceException {
        if (transactions == null || count <= 0) {
            throw new AIServiceException("Cannot estimate runway: no transaction records found.");
        }
        if (daysPassedInMonth <= 0) {
            throw new AIServiceException("Invalid timeframe: daysPassedInMonth must be greater than 0.");
        }
        long totalSpent = 0;
        for (int i = 0; i < count; i++) {
            if (transactions[i] instanceof ExpenseTransaction) {
                totalSpent += transactions[i].getCents();
            }
        }
        long remaining = user.getMonthlyBudgetCents() - totalSpent;
        if (remaining <= 0) return 0;
        if (totalSpent == 0) return 30;
        return (int) (remaining * daysPassedInMonth / totalSpent);
    }

    public String generateFinancialAdvice(StudentUser user, Transaction[] transactions,
                                          int count, int daysPassedInMonth) throws AIServiceException {
        double ratio = calculateAcademicRatio(transactions, count);
        int runway = calculateRemainingRunwayDays(user, transactions, count, daysPassedInMonth);

        StringBuilder advice = new StringBuilder();
        advice.append("=== Financial Insight ===\n");
        advice.append(String.format("Academic Spending Ratio: %.1f%%\n", ratio));
        advice.append(String.format("Estimated Runway: %d days remaining\n", runway));
        if (ratio < 40.0) {
            advice.append("Recommendation: over 60% of spending is non-academic. Consider cutting discretionary expenses.\n");
        } else {
            advice.append("Recommendation: good job prioritising academic necessities.\n");
        }
        if (runway < LOW_RUNWAY_DAYS) {
            advice.append("Warning: low runway. Your budget may run out in under 14 days.");
        } else {
            advice.append("Status: your spending pace is within safe limits for this month.");
        }
        return advice.toString();
    }
}