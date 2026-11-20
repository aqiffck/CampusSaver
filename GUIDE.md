# 📌 CampusSaver — Team Onboarding & Project Guide

## 💡 Project Overview
**CampusSaver** is a Java-based Command Line Interface (CLI) application designed for university students to manage their personal finances. It tracks monthly budgets, categorizes academic vs. non-academic expenses, calculates daily burn velocity and runway days, and saves financial history locally to text files.

---

## 📂 Project Structure & Directory Tree Map

To maintain a clean environment, ensure all files strictly follow the package structure outlined below:

```text
CampusSaver/src/
└── campussaver/
    ├── model/         [COMPLETED] User, StudentUser, Transaction, ExpenseTransaction
    ├── exception/     [COMPLETED] AIServiceException, LowRunwayException
    ├── service/       [IN PROGRESS] FileService, UserService, TransactionService, AIService
    └── main/          [IN PROGRESS] CampusSaverApp
```

---

## 🛠️ Detailed Team Task Breakdown

### 🔹 Member 1: Data Persistence & File Operations (`FileService.java`)
* **Goal**: Enable reading from and saving project data to local text files.
* **Target File**: `src/campussaver/service/FileService.java`
* **Key Deliverables**:
  1. Implement `saveTransactions()` and `loadTransactions()` to read/write `transactions.txt` and `users.txt`.
  2. Parse raw comma-separated text lines into concrete Java objects using model helper methods like `.toFileFormat()`.
  3. Include auto-creation logic using `file.createNewFile()` so missing files do not crash the application on launch.

---

### 🔹 Member 2: Core Business Logic (`UserService.java` & `TransactionService.java`)
* **Goal**: Process balance calculations, user profiles, and budget guardrails.
* **Target Files**: `src/campussaver/service/UserService.java` and `src/campussaver/service/TransactionService.java`
* **Key Deliverables**:
  1. **User Management**: Build `UserService` to handle user profile creation, monthly budget limits, living status (`HOSTEL` vs `COMMUTER`), and scholarship status.
  2. **Transaction Management**: Build `TransactionService` to add expenses, calculate total spending, and filter expenses by category.
  3. **Guardrails**: Implement financial checks that throw `LowRunwayException` when remaining funds drop below designated thresholds.

---

### 🔹 Member 3: Terminal UI & App Navigation (`CampusSaverApp.java`)
* **Goal**: Build the interactive CLI menu loop and manage user input.
* **Target File**: `src/campussaver/main/CampusSaverApp.java`
* **Key Deliverables**:
  1. Construct an interactive `Scanner` menu loop (`1. Add Expense` | `2. View Summary` | `3. AI Insights` | `4. Save & Exit`).
  2. Implement robust input validation wrapped in `try-catch` blocks to prevent crashes on invalid user input.
  3. Format all monetary values cleanly using `MYR` and ensure error messages display nicely in the console.

---

### 🔹 Member 4: Financial Insights & Service Integration (`AIService.java`)
* **Goal**: Generate spending recommendations and oversee end-to-end integration.
* **Target File**: `src/campussaver/service/AIService.java`
* **Key Deliverables**:
  1. Calculate academic vs. non-academic spending ratios and estimate remaining runway days based on daily velocity.
  2. Throw `AIServiceException` when transaction history is empty or insufficient for analysis.
  3. Perform end-to-end data flow testing across all services and review team Pull Requests before merging into `main`.

---

## ⚙️ Development & Onboarding Workflow

### 1. Workspace Configuration (Crucial)
To avoid package path errors (e.g., `src.campussaver` vs. `campussaver`):
1. Open VS Code.
2. Select **File $\rightarrow$ Open Folder...** (`Ctrl + K, Ctrl + O`).
3. Select the **`src`** directory inside the repository (`.../CampusSaver/src`).
4. Opening `src` directly ensures VS Code correctly recognizes `campussaver.*` as the root package prefix.

### 2. Git Branching Guidelines
Do **not** commit code directly to the `main` branch. Follow these commands:

```bash
# 1. Update your local main branch
git checkout main
git pull origin main

# 2. Create your isolated feature branch
git checkout -b feature/your-assigned-feature

# 3. Stage, commit, and push your work
git add .
git commit -m "Implement core functionality for assigned task"
git push origin feature/your-assigned-feature
```

### 3. Code Standards & Rules
* **Package Declarations**: Always start files with `package campussaver.model;`, `package campussaver.service;`, or `package campussaver.main;`.
* **Immutability**: Declare fields initialized in constructors as `final` where applicable.
* **Exceptions**: Handle custom exceptions (`AIServiceException` and `LowRunwayException`) gracefully without letting the application crash unexpectedly.
