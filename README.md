# Personal Finance & Budget Manager (CLI)

A zero-dependency Java command-line personal finance manager for tracking income and expenses, setting monthly category budgets, and viewing spending analytics with CSV storage and salted password hashing.

Built in plain Java with no Maven, Gradle, database, or internet access required. Users can register and log in securely, record income and expenses, filter transactions by month, and see a running balance. They can set monthly budgets per category, get alerts when spending goes over, and view monthly totals with a category breakdown chart. Data is stored in readable CSV files, and the code is organized into model, DAO, service, and UI layers so storage can later be swapped for a real database.

## Features

- Multi-user accounts with SHA-256 salted password hashing (no plain-text passwords)
- Add, view, filter by month, and delete transactions
- Automatic running balance
- Per-category, per-month budget limits with overspend alerts
- Analytics: total income, total expense, net savings, and a category bar chart
- Data persisted to human-readable CSV files under `data/`
- Timestamped logging to `data/app.log`
- Custom checked and unchecked exceptions with input validation
- Layered architecture (DAO → Service → UI)
- 11 automated unit tests with a dependency-free test runner

## Technologies

- **Language:** Java (JDK 21 tested; compatible with JDK 11+)
- **Build:** plain `javac` / `java`
- **Storage:** CSV files
- **Testing:** custom test runner (`com.pfbm.test`), no JUnit required
- **Version control:** Git

## Project Structure

```
finance-manager/
├── src/com/pfbm/
│   ├── Main.java          # entry point: wires DAOs, services, UI
│   ├── model/             # User, Transaction, Income, Expense, Category, Budget, BudgetStatus
│   ├── exception/         # custom exceptions
│   ├── dao/               # DAO interfaces + CSV implementations
│   ├── service/           # UserService, TransactionService, BudgetService, ReportService
│   ├── ui/                # ConsoleUI: all console input/output
│   └── util/              # PasswordUtil, AppLogger
├── test/com/pfbm/test/    # test runner and unit tests
├── docs/                  # architecture diagram
├── data/                  # created at runtime
├── statement.md
└── README.md
```

## Prerequisites

JDK 11 or newer, on your PATH. Check with:

```bash
java -version
javac -version
```

If Java is missing:

- **Ubuntu/Debian:** `sudo apt-get install openjdk-21-jdk`
- **macOS (Homebrew):** `brew install openjdk@21`
- **Windows:** install from [Adoptium](https://adoptium.net) and add it to PATH

No other dependencies are needed.

## Build and Run

### CLI version

```bash
# 1. Clone the repository
git clone https://github.com/<your-username>/<your-repo-name>.git
cd <your-repo-name>

# 2. Compile
find src -name "*.java" > sources.txt
mkdir -p out
javac -d out @sources.txt

# 3. Run
java -cp out com.pfbm.Main
```

On first run, choose **2) Register** to create an account. After that, use **1) Login**. Data is saved under `data/` and persists between runs.

### Browser version

A lightweight browser UI is included in the project root and can be served locally:

```bash
cd <your-repo-name>
py -m http.server 8000
```

Then open `http://localhost:8000` in a browser. The browser version stores data in the browser using local storage, so it works without a database or backend.

## Quick Walkthrough

1. Choose `2) Register` and pick a username (3+ characters) and password (4+ characters).
2. From the main menu, choose `1) Add Income`: amount `50000`, category `SALARY`.
3. Choose `2) Add Expense`: amount `1200`, category `FOOD`.
4. Choose `5) Set Monthly Budget` for `FOOD` at `1000` for the current month.
5. Choose `6) View Budgets & Alerts` to see the over-budget warning.
6. Choose `7) Monthly Report` to see totals and the category bar chart.
7. Choose `9) Exit`.

## Testing

```bash
# Compile the main sources first (step 2 above), then:
find test -name "*.java" > test_sources.txt
javac -cp out -d out @test_sources.txt
java -cp out com.pfbm.test.RunAllTests
```

Expected final line:

```
Total: 11  Passed: 11  Failed: 0
```

The runner exits with a non-zero status if any test fails, so it works in CI. Tests use temporary directories and never touch your real `data/` folder.

## Design & Documentation

- `docs/architecture.md`: system architecture diagram
- `statement.md`: problem statement and scope

## Non-Functional Requirements

| Requirement | How it's addressed |
|---|---|
| Security | Salted SHA-256 hashes via `PasswordUtil`; passwords are never stored in plain text |
| Reliability | File access uses try-with-resources; the UI loop catches errors so bad input doesn't crash the app |
| Usability | Numbered menus and actionable validation messages |
| Maintainability | Layered architecture (DAO → Service → UI); each class has one responsibility |
| Error handling | Checked exceptions (`InvalidAmountException`, `AuthenticationException`) and unchecked (`ValidationException`, `DataAccessException`) |
| Logging | Timestamped entries in `data/app.log` via `AppLogger` |
| Scalability | Storage sits behind DAO interfaces, so CSV can be replaced with a database by changing only the `dao` package |

## Future Enhancements

- JDBC-backed DAO implementations for a real relational database
- Export monthly reports to CSV or PDF
- Recurring transactions (e.g., monthly rent auto-logged)
- REST API layer on top of the service classes

