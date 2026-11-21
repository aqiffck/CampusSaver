# Member 2 Coding Checklist — Core Services & Spare-Change Round-Up

**Your Files:**
1. `[NEW]` `src/campussaver/model/SavingsTransaction.java`
2. `[NEW]` `src/campussaver/service/UserService.java`
3. `[NEW]` `src/campussaver/service/TransactionService.java`

**Your Branch Name:** `feature/member2-core-services`  
**Golden Rule:** Do NOT use `ArrayList`, `LinkedList`, `HashMap`, or `List`. Store everything in plain Java arrays (`User[]`, `Transaction[]`) + `int` counters!

---

## Part 1: Before You Start
- [ ] Run `git checkout main && git pull origin main`
- [ ] Create your branch: `git checkout -b feature/member2-core-services`
- [ ] Inspect existing files you depend on:
  - `src/campussaver/model/User.java` & `StudentUser.java`
  - `src/campussaver/model/Transaction.java` & `ExpenseTransaction.java`
  - `src/campussaver/exception/LowRunwayException.java`

---

## Part 2: Build `SavingsTransaction.java` (Do This FIRST to Unblock Member 1!)
Create `src/campussaver/model/SavingsTransaction.java`:
- [ ] Package: `package campussaver.model;`
- [ ] Extends `Transaction`: `public class SavingsTransaction extends Transaction`
- [ ] Private fields:
  - `private final String sourceType;` (e.g., `"ROUND_UP"` or `"MANUAL"`)
  - `private final String goalName;` (e.g., `"EMERGENCY_JAR"`)
- [ ] Exact constructor:
  ```java
  public SavingsTransaction(String id, double amount, String date, String description, String sourceType, String goalName) {
      super(id, amount, date, description);
      this.sourceType = sourceType;
      this.goalName = goalName;
  }
  ```
- [ ] Getters: `getSourceType()`, `getGoalName()`
- [ ] Override `getSummary()`:
  `return "[SAVINGS - " + sourceType + "] " + getDescription() + ": +MYR " + String.format("%.2f", getAmount());`
- [ ] Override `toFileFormat()`:
  `return "SAVINGS," + getId() + "," + getAmount() + "," + getDate() + "," + getDescription() + "," + sourceType + "," + goalName;`

---

## Part 3: Build `UserService.java`
Create `src/campussaver/service/UserService.java` (`package campussaver.service;`):
- [ ] Plain array storage:
  ```java
  private User[] users = new User[50];
  private int userCount = 0;
  ```
- [ ] Implement manual array-resize helper `ensureUserCapacity()` (doubles `users.length` using a `for` loop if `userCount == users.length`).
- [ ] Implement `public StudentUser registerStudent(String name, String livingType, boolean hasScholarship, double budgetLimit)`:
  - Generate clean ID: `"U" + String.format("%03d", userCount + 1)` (e.g., `U001`).
  - Create `new StudentUser(id, name, "MYR", livingType, hasScholarship, budgetLimit)`, store at `users[userCount++]`, and return it.
- [ ] Implement `public AdminUser registerAdmin(String name, String department)`:
  - *(Wait for `AdminUser.java` from Member 1 or sync after Stage 1 merge.)*
  - Generate ID: `"A" + String.format("%03d", userCount + 1)` -> store and return `AdminUser`.
- [ ] Implement `public User findUserById(String userId)`:
  - Loop `0` to `userCount - 1`; return matching `User` (`equalsIgnoreCase`) or `null`.
- [ ] Implement getters/loader hook for `FileService` and `CampusSaverApp`:
  - `public User[] getUsers() { return users; }`
  - `public int getUserCount() { return userCount; }`
  - `public void setLoadedCount(int count) { this.userCount = count; }`

---

## Part 4: Build `TransactionService.java`
Create `src/campussaver/service/TransactionService.java` (`package campussaver.service;`):
- [ ] Plain array storage:
  ```java
  private Transaction[] transactions = new Transaction[200];
  private int transactionCount = 0;
  private int idSequence = 1;
  ```
