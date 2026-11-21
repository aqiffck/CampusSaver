# Member 2: Core Services and Spare-Change (F4)

**Files:** `model/SavingsTransaction.java`, `model/SavingsGoal.java`, `service/UserService.java`, `service/TransactionService.java`
**Branch:** `feature/member2-core-services`
**Rules:** Plain arrays plus counters. Money is `long cents`. Follow `TEAM_CHECKLIST.md` section 5.

## Part 1: Start
- [ ] `git checkout main && git pull origin main`, then `git checkout -b feature/member2-core-services`

## Part 2: Models (do first, to unblock Member 1)
- [ ] `SavingsTransaction extends Transaction`: fields `sourceId`, `goalName`.
  - Constructor `(String id, String userId, long cents, String date, String description, String sourceId, String goalName)`.
  - Override `getSummary()` and `toFileFormat()` (different from `ExpenseTransaction`, which is the polymorphism the rubric wants).
- [ ] `SavingsGoal`: `goalId`, `userId`, `name`, `targetCents`, `savedCents`, with getters, `deposit(long cents)` and `percentDone()`.

## Part 3: UserService
- [ ] `User[] users = new User[50]` plus `int userCount`. Write a manual resize helper (copy with a `for` loop).
- [ ] `registerStudent(String name, String livingType, boolean hasScholarship, long budgetCents)` creates ID `U001`, `U002`, etc.
  - Validate that name is not empty and budget is above 0.
- [ ] `registerAdmin(String name, String department)` creates ID `A001`, etc.
- [ ] `findUserById(String id)` returns the user or `null`.
- [ ] `getUsers()`, `getUserCount()`, `setLoadedCount(int)`.
- [ ] Business rule: a student cannot register with a duplicate name.

## Part 4: TransactionService
- [ ] `Transaction[] transactions = new Transaction[200]`, `int transactionCount`, `int idSequence`. Add a resize helper.
  - `setLoadedCount(int c)` must also set `idSequence = c + 1`, so IDs never collide after a restart.
- [ ] `addExpense(StudentUser u, long cents, String date, String desc, String category, boolean academic) throws LowRunwayException`
  - Reject `cents <= 0` with `IllegalArgumentException`. Replace `|` in `desc`.
  - Save the expense first.
  - Then `remaining = budget - getTotalExpenses(u.getUserId())`.
  - If `remaining <= 0` or `remaining < 15%` of budget, throw `LowRunwayException` (message only).
  - The 14-day survival plan is separate, in `PredictorService`.
- [ ] `addRoundUpSaving(StudentUser u, long expenseCents, String date, String sourceId)`
  - Use `Money.roundUpToRinggit(expenseCents)`. If the result is 0, return `null`.
  - Otherwise save a `SavingsTransaction`. Message wording is "tracks savings", never "real savings".
- [ ] Totals, each filtered by `userId`: `getTotalExpenses`, `getTotalAcademic`, `getTotalSavings`. Use classic casts.
- [ ] `searchByKeyword(String keyword, Transaction[] results)`: match on description or ID, case-insensitive, and return the match count.
- [ ] `deleteById(String id)`: shift elements left, set the last slot to `null`, decrement the counter.
- [ ] Goals: `SavingsGoal[] goals` plus `goalCount`. Add `addGoal(String userId, String name, long targetCents)`, `getGoals()`, `getGoalCount()`, `setLoadedGoalCount(int)`.
  - The round-up deposits into a chosen goal.
  - Reject a goal with a target of 0 or less.
- [ ] Accessors: `getTransactions()`, `getTransactionCount()`.

## Part 5: Test and push
- [ ] Compile: `javac --release 17 -d out src/campussaver/*/*.java`
- [ ] Test: RM 7.40 (740 cents) round-up gives 60 cents. RM 10.00 gives `null`.
- [ ] Test: delete the first, middle and last items. Test an expense of 0 and a negative amount.
- [ ] Zero collection imports.
- [ ] Push your own files only, open a PR, and notify Members 1 and 3.
