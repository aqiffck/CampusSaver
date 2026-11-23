package campussaver.service;

import campussaver.exception.AIServiceException;
import campussaver.model.ExpenseTransaction;
import campussaver.model.StudentUser;
import campussaver.model.Transaction;
import campussaver.util.Money;

/** Spending analysis. Java calculates every number (cents); AI wording is added by Member 4. */
public class AIService {

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
                if (runway < PredictorService.LOW_RUNWAY_DAYS) {
            advice.append("Warning: low runway. Your budget may run out in under 14 days.");
        } else {
            advice.append("Status: your spending pace is within safe limits for this month.");
        }
        return advice.toString();
    }

        /** Prompt only. Java validates the reply, so a bad answer falls back to the template. */
    public static String budgetSplitPrompt(String livingType, boolean hasScholarship, long budgetCents) {
        return "You are a budgeting assistant for a Malaysian university student. "
             + "Living type: " + livingType + ". Scholarship: " + (hasScholarship ? "yes" : "no")
             + ". Monthly budget: " + Money.format(budgetCents) + ". "
             + "Suggest a monthly budget split of 4 to 6 categories. Reply with ONLY lines in the "
             + "format Category|Percent (whole numbers, no symbols), and the percents must total exactly 100. "
             + "No other text.";
    }

        // ---------- F2: spending tips ----------
    public static String tipsPrompt(long budgetCents, long spentCents, long projectedCents, int runwayDays) {
        return "You are a friendly budgeting coach for a Malaysian university student. "
             + "Monthly budget: " + Money.format(budgetCents) + ". Spent so far: " + Money.format(spentCents)
             + ". Projected month-end spending: " + Money.format(projectedCents)
             + ". Cash runway: " + runwayDays + " days. "
             + "Give exactly 3 short, practical tips, one per line. Use only the figures given; "
             + "do not invent any numbers. Never suggest skipping meals.";
    }

    public static String tips(long budgetCents, long spentCents, long projectedCents, int runwayDays) {
        try {
            return AIHelper.ask(tipsPrompt(budgetCents, spentCents, projectedCents, runwayDays));
        } catch (AIServiceException e) {
            return fallbackTips(projectedCents > budgetCents);
        }
    }

    public static String fallbackTips(boolean overspending) {
        if (overspending) {
            return "1. You are on track to overspend: cut non-essential purchases this week.\n"
                 + "2. Cook or buy meals from cheaper campus options.\n"
                 + "3. Review your largest expenses and set a weekly limit.";
        }
        return "1. You are within budget: keep logging every expense.\n"
             + "2. Move any extra money into your savings goal.\n"
             + "3. Check your budget again at the start of next week.";
    }

    // ---------- F5: survival plan ----------
    public static String survivalPlanPrompt(long remainingCents, int runwayDays, String livingType) {
        return "You are a supportive advisor for a Malaysian university student (" + livingType + "). "
             + "They have " + Money.format(remainingCents) + " left, which lasts about " + runwayDays + " days. "
             + "Write a short 7-day survival plan with practical, healthy ways to stretch the money. "
             + "Never advise skipping meals or anything unhealthy. Use only the figures given. "
             + "Tell them to contact the university financial aid office. "
             + "End with exactly: General guidance, not financial advice.";
    }

    public static String survivalPlan(long remainingCents, int runwayDays, String livingType) {
        String reply;
        try {
            reply = AIHelper.ask(survivalPlanPrompt(remainingCents, runwayDays, livingType));
        } catch (AIServiceException e) {
            reply = fallbackSurvivalPlan();
        }
        String ending = "General guidance, not financial advice.";
        if (!reply.trim().endsWith(ending)) reply = reply.trim() + "\n" + ending;
        return reply;
    }

    public static String fallbackSurvivalPlan() {
        return "1. Eat regular, simple meals: rice, eggs and vegetables are cheap and filling.\n"
             + "2. Pause all non-essential spending until your next allowance.\n"
             + "3. Use free campus resources and student discounts.\n"
             + "4. Contact the university financial aid office about emergency support.";
    }

    // ---------- F6: sponsor report wording ----------
    public static String sponsorWordingPrompt(String studentName, long totalCents, long academicCents, int academicPercent) {
        return "Write a short, polite 3-sentence summary for a student's parent or sponsor. "
             + "Student: " + studentName + ". Total spent: " + Money.format(totalCents)
             + ". Academic spending: " + Money.format(academicCents) + " (" + academicPercent + "%). "
             + "Use only these figures; do not invent numbers.";
    }

    public static String sponsorWording(String studentName, long totalCents, long academicCents, int academicPercent) {
        try {
            return AIHelper.ask(sponsorWordingPrompt(studentName, totalCents, academicCents, academicPercent));
        } catch (AIServiceException e) {
            return fallbackSponsorWording(studentName, totalCents, academicCents, academicPercent);
        }
    }

    public static String fallbackSponsorWording(String studentName, long totalCents, long academicCents, int academicPercent) {
        return studentName + " spent " + Money.format(totalCents) + " this period, of which "
             + Money.format(academicCents) + " (" + academicPercent + "%) was academic spending.";
    }
}