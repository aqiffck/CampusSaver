# CampusSaver: Master Team Checklist

> **Java:** JDK 17 (`javac --release 17`). Use classic casts, not `instanceof` pattern matching.
> **Rule #1 (WIX1002):** No `ArrayList`, `LinkedList`, `HashMap`, `HashSet` or `List` anywhere in `src/`. Plain arrays plus an `int` counter only.
> **Money:** always `long cents`, never `double`. Format only when printing, using `Money.format(long cents)`.
> **Deadline:** 10 Dec 2026, 11:00 pm.

---

## 1. Repo status (`src/campussaver/`)

| File | Owner | Status |
| :--- | :--- | :---: |
| `exception/AIServiceException.java` | Shared | Done |
| `exception/LowRunwayException.java` | Shared | Done |
| `model/User.java` (abstract) | Shared | Done |
| `model/StudentUser.java` | Shared | Update to cents |
| `model/Transaction.java` (abstract) | Shared | Update to cents |
| `model/ExpenseTransaction.java` | Shared | Update to cents |
| `model/AdminUser.java` | Member 1 | To build |
| `model/SponsorUser.java` (optional) | Member 1 | To build |
| `model/SavingsTransaction.java` | Member 2 | To build |
| `model/SavingsGoal.java` | Member 2 | To build |
| `model/BudgetPlan.java` | Member 4 | To build |
| `service/FileService.java` | Member 1 | To build |
| `service/UserService.java` | Member 2 | To build |
| `service/TransactionService.java` | Member 2 | To build |
| `service/BudgetService.java` | Member 4 | To build |
| `service/PredictorService.java` | Member 4 | To build |
| `service/AIHelper.java` | Member 4 | To build |
| `service/AIService.java` | Member 4 | Update |
| `service/CurrencyService.java` | Member 4 | To build |
| `util/InputHelper.java` | Member 3 | To build |
| `util/Money.java` | Member 3 | To build |
| `main/CampusSaverApp.java` | Member 3 | Update (terminal test menu) |
| `main/WebServer.java` | Member 4 | New (HttpServer, connects web/ to services) |
| `web/` (HTML, CSS, JS) | Member 3 | New |

---

## 2. Feature tiers

- **Must:** F1 Smart AI Budget Setup, F2 Spending Predictor and Alerts, F5 Cash Runway and Survival Plan
- **Should:** F4 Spare-Change Savings Bucket, F6 Sponsor Report Builder
- **Stretch:** F3 Multi-Currency (`rates.txt` first, then live fetch. The lecturer approved it: standard Java only, plain arrays, fall back to `rates.txt` if the request fails)

Do not start a Should feature until all Must features work offline.

---

## 3. Role rules

- **Student:** add, search and delete own expenses, set budget, view predictions, runway and plan, manage goals, generate the sponsor report.
- **Admin:** manage users, default split templates, currency rates, view aggregate stats. Cannot see individual transactions.
- **Sponsor (optional):** read-only report for linked students only.

---

## 4. Build order

| Days | Work |
| :--- | :--- |
| 1-3 | **Stage 1:** shared models in cents, `AdminUser`, `SavingsTransaction`, file formats (section 7) on `main` |
| 4-7 | **Stage 2:** services in parallel. Must features work offline by Day 7. |
| 8-14 | **Stage 3:** web frontend (`web/` + `WebServer`), role pages, F4, F6. **Code freeze on Day 14** except bug fixes. |
| 15-17 | **Stage 4:** full-team tests, rule checks (section 6) |
| 18-19 | Docs, SDG text, pitch and demo script, submit with buffer |

---

## 5. Locked method signatures (do NOT rename)

