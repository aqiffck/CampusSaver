package campussaver.main;

import campussaver.exception.AIServiceException;
import campussaver.exception.LowRunwayException;
import campussaver.model.BudgetPlan;
import campussaver.model.ExpenseTransaction;
import campussaver.model.StudentUser;
import campussaver.model.Transaction;
import campussaver.model.User;
import campussaver.service.AIService;
import campussaver.service.BudgetService;
import campussaver.service.CurrencyService;
import campussaver.service.PredictorService;
import campussaver.service.SponsorReportService;
import campussaver.util.InputHelper;
import campussaver.util.Money;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Scanner;

/** Terminal test menu for CampusSaver (Part 4 & Part 5). */
public class CampusSaverApp {

    private static final int MAX_USERS = 50;
    private static final int MAX_TRANSACTIONS = 500;
    private static final String TEMPLATE_FILE = "data/templates.txt";

    private static final User[] users = new User[MAX_USERS];
    private static int userCount = 0;

    private static final Transaction[] transactions = new Transaction[MAX_TRANSACTIONS];
    private static int txCount = 0;

    private static final CurrencyService currencyService = new CurrencyService();
    private static final PredictorService predictorService = new PredictorService();
    private static final AIService aiService = new AIService();

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Part 3 & 4: Initialize services and load saved data
        currencyService.loadRates();
        // TODO (when FileService is merged): load users, transactions, goals, budgets,
        // templates and set counts

        System.out.println("==========================================");
        System.out.println("   Welcome to CampusSaver (CLI Menu)");
        System.out.println("==========================================");