- [ ] Implement manual array-resize helper `ensureTransactionCapacity()` (doubles `transactions.length` with a `for` loop when full).
- [ ] Implement helper `generateNextId()` (returns `"T" + String.format("%03d", idSequence++)`; also make sure `setLoadedCount(int count)` updates `idSequence = count + 1` so loaded IDs like `T001` don't collide on restart!).
- [ ] **Add Expense + `LowRunwayException` Business Rule:**
  ```java
  public ExpenseTransaction addExpense(StudentUser user, double amount, String date, String description, String category, boolean isAcademic) throws LowRunwayException
  ```
  - Validate `amount > 0` (if `<= 0`, throw `IllegalArgumentException("Amount must be greater than 0.")`).
  - Sanitize commas in `description` (`description.replace(",", ";")`) so `.txt` saving never breaks.
  - Create `ExpenseTransaction`, store in `transactions[transactionCount++]`.
  - Compute `double remaining = user.getMonthlyBudgetLimit() - getTotalExpenses();`.
  - If `remaining <= 0` OR `remaining < user.getMonthlyBudgetLimit() * 0.15`, **throw** `new LowRunwayException("LOW RUNWAY ALERT: Only MYR " + String.format("%.2f", remaining) + " remaining in your monthly budget!");`.
  - Return the created `ExpenseTransaction`.
- [ ] **Spare-Change Round-Up (`SavingsTransaction`):**
  ```java
  public SavingsTransaction addRoundUpSaving(double expenseAmount, String date, String sourceDesc)
  ```
  - Compute `double spareChange = Math.round((Math.ceil(expenseAmount) - expenseAmount) * 100.0) / 100.0;`
  - If `spareChange <= 0.001`, return `null` (amount was already a whole Ringgit like `10.00`).
  - Otherwise create `new SavingsTransaction(generateNextId(), spareChange, date, "Round-up: " + sourceDesc.replace(",", ";"), "ROUND_UP", "EMERGENCY_JAR")`, store in `transactions[transactionCount++]`, and return it.
- [ ] **Aggregation Helpers (Loop + `instanceof`):**
  - `public double getTotalExpenses()` -> sum `t.getAmount()` ONLY where `transactions[i] instanceof ExpenseTransaction`.
  - `public double getTotalAcademicExpenses()` -> sum where `t instanceof ExpenseTransaction e && e.isVerifiedAcademic()`.
  - `public double getTotalSavings()` -> sum where `transactions[i] instanceof SavingsTransaction`.
- [ ] **Search & Shift-Left Delete:**
  - `public int searchByKeyword(String keyword, Transaction[] results)`:
    - Loop `0..transactionCount-1`; if `getDescription().toLowerCase().contains(keyword.toLowerCase())` or `getId().equalsIgnoreCase(keyword)`, copy into `results[matchCount++]`; return `matchCount`.
  - `public boolean deleteById(String id)`:
    - Find index `foundIndex` via `for` loop. If `-1`, return `false`.
    - Shift elements left (`transactions[i] = transactions[i + 1]`), set `transactions[transactionCount - 1] = null`, `transactionCount--`, return `true`.
- [ ] **Array Accessors:**
  - `getTransactions()`, `getTransactionCount()`, `setLoadedCount(int count)`.

---

## Part 5: Self-Test Before Pushing
- [ ] Compile cleanly from `CampusSaver/` root:
  ```bash
  javac --release 17 -d out src/campussaver/exception/*.java src/campussaver/model/*.java src/campussaver/service/UserService.java src/campussaver/service/TransactionService.java
  ```
- [ ] Confirm **0** `ArrayList`, `LinkedList`, `HashMap`, or `List` imports anywhere in your 3 files.
- [ ] Commit & push ONLY your 3 files (`SavingsTransaction.java`, `UserService.java`, `TransactionService.java`):
  ```bash
  git add src/campussaver/model/SavingsTransaction.java src/campussaver/service/UserService.java src/campussaver/service/TransactionService.java
  git commit -m "Add SavingsTransaction, UserService, and TransactionService with plain arrays"
  git push -u origin feature/member2-core-services
  ```
- [ ] Open Pull Request on GitHub and notify Member 1 & Member 3!
