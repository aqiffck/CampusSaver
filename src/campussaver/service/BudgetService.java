package campussaver.service;

import campussaver.exception.AIServiceException;
import campussaver.model.BudgetPlan;
import campussaver.model.StudentUser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/** Builds a budget split. AI suggests percents; Java validates. Any failure -> template. */
public class BudgetService {
    private static final String TEMPLATE_FILE = "data/templates.txt";

    public static BudgetPlan buildFromAI(StudentUser u) {
        try {
            String prompt = AIService.budgetSplitPrompt(
                    u.getLivingType(), u.hasScholarship(), u.getMonthlyBudgetCents());
            String reply = AIHelper.ask(prompt);
            BudgetPlan p = parseReply(reply);
            if (p != null && totalsHundred(p)) return p;
        } catch (AIServiceException e) {
            // fall through to template
        } catch (RuntimeException e) {
            // never crash on odd AI output
        }
        return loadTemplate(u.getLivingType());
    }

    public static boolean totalsHundred(BudgetPlan p) {
        if (p == null || p.getCount() == 0) return false;
        int sum = 0;
        for (int i = 0; i < p.getCount(); i++) sum += p.getPercent(i);
        return sum == 100;
    }

    /** Expects lines like Food|40. Returns null if the reply is unusable. */
    private static BudgetPlan parseReply(String reply) {
        if (reply == null) return null;
        BudgetPlan p = new BudgetPlan();
        String[] lines = reply.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            while (line.startsWith("|") || line.startsWith("*") || line.startsWith("-")) {
                line = line.substring(1).trim();
            }
            if (line.endsWith("|")) line = line.substring(0, line.length() - 1).trim();
            int bar = line.indexOf('|');
            if (bar <= 0) continue;
            String name = line.substring(0, bar).trim();
            String num = line.substring(bar + 1).trim();
            if (num.endsWith("%")) num = num.substring(0, num.length() - 1).trim();
            try {
                if (!p.add(name, Integer.parseInt(num))) return null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return (p.getCount() >= 3) ? p : null;
    }

    /** Reads rows for this livingType from templates.txt; hardcoded default if missing or invalid. */
    public static BudgetPlan loadTemplate(String livingType) {
        BudgetPlan p = new BudgetPlan();
        try (BufferedReader br = new BufferedReader(new FileReader(TEMPLATE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] f = line.split("\\|");
                if (f.length != 3 || !f[0].trim().equalsIgnoreCase(livingType)) continue;
                try {
                    p.add(f[1], Integer.parseInt(f[2].trim()));
                } catch (NumberFormatException e) {
                    // skip bad row
                }
            }
        } catch (IOException e) {
            // use default below
        }
        if (totalsHundred(p)) return p;
        return defaultTemplate(livingType);
    }

    private static BudgetPlan defaultTemplate(String livingType) {
        BudgetPlan p = new BudgetPlan();
        boolean hostel = StudentUser.HOSTEL.equalsIgnoreCase(livingType);
        p.add("Food", hostel ? 40 : 30);
        p.add("Transport", hostel ? 10 : 25);
        p.add("Academic", 20);
        p.add("Savings", hostel ? 15 : 10);
        p.add("Other", 15);
        return p;
    }
}