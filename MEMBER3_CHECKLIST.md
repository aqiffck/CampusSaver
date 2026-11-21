# Member 3: Scanner CLI, Safe Input and Currency

**Files:** `main/CampusSaverApp.java`, `util/InputHelper.java`, `util/Money.java`
**Branch:** `feature/member3-cli-ui`
**Rule:** The menu must never crash on letters, empty input, negative or huge numbers, or on `LowRunwayException` and `AIServiceException`.

## Part 1: Start
- [ ] `git checkout main && git pull origin main`, then `git checkout -b feature/member3-cli-ui`
- [ ] Do Part 2 first. Other members need `Money` and `InputHelper`.

## Part 2: Utilities (push these first)
- [ ] `Money`: `format(long cents)` returns `"RM 12.34"`, `parse(String text)` returns cents or -1 if invalid, `roundUpToRinggit(long cents)` returns the extra cents to the next whole Ringgit (0 if already whole). Use integer maths, no `double`.
- [ ] `InputHelper`: always use `sc.nextLine()`, never `nextInt()`.
  - `readInt(Scanner, String prompt, int min, int max)`
  - `readMoney(Scanner, String prompt)`: returns cents above 0, with a sensible upper limit
  - `readNonEmpty(Scanner, String prompt)`
  - `readYesNo(Scanner, String prompt)`
  - Each loops until the input is valid.

## Part 3: Currency (written by Member 4)
- [ ] Do not write `CurrencyService`. Member 4 provides `loadRates()`, `convert(long cents, String from, String to)`, `setRate(String code, String rateText)`, `getCodes()` and `getCount()`.
- [ ] Call `loadRates()` at startup. Use `convert(...)` for the currency switch in the Student summary, and `setRate(...)` for the Admin "edit currency rates" option.

## Part 4: Startup and login
- [ ] Create all services and one `Scanner`. Load users, transactions, goals, budgets and templates through `FileService`, and set the loaded counts.
- [ ] First run: register a student (name, HOSTEL or COMMUTER, scholarship Y/N, monthly budget) and offer to create an admin.
- [ ] Returning run: list accounts, pick one, or register a new account.

## Part 5: Menus per role (call the services, keep logic out of the menu)
**Student**
1. Add expense, then ask about round-up into a goal
2. Summary, with currency switch
3. Search or delete a transaction
4. Budget setup (F1, Member 4)
5. Spending predictor and alerts (F2, Member 4)
6. Runway and survival plan (F5, Member 4)
7. Savings goals (F4)
8. Sponsor report (F6, Member 4)
9. Save and exit

**Admin:** list users, add or remove a user, edit split templates, edit currency rates, aggregate stats (totals only, no individual transactions), save and exit.

**Sponsor (optional):** view the report for linked students only, then exit.

- [ ] Catch `LowRunwayException`: show a warning after the expense is saved.
- [ ] Catch `AIServiceException` around every AI feature: show the fallback text and continue.
- [ ] Days passed in month = `LocalDate.now().getDayOfMonth()`.
- [ ] Use classic casts, not `instanceof` patterns.

## Part 6: Test and push
- [ ] Compile: `javac --release 17 -d out src/campussaver/*/*.java`
- [ ] Type `abc`, empty, `-5`, `99999999999` into every prompt. Zero crashes.
- [ ] Compile (Windows PowerShell): `javac --release 17 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName`
- [ ] Push your own files only and open a PR.
