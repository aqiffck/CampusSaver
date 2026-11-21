# Member 1: File Storage and Admin Role

**Files:** `model/AdminUser.java`, `model/SponsorUser.java` (optional), `service/FileService.java`
**Branch:** `feature/member1-file-storage`
**Rules:** No `ArrayList`/`HashMap`/`List`. Plain arrays plus `int count`. Money is `long cents`. Follow `TEAM_CHECKLIST.md` section 5 and 7.

## Part 1: Start
- [ ] `git checkout main && git pull origin main`, then `git checkout -b feature/member1-file-storage`
- [ ] Read `User`, `StudentUser`, `Transaction`, `ExpenseTransaction` (now in cents, with `userId`).

## Part 2: AdminUser (do first, to unblock others)
- [ ] `public class AdminUser extends User` with field `department`.
- [ ] Constructor `AdminUser(String userId, String name, String baseCurrency, String department)`.
- [ ] `getDepartment()`, and override `getUserRole()` to return `"ADMIN"`.
- [ ] `SponsorUser extends User` (optional): constructor `(userId, name, baseCurrency)`, `getUserRole()` returns `"SPONSOR"`.

## Part 3: FileService
Constants: `data/users.txt`, `transactions.txt`, `budgets.txt`, `goals.txt`, `templates.txt`.

Rules for all files:
- Delimiter is `|`. Read with `line.split("\\|", -1)`.
- Before saving any text field (name, description, department), replace `|` with `/`.
- Skip blank lines. Skip any line with too few columns. Never crash on one bad line (catch `NumberFormatException`).
- If a file does not exist, return 0 (first run).
- Use a classic cast (`(StudentUser) u`), not `instanceof` patterns.

- [ ] `ensureDataFolder()`: `new File("data").mkdirs()` if missing.
- [ ] **users.txt:** `id|role|name|currency|livingType|scholarship|budgetCents|sponsorId`
  - Student: all columns.
  - Admin: put department in `livingType`, then `false|0|`.
  - Sponsor: `livingType` empty, then `false|0|`.
  - `saveUsers(User[] users, int count)` and `int loadUsers(User[] users)` rebuild the right subclass from `role`, then `loadUsers` and, for students, `setSponsorId(...)`.
- [ ] **transactions.txt:** `id|type|userId|cents|date|desc|category|isAcademic|goalName`
  - `type` is `EXPENSE` or `SAVINGS`.
  - For `SAVINGS`, put the source in `category` and the goal in `goalName`.
  - `saveTransactions(Transaction[] t, int count)` and `int loadTransactions(Transaction[] t)`.
- [ ] **goals.txt:** `goalId|userId|name|targetCents|savedCents`, with `saveGoals` and `loadGoals`.
- [ ] **budgets.txt:** `userId|category|percent`, with `saveBudgets` and `loadBudgets`. Use `BudgetPlan.add(category, percent)` when loading, grouped by `userId`.
- [ ] **templates.txt:** `livingType|category|percent`, with `saveTemplates` and `loadTemplates`. Create a default file with HOSTEL and COMMUTER splits if missing (each totals 100).
- [ ] `void saveReport(String fileName, String content)`: write to `data/<fileName>`.
- [ ] Every method catches `IOException` and prints a clear message. Never throws out of the class.

## Part 4: Test and push
- [ ] Compile: `javac --release 17 -d out src/campussaver/*/*.java`
- [ ] Write a small test: save, reload and compare user and transaction counts. Add a line with a missing column and confirm it is skipped.
- [ ] Zero collection imports.
- [ ] `git add` your own files only, commit, push, open a PR, and tell Members 2 and 3 once `AdminUser` and `FileService` are merged.
