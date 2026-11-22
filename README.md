# CampusSaver: Smart Budget Planner for University Students

A Java application with an HTML/CSS/JavaScript browser frontend and a Java backend that helps students plan their monthly budget, track spending, and avoid running out of cash before the month ends. Built for the WIX1002 group assignment and the FOP Hackathon, **Track 8 (Fintech)**.

**Team:** [Member 1], [Member 2], [Member 3], [Member 4] | **Due:** 10 Dec 2026, 11:00 pm

## Why it matters (UN SDGs)
- **SDG 4, Quality Education:** students who run out of money mid-semester risk missing meals, books or fees. CampusSaver helps them plan so money lasts until the month ends.
- **SDG 1, No Poverty:** it builds financial habits, shows early warnings, and points students in trouble to the university financial aid office.

## Roles
| Role | Can do |
| :--- | :--- |
| **Student** | Add, search and delete expenses; set a budget; see predictions, runway and a survival plan; track savings goals; build a sponsor report |
| **Admin** | Manage users, default split templates and currency rates; see aggregate stats only (never individual transactions) |
| **Sponsor** (optional) | Read-only report for linked students |

## Features
| Tier | Feature |
| :--- | :--- |
| Must | **F1** Smart Budget Setup: AI proposes a split by lifestyle (hostel or commuter); Java checks it totals 100% |
| Must | **F2** Spending Predictor and Overspending Alerts: Java predicts, AI writes tips |
| Must | **F5** Cash Runway and Survival Plan: days of cash left; plan shown if under 14 days |
| Should | **F4** Spare-Change Savings Bucket: rounds expenses up to the next Ringgit and *tracks* the savings in goals (virtual) |
| Should | **F6** Sponsor Report: academic vs personal spending, saved to a file |
| Stretch | **F3** Multi-Currency (MYR, USD, SGD): live rates fetched by `CurrencyService`, saved to `rates.txt`, with `rates.txt` as the fallback if there is no internet |

## How it works

**Golden rule: Java calculates, AI only writes words.** Every number (totals, runway, percentages, report figures) comes from Java. The AI only proposes a split, writes tips and plans, and words the report. If the AI is unavailable, the app shows a fallback and keeps running.

```
Add expense
    Web page (web/) -> WebServer -> InputHelper validates input
  -> TransactionService saves expense in Transaction[] (cents)
  -> optional round-up -> SavingsTransaction into a SavingsGoal
  -> if budget almost gone: LowRunwayException (after saving) -> warning shown

Budget setup (F1)
  BudgetService -> AIService builds prompt -> AIHelper.ask()
  -> parse category:percent lines -> Java checks total = 100
  -> if bad reply or AIServiceException: use template from templates.txt

Predictor and runway (F2, F5)
  PredictorService (pure Java): burn rate, projected month end, days left
  -> if over budget or days left < 14 -> AIService asks AI for tips / survival plan
  -> AIServiceException -> fallback text, app continues

Sponsor report (F6)
  Java totals academic vs personal -> AI only words the summary
  -> FileService saves data/report_<userId>.txt
```

## Class map
```
User (abstract)         -> StudentUser, AdminUser, SponsorUser
Transaction (abstract)  -> ExpenseTransaction, SavingsTransaction
Other models: SavingsGoal, BudgetPlan
Services: UserService, TransactionService, BudgetService, PredictorService,
          CurrencyService, FileService, AIService, AIHelper
Utilities: InputHelper, Money
Exceptions: AIServiceException, LowRunwayException
Main: CampusSaverApp (terminal test menu), WebServer (connects web/ pages to services)
```

## Project rules
- Plain arrays plus an `int` counter only. No `ArrayList`, `HashMap` or `List`.
- All data in `.txt` files under `data/`. Money is stored as `long` cents. Delimiter is `|`.
- All AI goes through `AIHelper.ask(String)` using `HttpURLConnection`. JSON is built and parsed with `indexOf` and `substring`.
- Missing key or no internet means `AIServiceException`, a fallback message, and the program keeps running.
- Every AI reply is appended to `data/ai_log.txt`.
- Invalid input never crashes the app.
- - Frontend: plain HTML, CSS and JavaScript in `web/`, served by `WebServer` (`com.sun.net.httpserver.HttpServer`). All logic, validation and file I/O stay in Java.
- Service classes never use `Scanner` or `System.out`. They take parameters and return values or throw exceptions.

## Setup (for evaluators)
1. Requires **JDK 17 or newer**.
2. Copy `config.example.txt` to `config.txt` in the project root, then fill it in:
   ```
   API_KEY=your_key_here
   
   API_MODEL=<Gemini model name from Google AI Studio>
   ```
   Without a valid key the app still works, using fallback text.
3. Compile from the project root:
   - Windows PowerShell: `javac --release 17 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName`
   - Mac/Linux: `javac --release 17 -d out src/campussaver/*/*.java`
4. Run from the project root (so `data/` is created there):

## Data files (`data/`)
See `TEAM_CHECKLIST.md` section 7 for the exact column layout of `users.txt`, `transactions.txt`, `budgets.txt`, `goals.txt`, `templates.txt`, `rates.txt` and `ai_log.txt`.

## Team workflow
- `TEAM_CHECKLIST.md` has the build order and the locked method signatures. Do not rename them.
- Each member has a `MEMBERx_CHECKLIST.md` in the repo root.
- Work on your own branch, open a pull request, and the team leader merges. Never push directly to `main`. Never commit `config.txt`.

## Disclaimer
General guidance only, not financial advice. Students in financial difficulty should contact the university financial aid office.
