# Member 3 Coding Checklist — Interactive `Scanner` CLI & Multi-Currency UI

**Your File:**
1. `[UPDATE]` `src/campussaver/main/CampusSaverApp.java`

**Your Branch Name:** `feature/member3-cli-ui`  
**Golden Rule:** Your `Scanner` menu must NEVER crash when a user types letters instead of numbers, enters bad dates, or triggers `LowRunwayException` / `AIServiceException`.

---

## Part 1: Before You Start
- [ ] Wait until Stage 1 & Stage 2 classes (`AdminUser`, `SavingsTransaction`, `FileService`, `UserService`, `TransactionService`, `AIHelper`, `AIService`) are merged to `main` (or pull their branch signatures).
- [ ] Run `git checkout main && git pull origin main`
- [ ] Create your branch: `git checkout -b feature/member3-cli-ui`

---

## Part 2: Build Crash-Proof `Scanner` Input Helpers
Inside `CampusSaverApp.java`, NEVER mix `sc.nextInt()`/`sc.nextDouble()` with `sc.nextLine()`. Always use `sc.nextLine()` inside helper methods:
- [ ] `readNonEmptyString(Scanner sc, String prompt)` — keeps prompting if `.trim().isEmpty()`.
- [ ] `readInt(Scanner sc, String prompt)` — `Integer.parseInt(sc.nextLine().trim())` inside `while (true)` + `try/catch (NumberFormatException)`.
- [ ] `readDouble(Scanner sc, String prompt)` — `Double.parseDouble(sc.nextLine().trim())` inside `while (true)` + `try/catch (NumberFormatException)` (check `> 0` for money amounts!).
- [ ] `readYesNo(Scanner sc, String prompt)` — accepts `Y`/`YES` (`true`) or `N`/`NO` (`false`), loops on invalid input.

---

## Part 3: Startup & User Login / Onboarding
- [ ] Instantiate all 4 services + `Scanner`:
  ```java
  FileService fileService = new FileService();
  UserService userService = new UserService();
  TransactionService transService = new TransactionService();
  AIService aiService = new AIService();
  Scanner sc = new Scanner(System.in);
  ```
- [ ] Load saved data on startup:
  ```java
  fileService.ensureDataFolder();
  int loadedUsers = fileService.loadUsers(userService.getUsers());
  userService.setLoadedCount(loadedUsers);
  int loadedTx = fileService.loadTransactions(transService.getTransactions());
  transService.setLoadedCount(loadedTx);
  ```
- [ ] If `userService.getUserCount() == 0` (First Run):
  - Prompt to register a `StudentUser` (`name`, `livingType` = `HOSTEL` or `COMMUTER`, `hasScholarship` = `Y/N`, `monthlyBudgetLimit`).
  - Ask if they also want to create an `AdminUser` (`name`, `department`) for testing Option 5.
- [ ] If `userService.getUserCount() > 0` (Returning Run):
  - List saved accounts (`[1] Ali (STUDENT)`, `[2] Aqif (ADMIN)`) and let the user pick which account to log into (plus an option to register a new account).

---

## Part 4: Build the 6-Option Main Menu Loop

```text
=== CampusSaver CLI Menu ===
User: Ali (STUDENT) | Base Currency: MYR
1. Add New Expense (+ Optional Spare-Change Round-Up)
2. View Financial Summary & Multi-Currency Display
3. Search or Delete a Transaction
4. Get AI Financial Advice
5. [Admin Only] Export Summary Report (report.txt)
6. Save Data & Exit
```

- [ ] **Option 1 — Add Expense + Optional Round-Up:**
  - Read `amount (> 0)`, `date` (e.g., `2026-11-21`), `description`, `category` (`FOOD`, `BOOKS`, `TRANSPORT`, `OTHER`), `isAcademic` (`Y/N`).
  - Verify active user is `StudentUser` (if logged in as `AdminUser`, pick a student or print a clear message).
  - Wrap call in `try/catch`:
    ```java
    try {
        transService.addExpense(student, amount, date, description, category, isAcademic);
        System.out.println("Expense recorded!");
    } catch (LowRunwayException e) {
        System.out.println("\n[!!!] " + e.getMessage());
    }
    ```
  - Ask `"Round up spare change to Emergency Savings Jar? (Y/N): "` -> if `Y`, call `transService.addRoundUpSaving(amount, date, description)` and print the saved amount if non-null.

- [ ] **Option 2 — Financial Summary & Multi-Currency Display:**
  - Ask display currency (`1=MYR`, `2=USD`, `3=SGD`, `4=IDR`):
    - `MYR`: `1.0` | `USD`: `0.22` | `SGD`: `0.30` | `IDR`: `3500.0`
  - Print `Total Spent`, `Academic Spent`, `Round-Up Savings Jar`, and `Remaining Budget` multiplied by the selected rate (formatted cleanly with `String.format("%.2f", ...)`).
  - Loop `0` to `transService.getTransactionCount() - 1` and print `transService.getTransactions()[i].getSummary()`.

- [ ] **Option 3 — Search or Delete Transaction:**
  - Sub-choice: `[1] Search by Keyword` or `[2] Delete by Transaction ID`.
  - For Search: create `Transaction[] matches = new Transaction[transService.getTransactionCount()];`, call `int found = transService.searchByKeyword(keyword, matches);`, print matches (or `"No matching transactions."`).
  - For Delete: call `boolean deleted = transService.deleteById(id);` and print confirmation or `"Transaction ID not found."`.

- [ ] **Option 4 — AI Financial Insights:**
  - Ask `daysPassedInMonth` (loop until `>= 1 && <= 31`).
  - Call `aiService.generateFinancialAdvice(student, transService.getTransactions(), transService.getTransactionCount(), daysPassed)` inside `try { ... } catch (AIServiceException e) { System.out.println("[Notice] " + e.getMessage()); }`.

- [ ] **Option 5 — Admin Report Export (`report.txt`):**
  - Check `if (!(currentUser instanceof AdminUser))`: print `"Access Denied: Only AdminUser can export reports."` and return to menu.
  - Build a formatted multi-line report string showing total users, total transactions, total expenses, academic ratio, total round-up savings, and remaining student budgets.
  - Print the report on screen AND call `fileService.saveReport(reportText)` -> print `"Saved to data/report.txt"`.

- [ ] **Option 6 — Save Data & Exit:**
  - Call `fileService.saveUsers(userService.getUsers(), userService.getUserCount());`
  - Call `fileService.saveTransactions(transService.getTransactions(), transService.getTransactionCount());`
  - Print `"Data saved to data/users.txt & data/transactions.txt. Goodbye!"`, close `sc`, exit `while` loop cleanly.

---

## Part 5: Self-Test Before Pushing
- [ ] Compile the entire project from `CampusSaver/` root:
  ```bash
  mkdir out
  javac --release 17 -d out src/campussaver/exception/*.java src/campussaver/model/*.java src/campussaver/service/*.java src/campussaver/main/*.java
  ```
- [ ] Run `java -cp out campussaver.main.CampusSaverApp` and intentionally type `"abc"` into every number prompt to confirm **0 crashes**.
- [ ] Commit & push ONLY `CampusSaverApp.java`:
  ```bash
  git add src/campussaver/main/CampusSaverApp.java
  git commit -m "Implement full CampusSaverApp Scanner CLI with multi-currency and round-up flows"
  git push -u origin feature/member3-cli-ui
  ```
