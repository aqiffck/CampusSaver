package campussaver.service;

import campussaver.model.ExpenseTransaction;
import campussaver.model.StudentUser;
import campussaver.model.Transaction;
import campussaver.util.Money;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

/** F6: builds the parent/sponsor report. Java calculates every number; AI only writes the summary. */
public class SponsorReportService {
    private static final int MAX_CATEGORIES = 50;

    /** Returns the full report text. Never throws; AI failure uses the fallback wording. */
    public static String buildReport(StudentUser student, Transaction[] transactions, int count) {
        if (student == null) return "No student selected.";

        long total = 0;
        long academic = 0;
        String[] cats = new String[MAX_CATEGORIES];
        long[] sums = new long[MAX_CATEGORIES];
        int n = 0;

        if (transactions != null) {
            for (int i = 0; i < count && i < transactions.length; i++) {
                if (!(transactions[i] instanceof ExpenseTransaction)) continue;
                ExpenseTransaction e = (ExpenseTransaction) transactions[i];
                if (!student.getUserId().equals(e.getUserId())) continue;

                total += e.getCents();
                if (e.isVerifiedAcademic()) academic += e.getCents();

                String cat = e.getCategory().isEmpty() ? "Other" : e.getCategory();
                int idx = -1;
                for (int j = 0; j < n; j++) {
                    if (cats[j].equalsIgnoreCase(cat)) { idx = j; break; }
                }
                if (idx < 0 && n < MAX_CATEGORIES) {
                    cats[n] = cat;
                    idx = n;
                    n++;
                }
                if (idx >= 0) sums[idx] += e.getCents(); // totals above stay correct even if full
            }
        }

        long personal = total - academic;
        int academicPercent = total == 0 ? 0 : (int) ((academic * 100 + total / 2) / total);
        int personalPercent = total == 0 ? 0 : 100 - academicPercent;

        StringBuilder sb = new StringBuilder();
        sb.append("=== CampusSaver Sponsor Report ===\n");
        sb.append("Student: ").append(student.getName()).append("\n");
        sb.append("Date: ").append(LocalDate.now()).append("\n");
        sb.append("Monthly budget: ").append(Money.format(student.getMonthlyBudgetCents())).append("\n\n");
        sb.append("Total spent: ").append(Money.format(total)).append("\n");
        sb.append("Academic: ").append(Money.format(academic)).append(" (").append(academicPercent).append("%)\n");
        sb.append("Personal: ").append(Money.format(personal)).append(" (").append(personalPercent).append("%)\n");

        if (n > 0) {
            sb.append("\nSpending by category:\n");
            for (int j = 0; j < n; j++) {
                sb.append("- ").append(cats[j]).append(": ").append(Money.format(sums[j])).append("\n");
            }
        }

        sb.append("\nSummary:\n");
        if (total == 0) {
            sb.append("No expenses have been recorded yet.");
        } else {
            sb.append(AIService.sponsorWording(student.getName(), total, academic, academicPercent).trim());
        }
        sb.append("\n\nFigures are calculated by CampusSaver. The summary wording is AI-assisted.\n");
        return sb.toString();
    }

    /** File used for this student's report, e.g. data/sponsor_report_U1.txt */
    public static String reportPath(StudentUser student) {
        String safe = student.getUserId().replaceAll("[^A-Za-z0-9_-]", "_");
        return "data/sponsor_report_" + safe + ".txt";
    }

    /** Saves the report to data/. Returns false on any problem; never throws. */
    public static boolean saveReport(StudentUser student, String report) {
        if (student == null || report == null) return false;
        try {
            new File("data").mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(reportPath(student), false))) {
                pw.print(report);
                return !pw.checkError();
            }
        } catch (IOException e) {
            return false;
        }
    }
}