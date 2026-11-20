# 🎓 CampusSaver — Complete Project Explanation & Execution Flow

---

## 💡 1. What is CampusSaver? (The Core Idea)

**CampusSaver** is a local terminal application built in Java that acts as a **financial survival calculator for university students**.

### The Student Problem
Most university students receive a fixed monthly allowance, parental stipend, or scholarship payout at the start of each month. A common issue is overspending during the first two weeks on non-academic activities (e.g., eating out, entertainment), leaving them with insufficient money for books, project materials, or daily meals toward the end of the month.

### The CampusSaver Solution
CampusSaver doesn't just log expenses; it actively protects the student's monthly runway:
1. **Tracks Budget & Categories**: Separates spendings into academic necessities vs. non-academic discretionary expenses.
2. **Calculates Burn Velocity**: Evaluates daily spending rate to predict how long remaining funds will last.
3. **Triggers Early Warnings**: Throws runtime exceptions (`LowRunwayException`) when spending threatens the monthly budget.
4. **Delivers AI Insights**: Uses `AIService` to calculate spending ratios and remaining runway days.
5. **Persists Data Locally**: Saves user profiles and transaction history to local `.txt` files so data is never lost when the program closes.

---

## 🔄 2. End-to-End Application Flow

The diagram below illustrates how data moves through the application from start to finish:

```text
               +----------------------------------+
               |        1. Start Program          |
               |     (CampusSaverApp.main)        |
               +----------------+-----------------+
                                |
                                v
               +----------------------------------+
               |      2. Load Persisted Data      |
               |   FileService.loadTransactions() |
               |     FileService.loadUser()       |
               +----------------+-----------------+
                                |
       +------------------------+------------------------+
       | (If user profile missing)                       | (If user profile exists)
       v                                                 v
+-----------------------------+           +------------------------------+
|   3a. Onboard New Student   |           |    3b. Main Menu Loop        |
|  UserService.registerUser() |           | (CLI Option Selection 1 - 4) |
+--------------+--------------+           +--------------+---------------+
               |                                         |
               +------------------+----------------------+
                                  |
            +---------------------+---------------------+---------------------+
            |                     |                     |                     |
            v                     v                     v                     v
   [Option 1: Add Expense]  [Option 2: Summary]   [Option 3: AI Advice]  [Option 4: Save & Exit]
   - Input amount & type   - View total spent    - Calculate ratios     - Save data to disk
   - Create transaction    - View balance left   - Estimate runway      - Terminate program
   - Check threshold       - Display history     - Print AI advice      
```

---

## 📐 3. Core Mathematical & Logic Models

CampusSaver uses three key calculations to manage student finances:

### A. Academic Spending Ratio
Evaluates the proportion of total funds allocated strictly to academic necessities:
$$\text{Academic Ratio} = \left( \frac{\text{Academic Spent}}{\text{Total Spent}} \right) \times 100\%$$

### B. Daily Burn Velocity
Measures average spending speed per day during the current month:
$$\text{Daily Velocity} = \frac{\text{Total Spent}}{\text{Days Passed in Month}}$$

### C. Estimated Runway Days
Predicts how many days the remaining budget will last under current spending velocity:
$$\text{Runway Days} = \frac{\text{Remaining Monthly Budget}}{\text{Daily Velocity}}$$

---

## 📑 4. Step-by-Step Execution Journey

### Step 1: Program Launch & Initialization (`CampusSaverApp.java`)
* The `main()` method runs when the user starts the application.
* The application instantiates all four internal service layers:
  ```java
  FileService fileService = new FileService();
  UserService userService = new UserService();
  TransactionService transService = new TransactionService();
  AIService aiService = new AIService();
  ```
* `FileService` reads `users.txt` and `transactions.txt`. If saved records exist, objects are instantiated and loaded directly into memory.

### Step 2: Student Onboarding (`UserService.java`)
* If no user record exists, `CampusSaverApp` prompts the user for profile setup:
  * Student Name & ID
  * Living Type: `HOSTEL` or `COMMUTER`
  * Scholarship Status: `true` / `false`
  * Monthly Budget Limit (e.g., `800.00 MYR`)
