# Member 4: AI Engine, Budget Setup (F1), Predictor (F2), Runway (F5), Sponsor Report (F6)

**Files:** `service/AIHelper.java`, `service/AIService.java`, `service/BudgetService.java`, `service/PredictorService.java`, `service/CurrencyService.java`, `model/BudgetPlan.java`
**Branch:** `feature/member4-ai-integration`
**Rules:** All AI goes through `AIHelper.ask(String)`. Java calculates every number. The AI only writes tips, plans and wording. Money is `long cents`.

## Part 1: Start
- [ ] `git checkout main && git pull origin main`, then `git checkout -b feature/member4-ai-integration`
- [ ] Read `config.example.txt`, confirm `config.txt` and `data/` are gitignored. Never commit a key.

## Part 2: AIHelper (do this first, it is the biggest risk)
- [ ] `public static String ask(String prompt) throws AIServiceException`
- [ ] Read `config.txt` with `BufferedReader` (ignore blank and `#` lines, split at the first `=`): `API_KEY` (required), `API_URL`, `API_MODEL` (optional, ILMUCode defaults).
  - Missing file, blank key or the placeholder key: log a notice and throw `AIServiceException`.
- [ ] Build the JSON body with string methods only, escaping `\`, `"`, newline, `\r`, tab. No Gson, Jackson or org.json.
- [ ] `HttpURLConnection`: POST, 10 s connect and 15 s read timeouts, `Content-Type`, `Authorization: Bearer <key>`, `setDoOutput(true)`.
- [ ] Non-2xx response: log it and throw `AIServiceException("AI HTTP error: <code>")`.
- [ ] Parse the reply with `indexOf` and `substring`: find `"choices"`, then `"content"`, skip to the opening quote, scan to the unescaped closing quote, handle `\"`, `\\`, `\n`, `\t`. If it fails, throw `AIServiceException`.
- [ ] `IOException` (offline or timeout): log it and throw `AIServiceException`.
- [ ] Log every reply shown to a user to `data/ai_log.txt`, with timestamp, feature and reply, one line per entry. Never log the key.

## Part 3: BudgetPlan and BudgetService (F1)
- [ ] `BudgetPlan`: `userId`, categories array, percents array, size. Methods: `add(category, percent)`, `getSize()`, `getCategory(i)`, `getPercent(i)`, `getUserId()`, `total()`.
- [ ] `buildFromAI(StudentUser u)`: ask the AI for `category:percent` lines, parse them, then validate in Java:
  - Every category is known, every percent is a whole number from 0 to 100, and the total is exactly 100.
  - Any failure, or `AIServiceException`: use the default split from `templates.txt` for the user's `livingType`.
- [ ] `totalsHundred(BudgetPlan p)`.

## Part 4: PredictorService (F2 and F5, pure Java)
- [ ] `LOW_RUNWAY_DAYS = 14`.
- [ ] `projectedMonthEnd(spentCents, daysPassed)`, `isOverspending(u, spentCents, daysPassed)`, `remainingRunwayDays(u, spentCents, daysPassed)`.
  - Guard `daysPassed <= 0`.
  - No spending means a high runway, with no division by zero.

## Part 5: AIService (wording only, each method has a fallback)
- [ ] `tipsPrompt(...)` and a fallback tip text (the existing rule-based text can be reused).
- [ ] `survivalPlanPrompt(...)`: no unhealthy advice (never skipping meals), point to the university financial aid office, end with "General guidance, not financial advice." Fallback text has the same content.
- [ ] `sponsorWordingPrompt(...)`: give the AI only the figures Java calculated (academic, personal, total) and tell it not to change them. Java builds the report numbers and saves it with `FileService.saveReport`.
- [ ] Every method catches `AIServiceException` and returns the fallback. Replace `instanceof` patterns with classic casts.

## Part 6: CurrencyService (F3, Stretch)
- [ ] Build the `rates.txt` version first. File format: `MYR|1.0000`. Rates are stored scaled by 10000 as `long`, never `double`.
- [ ] Methods: `loadRates()`, `long convert(long cents, String from, String to)`, `boolean setRate(String code, String rateText)` (also saves the file), `String[] getCodes()`, `int getCount()`.
- [ ] Missing file: create defaults (MYR, USD, SGD). Unknown currency or bad rate: no crash.
- [ ] Test: `convert(1000, "MYR", "USD")` with `USD|0.2100` returns `210`.
- [ ] Live fetching (approved by the lecturer): `HttpURLConnection` only, plain arrays, save to `rates.txt` on success, fall back to `rates.txt` on any failure (catch a `RateFetchException`). Do this last. Explain it in the report and video.

## Part 7: Test and push
- [ ] Compile (Windows PowerShell): `javac --release 17 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName`
- [ ] Offline test: no `config.txt` gives a fallback, no crash, and a notice in `ai_log.txt`.
- [ ] Online test: a real key gives a real reply, which is logged.
- [ ] Bad split test: force a reply that totals 90 and confirm the template is used.
- [ ] `git status` shows `config.txt` untracked and ignored.
- [ ] Push your own files only and open a PR.
- [ ] `main/WebServer.java`: `HttpServer` on port 8080, serves files from `web/`, routes `/api/...` to the services, replies with text or JSON built by string concatenation, catches every exception and returns an error message (never crashes). Build after F1, F2 and F5 work offline (Day 7-10).
