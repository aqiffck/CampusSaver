package campussaver.service;

import campussaver.exception.AIServiceException;
import campussaver.model.ExpenseTransaction;
import campussaver.model.StudentUser;
import java.util.List;

public class AIService {

    /**
     * Calculates the percentage of total spending allocated to academic necessities.
     */
    public double calculateAcademicRatio(List<ExpenseTransaction> transactions) throws AIServiceException {
        if (transactions == null || transactions.isEmpty()) {
            throw new AIServiceException("Insufficient data: Transaction history is empty.");
        }

        double totalSpent = 0.0;
        double academicSpent = 0.0;

        for (ExpenseTransaction t : transactions) {
            totalSpent += t.getAmount();
            if (t.isVerifiedAcademic()) {
                academicSpent += t.getAmount();
            }
        }

        if (totalSpent == 0.0) {
            throw new AIServiceException("Cannot compute spending ratio when total expenses are MYR 0.00.");
        }

        return (academicSpent / totalSpent) * 100.0;
    }

    /**
     * Estimates remaining runway days based on daily spending velocity.
     */
    public int calculateRemainingRunwayDays(StudentUser user, List<ExpenseTransaction> transactions, int daysPassedInMonth) throws AIServiceException {
        if (transactions == null || transactions.isEmpty()) {
            throw new AIServiceException("Cannot estimate runway: No transaction records found.");
        }
        if (daysPassedInMonth <= 0) {
            throw new AIServiceException("Invalid analysis timeframe: daysPassedInMonth must be greater than 0.");
        }

        double totalSpent = 0.0;
        for (ExpenseTransaction t : transactions) {
            totalSpent += t.getAmount();
        }

        double remainingBudget = user.getMonthlyBudgetLimit() - totalSpent;
        if (remainingBudget <= 0) {
            return 0;
        }

        double dailyVelocity = totalSpent / daysPassedInMonth;
        if (dailyVelocity <= 0) {
            return 30;
        }

        return (int) (remainingBudget / dailyVelocity);
    }

    /**
     * Generates tailored financial advice based on spending pattern analysis.
     */
    public String generateFinancialAdvice(StudentUser user, List<ExpenseTransaction> transactions, int daysPassedInMonth) throws AIServiceException {
        double academicRatio = calculateAcademicRatio(transactions);
        int runwayDays = calculateRemainingRunwayDays(user, transactions, daysPassedInMonth);

        StringBuilder advice = new StringBuilder();
        advice.append("=== AI Financial Insight ===\n");
        advice.append(String.format("Academic Spending Ratio: %.1f%%\n", academicRatio));
        advice.append(String.format("Estimated Runway: %d days remaining\n", runwayDays));

        if (academicRatio < 40.0) {
            advice.append("Recommendation: Over 60% of spending is non-academic. Consider cutting discretionary expenses.\n");
        } else {
            advice.append("Recommendation: Great job prioritizing academic necessities!\n");
        }

        if (runwayDays < 7) {
            advice.append("Warning: High burn rate detected! Your budget will be depleted in less than a week.");
        } else {
            advice.append("Status: Your spending velocity is within safe limits for this month.");
        }

        return advice.toString();
    }
}