| Class | Signatures |
| :--- | :--- |
| `Money` | `static String format(long cents)`, `static long parse(String text)` (returns -1 if invalid), `static long roundUpToRinggit(long cents)` (returns the extra cents to add) |
| `AdminUser` | `new AdminUser(String userId, String name, String baseCurrency, String department)` |
| `StudentUser` | `new StudentUser(String userId, String name, String baseCurrency, String livingType, boolean hasScholarship, long monthlyBudgetCents)` |
| `ExpenseTransaction` | `new ExpenseTransaction(String id, String userId, long cents, String date, String description, String category, boolean isVerifiedAcademic)` |
| `SavingsTransaction` | `new SavingsTransaction(String id, String userId, long cents, String date, String description, String sourceId, String goalName)` |
| `Transaction` | `long getCents()` (replaces `getAmount()`) |
| `FileService` | `void ensureDataFolder()`, `int loadUsers(User[] users)`, `void saveUsers(User[] users, int count)`, `int loadTransactions(Transaction[] t)`, `void saveTransactions(Transaction[] t, int count)`, `void saveReport(String fileName, String content)` |
| `UserService` | `StudentUser registerStudent(String name, String livingType, boolean hasScholarship, long budgetCents)`, `AdminUser registerAdmin(String name, String department)`, `User findUserById(String id)`, `User[] getUsers()`, `int getUserCount()`, `void setLoadedCount(int count)` |
| `TransactionService` | `ExpenseTransaction addExpense(StudentUser u, long cents, String date, String desc, String category, boolean academic) throws LowRunwayException`, `SavingsTransaction addRoundUpSaving(StudentUser u, long expenseCents, String date, String sourceId)`, `long getTotalExpenses(String userId)`, `long getTotalAcademic(String userId)`, `long getTotalSavings(String userId)`, `int searchByKeyword(String keyword, Transaction[] results)`, `boolean deleteById(String id)`, `Transaction[] getTransactions()`, `int getTransactionCount()`, `void setLoadedCount(int count)` |
| `PredictorService` | `int remainingRunwayDays(StudentUser u, long spentCents, int daysPassed)`, `boolean isOverspending(StudentUser u, long spentCents, int daysPassed)`, `long projectedMonthEnd(long spentCents, int daysPassed)`, `static final int LOW_RUNWAY_DAYS = 14` |
| `BudgetService` | `BudgetPlan buildFromAI(StudentUser u) ` (falls back to the template on any failure), `boolean totalsHundred(BudgetPlan p)` |
| `AIHelper` | `static String ask(String prompt) throws AIServiceException` |
| `AIService` | `String budgetSplitPrompt(...)`, `String tipsPrompt(...)`, `String survivalPlanPrompt(...)`, `String sponsorWordingPrompt(...)`, plus fallback text methods. Each catches `AIServiceException` and returns the fallback. |
| `CurrencyService` | `void loadRates()`, `long convert(long cents, String from, String to)`, `boolean setRate(String code, String rateText)`, `String[] getCodes()`, `int getCount()` |
| `InputHelper` | `int readInt(Scanner sc, String prompt, int min, int max)`, `long readMoney(Scanner sc, String prompt)`, `String readNonEmpty(Scanner sc, String prompt)`, `boolean readYesNo(Scanner sc, String prompt)` |
- Web input: the browser sends text, so `InputHelper` also gets String versions (e.g. `parseInt(String s, int min, int max)`, `parseMoney(String s)`) that throw an exception on bad input. The Scanner versions above stay. Adding methods is fine, renaming is not.
**Notes**
  
- `LowRunwayException` is thrown **after** the expense is saved, so the warning never loses data.
- Java calculates every number. The AI only writes tips, plans and wording.
- Survival plan must never advise skipping meals, must point to the university financial aid office, and must end with: "General guidance, not financial advice."
- Spare-change is virtual. Say "tracks savings".

---

## 6. Final checks (whole team, Days 15-17)

- [ ] **Compile:**
  ```
  mkdir out
    Windows PowerShell:
  javac --release 17 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName

  Mac/Linux:
  javac --release 17 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
  ```
  0 errors.
- [ ] **No collections:** search `src/` for `ArrayList`, `LinkedList`, `HashMap`, `HashSet`, `java.util.List`. 0 matches.
- [ ] **10+ classes:** abstract `User` and `Transaction`, each with 2+ subclasses and overridden methods.
- [ ] **Bad input:** letters where numbers are expected, empty input, negative money, huge numbers. No crash.
- [ ] **Offline AI:** delete or rename `config.txt` and disconnect Wi-Fi. `AIServiceException` is caught, the fallback shows, the app keeps running, and the notice goes in `ai_log.txt`.
- [ ] **Live AI:** real key in `config.txt`, reply is shown and appended to `ai_log.txt`. Run `git status` and confirm `config.txt` is not tracked.
- [ ] **Split check:** force a bad AI reply (total not 100). The template fallback is used.
- [ ] **Restart persistence:** save, exit, rerun. Users, expenses, budgets and goals reload.
- [ ] **Low runway:** under 14 days shows the survival plan with safe advice and the disclaimer.
- [ ] **Roles:** Student, Admin and (if built) Sponsor each see only their own pages and data.
- [ ] **Report:** sponsor report saved to `data/` and readable. Every number comes from Java.
- [ ] **Comments:** every class and key method is commented.

---

## 7. File formats (`data/`, pipe-delimited, money in cents)

```
users.txt         id|role|name|currency|livingType|scholarship|budgetCents|sponsorId
transactions.txt  id|type|userId|cents|date|desc|category|isAcademic|goalName
budgets.txt       userId|category|percent
goals.txt         goalId|userId|name|targetCents|savedCents
templates.txt     livingType|category|percent
rates.txt         MYR|1.0000   USD|0.2100   SGD|0.2900
ai_log.txt        timestamp|feature|PROMPT|...|REPLY|...   (append only)
```

Reports are saved as `data/report_<userId>.txt`. `config.txt` (root, gitignored) holds `API_KEY=...`.