        while (true) {
            User currentUser;
            if (userCount == 0) {
                System.out.println("\nNo accounts found. Let's set up your first student account.");
                currentUser = registerStudent(input);
                if (InputHelper.readYesNo(input, "Would you also like to create an Admin account now? (Y/N): ")) {
                    registerAdmin(input);
                }
            } else {
                currentUser = loginOrRegisterMenu(input);
                if (currentUser == null) {
                    saveAll();
                    System.out.println("Goodbye!");
                    break;
                }
            }

            if ("STUDENT".equals(currentUser.getUserRole())) {
                studentMenu(input, (StudentUser) currentUser);
            } else if ("ADMIN".equals(currentUser.getUserRole())) {
                adminMenu(input, currentUser);
            } else {
                System.out.println("Sponsor role view will be enabled once SponsorUser is merged.");
            }
        }
        input.close();
    }

    // ==================== PART 4: LOGIN & REGISTRATION ====================

    private static User loginOrRegisterMenu(Scanner input) {
        System.out.println("\n--- Select Account ---");
        for (int i = 0; i < userCount; i++) {
            System.out.println((i + 1) + ". " + users[i].getName()
                    + " [" + users[i].getUserRole() + "] (" + users[i].getUserId() + ")");
        }
        int regStudentOpt = userCount + 1;
        int regAdminOpt = userCount + 2;
        int exitOpt = userCount + 3;

        System.out.println(regStudentOpt + ". Register new Student account");
        System.out.println(regAdminOpt + ". Register new Admin account");
        System.out.println(exitOpt + ". Save and Exit");

        int choice = InputHelper.readInt(input, "Choose an option: ", 1, exitOpt);
        if (choice <= userCount) {
            return users[choice - 1];
        } else if (choice == regStudentOpt) {
            return registerStudent(input);
        } else if (choice == regAdminOpt) {
            return registerAdmin(input);
        }
        return null;
    }

    private static StudentUser registerStudent(Scanner input) {
        if (userCount >= MAX_USERS) {
            System.out.println("User limit reached.");
            return null;
        }
        String name = InputHelper.readNonEmpty(input, "Student name: ");
        int liveChoice = InputHelper.readInt(input, "Living type (1 = HOSTEL, 2 = COMMUTER): ", 1, 2);
        String livingType = (liveChoice == 1) ? StudentUser.HOSTEL : StudentUser.COMMUTER;
        boolean hasScholarship = InputHelper.readYesNo(input, "Do you have a scholarship? (Y/N): ");
        long budgetCents = InputHelper.readMoney(input, "Monthly budget (RM, e.g. 1000.00): ");

        String id = "U" + (userCount + 1);
        StudentUser student = new StudentUser(id, name, "MYR", livingType, hasScholarship, budgetCents);
        users[userCount++] = student;
        System.out.println("Student registered: " + student.getName() + " (" + id + ")");
        return student;
    }

    private static User registerAdmin(Scanner input) {
        if (userCount >= MAX_USERS) {
            System.out.println("User limit reached.");
            return null;
        }
        String name = InputHelper.readNonEmpty(input, "Admin name: ");
        String id = "A" + (userCount + 1);
        // Uses anonymous User subclass until AdminUser.java is merged by teammate
        User admin = new User(id, name, "MYR") {
            @Override
            public String getUserRole() {
                return "ADMIN";
            }
        };
        users[userCount++] = admin;
        System.out.println("Admin registered: " + admin.getName() + " (" + id + ")");
        return admin;
    }

    // ==================== PART 5: STUDENT MENU ====================

    private static void studentMenu(Scanner input, StudentUser student) {
        String displayCurrency = student.getBaseCurrency();

        while (true) {
            System.out.println("\n=== Student Menu (" + student.getName() + " | Display: " + displayCurrency + ") ===");
            System.out.println("1. Add expense");
            System.out.println("2. Summary (with currency switch)");
            System.out.println("3. Search or delete a transaction");
            System.out.println("4. Budget setup (AI / Template)");
            System.out.println("5. Spending predictor and alerts");
            System.out.println("6. Runway and survival plan");
            System.out.println("7. Savings goals");
            System.out.println("8. Sponsor report");
            System.out.println("9. Save and logout");

            int choice = InputHelper.readInt(input, "Select option (1-9): ", 1, 9);
            switch (choice) {
                case 1:
                    addExpense(input, student);
                    break;
                case 2:
                    displayCurrency = showSummary(input, student, displayCurrency);
                    break;
                case 3:
                    searchOrDeleteTransaction(input, student);
                    break;
                case 4:
                    setupBudget(student);
                    break;
                case 5:
                    showPredictorAndAlerts(student);
                    break;
                case 6:
                    showRunwayAndSurvival(student);
                    break;
                case 7:
                    System.out.println("Savings goals module will connect once GoalService is merged.");
                    break;
                case 8:
                    showSponsorReport(student);
                    break;
                case 9:
                    saveAll();
                    System.out.println("Progress saved. Logging out...");
                    return;
            }
        }
    }

    private static void addExpense(Scanner input, StudentUser student) {
        if (txCount >= MAX_TRANSACTIONS) {
            System.out.println("Transaction storage is full.");
            return;
        }
        String desc = InputHelper.readNonEmpty(input, "Expense description: ");
        String category = InputHelper.readNonEmpty(input, "Category (Food/Transport/Academic/Other): ");
        long cents = InputHelper.readMoney(input, "Amount spent (RM): ");
        boolean isAcademic = InputHelper.readYesNo(input, "Is this a verified academic expense? (Y/N): ");

        String id = "T" + (txCount + 1);
        String today = LocalDate.now().toString();
        ExpenseTransaction tx = new ExpenseTransaction(id, student.getUserId(), cents, today, desc, category,
                isAcademic);
        transactions[txCount++] = tx;
        System.out.println("Saved: " + tx.getSummary());

        // Round-up prompt
        long roundUp = Money.roundUpToRinggit(cents);
        if (roundUp > 0) {
            boolean wantRoundUp = InputHelper.readYesNo(input,
                    "Round up " + Money.format(roundUp) + " into a savings goal? (Y/N): ");
            if (wantRoundUp) {
                // TODO (when GoalService is merged): add roundUp cents to active goal
                System.out.println(
                        "Round-up of " + Money.format(roundUp) + " noted (will link to GoalService once merged).");
            }
        }

        // Check low runway after expense is saved
        try {
            checkRunwayWarning(student);
        } catch (LowRunwayException e) {
            System.out.println("WARNING: " + e.getMessage());
        }
    }

    private static void checkRunwayWarning(StudentUser student) throws LowRunwayException {
        long spent = totalSpentFor(student.getUserId());
        int daysPassed = LocalDate.now().getDayOfMonth();
        if (predictorService.isLowRunway(student, spent, daysPassed)) {
            int daysLeft = predictorService.remainingRunwayDays(student, spent, daysPassed);
            throw new LowRunwayException("Low runway! Remaining budget may only last " + daysLeft + " day(s).");
        }
    }

    private static String showSummary(Scanner input, StudentUser student, String currentCurrency) {
        long budget = student.getMonthlyBudgetCents();
        long spent = totalSpentFor(student.getUserId());
        long remaining = budget - spent;

        long dispBudget = currencyService.convert(budget, "MYR", currentCurrency);
        long dispSpent = currencyService.convert(spent, "MYR", currentCurrency);
        long dispRem = currencyService.convert(remaining, "MYR", currentCurrency);

        System.out.println("\n--- Spending Summary (" + currentCurrency + ") ---");
        System.out.println("Monthly Budget: " + formatInCurrency(dispBudget, currentCurrency));
        System.out.println("Total Spent:    " + formatInCurrency(dispSpent, currentCurrency));
        System.out.println("Remaining:      " + formatInCurrency(dispRem, currentCurrency));

        if (InputHelper.readYesNo(input, "Switch display currency? (Y/N): ")) {
            String[] codes = currencyService.getCodes();
            System.out.print("Available currencies: ");
            for (int i = 0; i < codes.length; i++) {
                System.out.print(codes[i] + (i < codes.length - 1 ? ", " : "\n"));
            }
            String chosen = InputHelper.readNonEmpty(input, "Enter currency code: ").toUpperCase();
            for (int i = 0; i < codes.length; i++) {
                if (codes[i].equals(chosen)) {
                    return chosen;
                }
            }
            System.out.println("Unknown currency code. Keeping " + currentCurrency + ".");
        }
        return currentCurrency;
    }

    private static void searchOrDeleteTransaction(Scanner input, StudentUser student) {
        System.out.println("\n--- Your Transactions ---");
        int found = 0;
        for (int i = 0; i < txCount; i++) {
            if (transactions[i].getUserId().equals(student.getUserId())) {
                System.out.println(
                        (i + 1) + ". " + transactions[i].getSummary() + " (" + transactions[i].getDate() + ")");
                found++;
            }
        }
        if (found == 0) {
            System.out.println("No transactions recorded yet.");
            return;
        }
        if (!InputHelper.readYesNo(input, "Would you like to delete a transaction? (Y/N): ")) {
            return;
        }
        String targetId = InputHelper.readNonEmpty(input, "Enter Transaction ID to delete (e.g., T1): ").toUpperCase();
        for (int i = 0; i < txCount; i++) {
            if (transactions[i].getUserId().equals(student.getUserId())
                    && transactions[i].getId().equalsIgnoreCase(targetId)) {
                for (int j = i; j < txCount - 1; j++) {
                    transactions[j] = transactions[j + 1];
                }
                transactions[--txCount] = null;
                System.out.println("Deleted transaction " + targetId + ".");
                return;
            }
        }
        System.out.println("Transaction ID not found.");
    }

    private static void setupBudget(StudentUser student) {
        System.out.println("\nGenerating recommended budget split...");
        BudgetPlan plan = BudgetService.buildFromAI(student);
        long totalBudget = student.getMonthlyBudgetCents();
        System.out.println("--- Recommended Budget Plan (" + Money.format(totalBudget) + ") ---");
        for (int i = 0; i < plan.getCount(); i++) {
            int pct = plan.getPercent(i);
            long catCents = totalBudget * pct / 100;
            System.out.println("- " + plan.getCategory(i) + ": " + pct + "% (" + Money.format(catCents) + ")");
        }
    }

    private static void showPredictorAndAlerts(StudentUser student) {
        long budget = student.getMonthlyBudgetCents();
        long spent = totalSpentFor(student.getUserId());
        int daysPassed = LocalDate.now().getDayOfMonth();
        long projected = predictorService.projectedMonthEnd(spent, daysPassed);
        int runway = predictorService.remainingRunwayDays(student, spent, daysPassed);

        System.out.println("\n--- Spending Predictor & Alerts ---");
        System.out.println("Projected Month-End Spend: " + Money.format(projected));
        System.out.println("Estimated Runway:          " + runway + " days");
        if (predictorService.isOverspending(student, spent, daysPassed)) {
            System.out.println("ALERT: You are projected to exceed your monthly budget!");
        }
        System.out.println("\nTips:");
        System.out.println(AIService.tips(budget, spent, projected, runway));

        // Also run AIService insight with explicit AIServiceException handling per
        // checklist
        try {
            String insight = aiService.generateFinancialAdvice(student, transactions, txCount, daysPassed);
            System.out.println("\n" + insight);
        } catch (AIServiceException e) {
            System.out.println("\n[AI Insight Fallback] " + e.getMessage());
        }
    }

    private static void showRunwayAndSurvival(StudentUser student) {
        long budget = student.getMonthlyBudgetCents();
        long spent = totalSpentFor(student.getUserId());
        long remaining = Math.max(0, budget - spent);
        int daysPassed = LocalDate.now().getDayOfMonth();
        int runway = predictorService.remainingRunwayDays(student, spent, daysPassed);

        System.out.println("\n--- Runway & Survival Plan ---");
        System.out.println("Remaining Budget: " + Money.format(remaining) + " | Runway: " + runway + " days");
        System.out.println(AIService.survivalPlan(remaining, runway, student.getLivingType()));
    }

    private static void showSponsorReport(StudentUser student) {
        String report = SponsorReportService.buildReport(student, transactions, txCount);
        System.out.println("\n" + report);
        if (SponsorReportService.saveReport(student, report)) {
            System.out.println("Report saved to " + SponsorReportService.reportPath(student));
        }
    }

    // ==================== PART 5: ADMIN MENU ====================

    private static void adminMenu(Scanner input, User admin) {
        while (true) {
            System.out.println("\n=== Admin Menu (" + admin.getName() + ") ===");
            System.out.println("1. List users");
            System.out.println("2. Add or remove a user");
            System.out.println("3. Edit split templates");
            System.out.println("4. Edit currency rates");
            System.out.println("5. Aggregate stats (totals only)");
            System.out.println("6. Save and logout");

            int choice = InputHelper.readInt(input, "Select option (1-6): ", 1, 6);
            switch (choice) {
                case 1:
                    for (int i = 0; i < userCount; i++) {
                        System.out.println((i + 1) + ". " + users[i].getUserId() + " - "
                                + users[i].getName() + " [" + users[i].getUserRole() + "]");
                    }
                    break;
                case 2:
                    int action = InputHelper.readInt(input, "1 = Add Student, 2 = Remove User: ", 1, 2);
                    if (action == 1) {
                        registerStudent(input);
                    } else {
                        String delId = InputHelper.readNonEmpty(input, "Enter User ID to remove: ");
                        removeUser(delId, admin.getUserId());
                    }
                    break;
                case 3:
                    editTemplates(input);
                    break;
                case 4:
                    editCurrencyRates(input);
                    break;
                case 5:
                    showAggregateStats();
                    break;
                case 6:
                    saveAll();
                    System.out.println("Admin changes saved. Logging out...");
                    return;
            }
        }
    }

    private static void removeUser(String targetId, String currentAdminId) {
        if (targetId.equalsIgnoreCase(currentAdminId)) {
            System.out.println("You cannot delete the currently logged-in admin.");
            return;
        }
        for (int i = 0; i < userCount; i++) {
            if (users[i].getUserId().equalsIgnoreCase(targetId)) {
                for (int j = i; j < userCount - 1; j++) {
                    users[j] = users[j + 1];
                }
                users[--userCount] = null;
                System.out.println("Removed user " + targetId + ".");
                return;
            }
        }
        System.out.println("User ID not found.");
    }

    private static void editTemplates(Scanner input) {
        int typeChoice = InputHelper.readInt(input, "Edit template for (1 = HOSTEL, 2 = COMMUTER): ", 1, 2);
        String livingType = (typeChoice == 1) ? StudentUser.HOSTEL : StudentUser.COMMUTER;

        int food = InputHelper.readInt(input, "Food % (0-100): ", 0, 100);
        int transport = InputHelper.readInt(input, "Transport % (0-100): ", 0, 100);
        int academic = InputHelper.readInt(input, "Academic % (0-100): ", 0, 100);
        int savings = InputHelper.readInt(input, "Savings % (0-100): ", 0, 100);
        int other = InputHelper.readInt(input, "Other % (0-100): ", 0, 100);

        if (food + transport + academic + savings + other != 100) {
            System.out.println("Percentages must add up to exactly 100. Template not saved.");
            return;
        }
        new File("data").mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(TEMPLATE_FILE, true))) {
            pw.println(livingType + "|Food|" + food);
            pw.println(livingType + "|Transport|" + transport);
            pw.println(livingType + "|Academic|" + academic);
            pw.println(livingType + "|Savings|" + savings);
            pw.println(livingType + "|Other|" + other);
            System.out.println("Saved " + livingType + " split template to " + TEMPLATE_FILE + ".");
        } catch (IOException e) {
            System.out.println("Could not write template file.");
        }
    }

    private static void editCurrencyRates(Scanner input) {
        String[] codes = currencyService.getCodes();
        System.out.println("Current currencies (" + currencyService.getCount() + "):");
        for (int i = 0; i < codes.length; i++) {
            System.out.println("- " + codes[i]);
        }
        String code = InputHelper.readNonEmpty(input, "Currency code to add/update (e.g., USD, SGD): ").toUpperCase();
        String rate = InputHelper.readNonEmpty(input, "Rate for 1 MYR (e.g., 0.2150): ");
        if (currencyService.setRate(code, rate)) {
            System.out.println("Updated rate for " + code + ".");
        } else {
            System.out.println("Invalid currency code or rate (note: MYR base rate cannot be changed).");
        }
    }

    private static void showAggregateStats() {
        long grandTotal = 0;
        for (int i = 0; i < txCount; i++) {
            if (transactions[i] instanceof ExpenseTransaction) {
                grandTotal += transactions[i].getCents();
            }
        }
        System.out.println("\n--- Aggregate System Stats ---");
        System.out.println("Total Registered Users: " + userCount);
        System.out.println("Total Transactions:     " + txCount);
        System.out.println("Total System Spending:  " + Money.format(grandTotal));
    }

    // ==================== HELPERS ====================

    private static long totalSpentFor(String userId) {
        long sum = 0;
        for (int i = 0; i < txCount; i++) {
            if (transactions[i] instanceof ExpenseTransaction && transactions[i].getUserId().equals(userId)) {
                sum += transactions[i].getCents();
            }
        }
        return sum;
    }

    private static String formatInCurrency(long cents, String currencyCode) {
        if ("MYR".equalsIgnoreCase(currencyCode)) {
            return Money.format(cents);
        }
        long abs = Math.abs(cents);
        long minor = abs % 100;
        return currencyCode + " " + (cents < 0 ? "-" : "") + (abs / 100) + "." + (minor < 10 ? "0" : "") + minor;
    }

    private static void saveAll() {
        // TODO (when FileService is merged): save users, transactions, goals, budgets
    }
}