* This creates a `StudentUser` model instance stored in `UserService`.

### Step 3: Main Navigation Menu Loop (`CampusSaverApp.java`)
The CLI displays an interactive loop:
```text
=== CampusSaver CLI Menu ===
1. Add New Expense
2. View Summary & Balance
3. Get AI Financial Insights
4. Save Data & Exit
Select an option (1-4):
```

### Step 4: Menu Option Execution

#### 🔹 Option 1: Log an Expense
* User enters amount, date, description, category (`FOOD`, `BOOKS`, `TRANSPORT`, etc.), and whether it is an academic necessity (`true`/`false`).
* `TransactionService` creates an `ExpenseTransaction` object and adds it to the active list.
* **Low Runway Check**: `TransactionService` compares remaining balance to threshold safety limits. If balance is critically low, it throws `LowRunwayException`, and the UI displays a warning banner.

#### 🔹 Option 2: Financial Summary Dashboard
* Displays total money spent so far and remaining budget in `MYR`.
* Prints a formatted table listing all recorded transactions.

#### 🔹 Option 3: AI Financial Insights (`AIService.java`)
* `AIService` analyzes the current `List<ExpenseTransaction>`.
* **Exception Guard**: If no transactions exist, `AIService` throws `AIServiceException` ("Insufficient data to analyze").
* **Insight Generation**: Calculates Academic Spending Ratio and Runway Days, printing recommendations to help the student pace their remaining budget.

#### 🔹 Option 4: Save Data & Exit (`FileService.java`)
* Calls `FileService.saveTransactions()` and `FileService.saveUser()`.
* Iterates through active in-memory models, formats them into comma-separated text strings via `.toFileFormat()`, and writes them to `data/transactions.txt` and `data/users.txt`.
* Displays a exit banner and closes the `Scanner` stream.

---

## 🤝 5. How Team Roles Interconnect

Every team member owns a specific component of this data flow pipeline:

```text
[Member 3: CLI UI Menu]
        │
        ├────────► [Member 2: User & Transaction Logic]
        │                   │
        │                   ├───► Triggers LowRunwayException
        │                   │
        ├────────► [Member 4: AI Insights Engine]
        │                   │
        │                   └───► Triggers AIServiceException
        │
        └────────► [Member 1: File Storage System]
                            │
                            └───► Reads/Writes text files (Data Persistence)
```

| Team Member | Component | Connection to the System |
| :--- | :--- | :--- |
| **Member 1** | `FileService.java` | **Data Persistence**: Loads data into memory at startup and writes memory state back to text files on exit. |
| **Member 2** | `UserService.java` & `TransactionService.java` | **Core Engine**: Holds user state, computes spending totals, and triggers `LowRunwayException` when funds are depleted. |
| **Member 3** | `CampusSaverApp.java` | **User Interface**: Coordinates menu selections, captures user inputs, calls appropriate service methods, and handles errors cleanly. |
| **Member 4** | `AIService.java` | **Analytics & Quality**: Analyzes spending data for insights, throws `AIServiceException` on bad/missing data, and handles PR reviews/integration. |

---

## 📂 6. Repository Tree Map

```text
CampusSaver/src/
└── campussaver/
    ├── model/         (Completed: User, StudentUser, Transaction, ExpenseTransaction)
    ├── exception/     (Completed: AIServiceException, LowRunwayException)
    ├── service/       (FileService, UserService, TransactionService, AIService)
    └── main/          (CampusSaverApp)
```

---

## 🚀 7. Member Onboarding Quickstart

1. **Clone the Repo**:
   ```bash
   git clone https://github.com/aqiffck/CampusSaver.git
   ```
2. **Open in VS Code**:
   Open **`CampusSaver/src`** directly as your root folder (`File` $\rightarrow$ `Open Folder...`). This ensures all `package campussaver...` declarations compile cleanly.
3. **Feature Branching**:
   ```bash
   git checkout main
   git pull origin main
   git checkout -b feature/your-assigned-task
   ```