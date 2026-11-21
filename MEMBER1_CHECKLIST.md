# Member 1 Coding Checklist — File Persistence & Admin Role

**Your Files:**
1. `[NEW]` `src/campussaver/model/AdminUser.java`
2. `[NEW]` `src/campussaver/service/FileService.java`

**Your Branch Name:** `feature/member1-file-storage`  
**Golden Rule:** Do NOT use `ArrayList`, `LinkedList`, `HashMap`, or `List`. Work ONLY with plain arrays (`User[]`, `Transaction[]`) + `int count`!

---

## Part 1: Before You Start
- [ ] Run `git checkout main && git pull origin main`
- [ ] Create your branch: `git checkout -b feature/member1-file-storage`
- [ ] Read existing files so you know how `User`, `StudentUser`, `Transaction`, and `ExpenseTransaction` work:
  - `src/campussaver/model/User.java` (has `userId`, `name`, `baseCurrency`, abstract `getUserRole()`)
  - `src/campussaver/model/StudentUser.java` (has `livingType`, `hasScholarship`, `monthlyBudgetLimit`)
  - `src/campussaver/model/ExpenseTransaction.java` (note: `toFileFormat()` returns **6 comma-separated columns**: `id,amount,date,description,category,isVerifiedAcademic`)

---

## Part 2: Build `AdminUser.java` (Do This FIRST to Unblock Teammates!)
Create `src/campussaver/model/AdminUser.java`:
- [ ] Package declaration: `package campussaver.model;`
- [ ] Class header: `public class AdminUser extends User`
- [ ] Private field: `private String department;` (e.g., `"Campus Treasurer"`)
- [ ] Exact constructor:
  ```java
  public AdminUser(String userId, String name, String baseCurrency, String department) {
      super(userId, name, baseCurrency);
      this.department = department;
  }
  ```
- [ ] Getter: `public String getDepartment() { return department; }`
- [ ] Override abstract method from `User`:
  ```java
  @Override
  public String getUserRole() {
      return "ADMIN";
  }
  ```

---

## Part 3: Build `FileService.java`
Create `src/campussaver/service/FileService.java` (`package campussaver.service;`).

### A. Folder Setup & Constants
- [ ] Define file path constants:
  - `private static final String DATA_DIR = "data";`
  - `private static final String USERS_FILE = "data/users.txt";`
  - `private static final String TRANSACTIONS_FILE = "data/transactions.txt";`
  - `private static final String REPORT_FILE = "data/report.txt";`
- [ ] Implement `public void ensureDataFolder()`:
  - Check `File dir = new File(DATA_DIR);` -> if `!dir.exists()`, call `dir.mkdirs();`.

### B. User Save & Load (`data/users.txt`)
Format per line (7 columns):
- Student: `STUDENT,U001,Ali,MYR,HOSTEL,true,800.0`
- Admin: `ADMIN,A001,Aqif,MYR,Campus Treasurer,false,0.0`

- [ ] Implement `public void saveUsers(User[] users, int userCount)`:
  - Call `ensureDataFolder()`.
  - Use `try (PrintWriter out = new PrintWriter(new FileWriter(USERS_FILE)))`.
  - Loop `for (int i = 0; i < userCount; i++)`:
    - Sanitize `users[i].getName()` by replacing `,` with `;` so commas never break file columns.
    - If `users[i] instanceof StudentUser s`: write `"STUDENT," + s.getUserId() + "," + safeName + "," + s.getBaseCurrency() + "," + s.getLivingType() + "," + s.isHasScholarship() + "," + s.getMonthlyBudgetLimit()`.
    - Else if `users[i] instanceof AdminUser a`: write `"ADMIN," + a.getUserId() + "," + safeName + "," + a.getBaseCurrency() + "," + safeDept + ",false,0.0"`.
  - Catch `IOException` and print a friendly error message (never crash!).

