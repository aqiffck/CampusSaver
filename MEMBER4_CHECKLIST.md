# Member 4 Coding Checklist — `AIHelper` HTTP Engine & `AIService` Integration

**Your Files:**
1. `[NEW]` `src/campussaver/service/AIHelper.java`
2. `[UPDATE]` `src/campussaver/service/AIService.java`

**Your Branch Name:** `feature/member4-ai-integration`  
**Good News from `feb1c9b`:** `AIService.java` already uses plain arrays (`Transaction[] transactions, int count`)! Now your job is connecting real HTTP AI calls via `AIHelper.ask(String)` + `config.txt` + `data/ai_log.txt` + graceful `AIServiceException` offline fallback.

---

## Part 1: Before You Start
- [ ] Run `git checkout main && git pull origin main`
- [ ] Create your branch: `git checkout -b feature/member4-ai-integration`
- [ ] Read:
  - `config.example.txt` (`API_KEY=your_actual_api_key_here`)
  - `.gitignore` (verify `config.txt` is ignored — NEVER commit real API keys!)
  - `src/campussaver/service/AIService.java`
  - `src/campussaver/exception/AIServiceException.java`

---

## Part 2: Build `AIHelper.java` (Strict WIX1002 Rules)
Create `src/campussaver/service/AIHelper.java` (`package campussaver.service;`).

### A. Read API Config from `config.txt`
- [ ] Read `config.txt` line by line using `BufferedReader` / `FileReader` (ignore lines starting with `#` or blank lines).
- [ ] Split each line at the first `=` (`int eq = line.indexOf('=')`) to load:
  - `API_KEY` (required)
  - `API_URL` (optional, default e.g. `"https://api.ilmu.ai/v1/chat/completions"` for ILMU/OpenAI-compatible)
  - `API_MODEL` (optional, default e.g. `"ilmu-chat"`)
- [ ] If `config.txt` is missing, `API_KEY` is blank, or `API_KEY` equals `"your_actual_api_key_here"`, log an offline notice to `data/ai_log.txt` and throw `new AIServiceException("AI API key not configured in config.txt.")`.

### B. `public String ask(String prompt) throws AIServiceException`
- [ ] Escape special characters in `prompt` manually using pure `String` (`\`, `"`, `\n`, `\r`, `\t`) — **NO Gson, Jackson, or org.json libraries!**
- [ ] Build the JSON request body string manually:
  ```java
  String jsonBody = "{\"model\":\"" + escapeJson(model) + "\",\"messages\":[{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}]}";
  ```
- [ ] Open `java.net.HttpURLConnection`:
  - `conn.setRequestMethod("POST");`
  - `conn.setConnectTimeout(10000);` / `conn.setReadTimeout(15000);`
  - `conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");`
  - `conn.setRequestProperty("Authorization", "Bearer " + apiKey);`
  - `conn.setDoOutput(true);` -> write `jsonBody.getBytes(StandardCharsets.UTF_8)`.
- [ ] Check `int status = conn.getResponseCode();`. If `< 200 || >= 300`, read `conn.getErrorStream()`, log failure to `data/ai_log.txt`, and throw `new AIServiceException("AI HTTP error: " + status)`.
- [ ] Read full response text from `conn.getInputStream()`.
- [ ] **Parse `"content"` using `indexOf` & `substring` ONLY:**
  - Locate `"choices"` -> locate `"content"` -> advance past `:` and opening `"` -> scan character-by-character until unescaped closing `"` (handling `\"`, `\\`, `\n`, `\t`).
  - If `"content"` cannot be parsed, throw `new AIServiceException("Unable to parse AI response text.")`.
- [ ] **Log every reply to `data/ai_log.txt`:**
  - Ensure `data/` directory exists (`new File("data").mkdirs()`).
  - Append timestamp, prompt summary, and full AI reply using `new FileWriter("data/ai_log.txt", true)`.
  - **NEVER** write `apiKey` into `ai_log.txt`!
- [ ] Wrap `IOException` (no internet / timeout) -> log offline notice to `data/ai_log.txt` and throw `new AIServiceException("AI service unreachable (offline).")`.

---

## Part 3: Connect `AIHelper` Inside `AIService.java` (With Offline Fallback!)
Open `src/campussaver/service/AIService.java`:
- [ ] Add field: `private final AIHelper aiHelper = new AIHelper();`
- [ ] Keep `calculateAcademicRatio(Transaction[] transactions, int count)` and `calculateRemainingRunwayDays(...)` math **unchanged**!
- [ ] Optionally add a helper `calculateTotalSavings(Transaction[] transactions, int count)` (summing `SavingsTransaction` amounts) so the AI knows how much spare change the student saved.
- [ ] Inside `generateFinancialAdvice(StudentUser user, Transaction[] transactions, int count, int daysPassedInMonth)`:
  1. Keep building the existing math report (`localAdvice`) first (`Academic Spending Ratio`, `Estimated Runway`, rule-based status).
  2. Build a concise plain-text prompt containing the student's `livingType`, `hasScholarship`, `monthlyBudgetLimit`, `academicRatio`, `runwayDays`, and `totalSavings`.
  3. Call `aiHelper.ask(prompt)` inside `try/catch`:
     ```java
     try {
         String aiTip = aiHelper.ask(prompt);
         advice.append("\n\n--- AI Coach Recommendation ---\n").append(aiTip);
     } catch (AIServiceException e) {
         advice.append("\n\n[Offline Fallback] ").append(e.getMessage())
               .append(" Showing local CampusSaver calculation above.");
     }
     ```
  4. Keep `if (transactions == null || count <= 0)` throwing `AIServiceException` as it already does when there is no transaction data at all!

---

## Part 4: Self-Test (Both Offline & Online!) & Push
- [ ] Compile cleanly from `CampusSaver/` root:
  ```bash
  javac --release 17 -d out src/campussaver/exception/*.java src/campussaver/model/*.java src/campussaver/service/AIHelper.java src/campussaver/service/AIService.java
  ```
- [ ] **Test Offline Fallback:** Without `config.txt`, call `generateFinancialAdvice(...)` with sample `ExpenseTransaction[]` -> confirm **zero crash**, fallback message prints, and `data/ai_log.txt` records the offline log.
- [ ] **Test Online AI:** Copy `config.example.txt` -> `config.txt`, paste real API key, run again -> confirm real AI advice prints AND appends to `data/ai_log.txt`.
- [ ] **Security Check:** Run `git status` and verify `config.txt` does **NOT** appear as untracked/staged!
- [ ] Commit & push ONLY `AIHelper.java` and `AIService.java`:
  ```bash
  git add src/campussaver/service/AIHelper.java src/campussaver/service/AIService.java
  git commit -m "Add AIHelper HTTP client with ai_log.txt and connect AIService fallback"
  git push -u origin feature/member4-ai-integration
  ```