- [ ] Implement `public int loadUsers(User[] users)`:
  - If `new File(USERS_FILE)` does not exist, return `0`.
  - Use `try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE)))`.
  - Loop `while ((line = br.readLine()) != null)`:
    - Skip blank lines (`if (line.trim().isEmpty()) continue;`).
    - Split: `String[] p = line.split(",");`
    - Guard length (`if (p.length < 7) continue;`) and array capacity (`if (count >= users.length) break;`).
    - If `"STUDENT".equals(p[0])`: parse `boolean scholarship = Boolean.parseBoolean(p[5]);` and `double limit = Double.parseDouble(p[6]);`, store `new StudentUser(p[1], p[2], p[3], p[4], scholarship, limit)` in `users[count++]`.
    - Else if `"ADMIN".equals(p[0])`: store `new AdminUser(p[1], p[2], p[3], p[4])` in `users[count++]`.
  - Wrap parsing in `try/catch (IOException | NumberFormatException e)` so one bad line never crashes startup.
  - Return `count`.

### C. Transaction Save & Load (`data/transactions.txt`)
> **Important Check on Existing Code:** `ExpenseTransaction.toFileFormat()` already exists in the repo and returns **6 columns** (`id,amount,date,description,category,isVerifiedAcademic`) without `"EXPENSE,"` at the front. Meanwhile `SavingsTransaction.toFileFormat()` starts with `"SAVINGS,"` (7 columns). Handle both cleanly!

- [ ] Implement `public void saveTransactions(Transaction[] transactions, int count)`:
  - Call `ensureDataFolder()`.
  - Loop `for (int i = 0; i < count; i++)`:
    - If `transactions[i] instanceof ExpenseTransaction`: write `"EXPENSE," + transactions[i].toFileFormat()`.
    - Else (`SavingsTransaction` already starts with `"SAVINGS,"`): write `transactions[i].toFileFormat()`.
- [ ] Implement `public int loadTransactions(Transaction[] transactions)`:
  - If `new File(TRANSACTIONS_FILE)` does not exist, return `0`.
  - Read line by line, skip empty lines, split by `,`.
  - If `"EXPENSE".equals(p[0])` and `p.length >= 7`:
    - `double amt = Double.parseDouble(p[2]);`
    - `boolean academic = Boolean.parseBoolean(p[6]);`
    - `transactions[count++] = new ExpenseTransaction(p[1], amt, p[3], p[4], p[5], academic);`
  - Else if `"SAVINGS".equals(p[0])` and `p.length >= 7`:
    - *(Wait until Member 2 merges `SavingsTransaction.java` — or ask Member 2 for its exact 6-arg constructor!)*
    - `double amt = Double.parseDouble(p[2]);`
    - `transactions[count++] = new SavingsTransaction(p[1], amt, p[3], p[4], p[5], p[6]);`
  - Return `count`.

### D. Admin Report Export (`data/report.txt`)
- [ ] Implement `public void saveReport(String reportContent)`:
  - Call `ensureDataFolder()`.
  - Write `reportContent` to `REPORT_FILE` (`data/report.txt`) using `PrintWriter`.

---

## Part 4: Self-Test Before Pushing
- [ ] Test compiling your files from `CampusSaver/` root:
  ```bash
  javac --release 17 -d out src/campussaver/model/User.java src/campussaver/model/StudentUser.java src/campussaver/model/AdminUser.java src/campussaver/model/Transaction.java src/campussaver/model/ExpenseTransaction.java src/campussaver/model/SavingsTransaction.java src/campussaver/service/FileService.java
  ```
- [ ] Verify you have **0** imports from `java.util.ArrayList`, `java.util.List`, or `java.util.HashMap`.
- [ ] Commit & push ONLY your own files (`AdminUser.java`, `FileService.java`):
  ```bash
  git add src/campussaver/model/AdminUser.java src/campussaver/service/FileService.java
  git commit -m "Add AdminUser model and FileService .txt persistence"
  git push -u origin feature/member1-file-storage
  ```
- [ ] Open Pull Request on GitHub and tell Member 2 & Member 3 when `AdminUser` and `FileService` are merged!
