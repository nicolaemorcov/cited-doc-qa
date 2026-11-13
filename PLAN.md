# PLAN.md — cited-doc-qa

Build plan for a Spring Boot service that answers questions about a public
document and cites the section each answer came from. Written on 2026-11-13.
Two stages: a showable version by **2026-11-30**, the full version by **2026-12-14**.
Schedule updated on 2026-11-13.

## 1. What this project is

A document question-answering service. The engine is generic: it loads a
document, splits it into sections, finds the sections related to a question,
asks an AI model to answer from those sections only, and returns the answer
with its sources.

The GDPR (Regulation (EU) 2016/679, English text) is the first document loaded. It was chosen because it has 99 numbered articles,
so a source is unambiguous ("Article 33"), and because the audience (EU B2B
clients) knows it. A second document, the EU AI Act, is added in Stage 2 to test
the claim that the engine is generic: it should need a new corpus file and
parser rules, not a new service.

The repo must prove four things to a client:

1. Answers come with sources (document + article).
2. Every request's cost is logged.
3. Quality is measured: 20 questions with known correct articles, scored automatically.
4. Personal data is masked before anything is sent to the AI provider, and never logged.

## 2. Request flow

```
POST /api/questions  {"question": "..."}
  1. Mask      plain Java   replace personal data with [EMAIL], [PHONE], ...
  2. Search    1 embedding call   find the 4 most related articles
  3. Ask       1 chat call        question + those 4 articles -> answer + cited article numbers
  4. Validate  plain Java   keep only cited articles that were really retrieved
  5. Log       plain Java   request id, tokens, cost, latency, sources (never the question text)
-> {"requestId": "...", "answer": "...", "sources": [{"document": "...", "section": "Article 33", "title": "..."}]}
```

Two external calls per request (one embedding, one chat). Everything else is plain Java.
Masking (flow step 1) is built in Stage 2, Step 7. Until then the flow starts at Search.

## 3. Fixed decisions

| Decision | Choice | Reason |
|---|---|---|
| Language / build | Java 21, Maven | Already in the skeleton |
| Framework | Spring Boot 4.1.2 | Latest stable on 2026-11-13 |
| AI library | Spring AI 2.0.2 | Latest stable patch; 2.1 is still a release candidate |
| AI provider | Mistral AI (chat + embeddings) | Supported by Spring AI; EU company. Its data processing terms are NOT yet checked (Step 6) |
| Vector store | Spring AI `SimpleVectorStore`, saved to a local file | 99 chunks (212 after Stage 2) need no database |
| Chunking | One chunk per article (99 chunks), recitals excluded | Simplest parser; a source is an article. Revisit only if the Step 5 score is below target |
| Retrieval size | Top 4 articles per question | Roughly 2,000-3,000 input tokens per request |
| Licence | Apache-2.0, repo public from the first commit | Dated public history; anyone, including an employer, can use it under the licence |

Names below marked **(verify)** come from Spring AI documentation as of mid-2026.
Confirm them in the Spring AI 2.0.2 reference before use; do not guess.

- Starter: `spring-ai-starter-model-mistral-ai` **(verify)**
- Properties: `spring.ai.mistralai.api-key`, `spring.ai.mistralai.chat.options.model` **(verify)**
- Token usage: `ChatResponse.getMetadata().getUsage()` **(verify)**
- Model names and prices: read them from Mistral's current pricing page; put them in `application.yml`, never in code.

## 4. Rules for Claude Code

- Do exactly one step per session. Stop when its "Done when" check passes and report the evidence (command run, output seen).
- Do not add anything a step does not list (extra tests, mappers, docs, endpoints). If something seems missing, ask first.
- If a **(verify)** name is wrong in the current docs, stop and say so instead of improvising.
- No employer code or data. Public data only.
- Secrets: the API key comes from the environment variable `MISTRAL_API_KEY`. Never commit a key.
- Privacy rule that applies to every step: question text is never written to logs, files, or exception messages.

### Code conventions (non-negotiable)

- No Lombok. No `var`; declare every type explicitly.
- No `System.out.println`. SLF4J only: `private static final Logger log = LoggerFactory.getLogger(ClassName.class);`
- Constructor injection only; explicit constructors; no field `@Autowired`.
- Every service is an interface plus an `Impl` class in separate files.
- `record` for request/response and immutable value objects; no "DTO" suffix.
- Variable names match their type name in camelCase.
- Switch expressions with arrow syntax.
- `@RestController`, `@Service`, `@Repository`; never a generic `@Component` for these layers.
- Controllers return `ResponseEntity<T>`.
- Errors handled in one `@RestControllerAdvice` with `@ExceptionHandler`.
- Tests: JUnit 5 + Mockito only; `// Given`, `// When`, `// Then` in every test; descriptive names such as `shouldMaskEmailBeforeProviderCall`; service tests use `@ExtendWith(MockitoExtension.class)`.

### Package layout

```
dev.citedqa
  corpus     load and split the document
  retrieval  embeddings, vector store, search
  answer     prompt, chat call, source validation
  cost       token and cost logging
  privacy    masking
  eval       the 20-question scorer
  web        controller, request/response records, error handling
```

## 5. Schedule

Build time is about 6 hours per week. Job applications run in parallel, about
2.5 hours per week, outside this plan. They start now and do not wait for the repo.

### Stage 1 — showable version, by 30 November (15.5 h)

| Step | What | Hours |
|---|---|---|
| 0 | Skeleton runs and is public | 0.5 |
| 1 | Load the GDPR and split it into articles | 2.5 |
| 2 | Embed the articles and search them | 2.5 |
| 3 | Answer with sources | 3 |
| 4 | Log cost per request | 1.5 |
| 5 | The 20-question test set, scored automatically | 3.5 |
| 6 | README, first version | 2 |

When Step 6 is done, the repo link goes on the profile and into applications.
If Stage 1 runs late, it still finishes before Stage 2 starts. Steps 5 and 6 are never cut.

### Stage 2 — full version, by 14 December (12 h)

| Step | What | Hours |
|---|---|---|
| 7 | Mask personal data, and measure it | 3.5 |
| 8 | Add a second document (EU AI Act) | 3.5 |
| 9 | Grow the test set to 50 questions | 2 |
| 10 | Timeouts, retries, fallback | 2 |
| 11 | README, final version | 1 |

Cut rule for Stage 2: if behind on 10 December, cut Step 10, then Step 9. Step 11 is never cut.
No new steps are added to this plan after Stage 2 without a decision made outside Claude Code.

---

## Step 0 — Skeleton runs and is public (0.5 h)

**Goal:** the empty service from `cited-doc-qa.zip` runs locally and is on GitHub.

**Build**
- Unzip. The folder already contains `pom.xml`, the main class, `application.yml`, `.gitignore`,
  the Apache-2.0 `LICENSE`, and this `PLAN.md`.
- `git init`, first commit, push to a new, empty, public GitHub repo named `cited-doc-qa`.

**Done when**
- `mvn spring-boot:run` starts without errors.
- `curl localhost:8080/actuator/health` returns `{"status":"UP"}`.
- The repo is visible on GitHub with one commit.

**Do not:** add README content, tests, or CI yet.

## Step 1 — Load the GDPR and split it into articles (2.5 h)

**Goal:** plain Java turns the regulation text into 99 article objects. No AI.

**Why:** every later step depends on clean sections with correct numbers.

**Build**
- Save the English text of Regulation (EU) 2016/679 from EUR-Lex
  (`https://eur-lex.europa.eu/eli/reg/2016/679/oj`) as
  `src/main/resources/corpus/gdpr-en.txt`. This is a one-time manual save.
  Note the download date and URL in a `corpus/SOURCE.md` file.
- `Article` record: `String document`, `int number`, `String title`, `String text`.
  `document` is "GDPR" for now; the field exists so Stage 2 can add a second document without a refactor.
- `CorpusLoader` interface + `CorpusLoaderImpl`: reads the file, skips the
  recitals, splits at each `Article N` heading, returns `List<Article>`.
- One unit test class for the loader.

**Done when** (the test asserts all of these)
- Exactly 99 articles are returned, numbered 1 to 99 with no gaps.
- Article 1 has the title "Subject-matter and objectives".
- Article 33 text contains "72 hours".
- Article 99 has the title "Entry into force and application".
- No article has empty text.

**Do not:** split by paragraph, parse recitals, or add an HTML parser library.

## Step 2 — Embed the articles and search them (2.5 h)

**Goal:** given a question, return the 4 most related articles.

**Why:** if the right article is not retrieved, the model cannot cite it. This
is the step that most affects the final score.

**Build**
- Add the Mistral starter and the vector store module to `pom.xml` **(verify)**.
- `application.yml`: API key from `${MISTRAL_API_KEY}`, embedding model name.
- On startup: if `data/vector-store.json` exists, load it; otherwise embed the
  99 articles, save the file, and log how many were embedded. Add `data/` to `.gitignore`.
- Each stored chunk carries metadata: `document`, `articleNumber`, `title`.
- `RetrievalService` interface + `RetrievalServiceImpl`: `List<Article> search(String question)`, top 4.
- Temporary endpoint `GET /api/debug/search?q=...` returning article numbers and titles.

**Done when**
- First start embeds 99 chunks; second start embeds 0 (log line proves it).
- Search for "deadline to report a data breach" includes Article 33.
- Search for "right to have my data deleted" includes Article 17.
- Search for "when is a data protection officer required" includes Article 37.

**Do not:** add a database, tune chunk sizes, or add a chat call.

## Step 3 — Answer with sources (3 h)

**Goal:** `POST /api/questions` returns an answer and the articles it used. This is project item 1.

**Build**
- Records: `QuestionRequest(String question)`, `Source(String document, String section, String title)`,
  `AnswerResponse(String requestId, String answer, List<Source> sources)`.
- `AnswerService` interface + `AnswerServiceImpl`:
  1. Call `RetrievalService`.
  2. One chat call. The prompt gives the 4 articles, each labelled with its number, and instructs the model to
     answer only from them, to list the article numbers it used, and to say the document does not cover the
     question when that is the case. Use Spring AI structured output to get `answer` + `List<Integer> articles`.
  3. Plain Java validation: drop any cited number that was not among the 4 retrieved.
  4. If no valid citation remains, return the "not covered" answer with empty sources.
- `QuestionController` returning `ResponseEntity<AnswerResponse>`; validation error for a blank question (HTTP 400)
  via `@RestControllerAdvice`.
- Chat model name in `application.yml`; temperature 0.
- Unit tests for the validation logic with a mocked chat call.

**Done when**
- "How long do I have to report a data breach?" returns an answer mentioning 72 hours with Article 33 in `sources`.
- "What is the capital of France?" returns the "not covered" answer with empty `sources`.
- A blank question returns HTTP 400.
- Unit tests pass without an API key.

**Do not:** add streaming, chat history, or a UI. Remove nothing yet; the debug endpoint goes in Step 6.

## Step 4 — Log cost per request (1.5 h)

**Goal:** one log line per request with tokens and cost. This is project item 2.

**Build**
- `application.yml`: price per 1 million input tokens and per 1 million output tokens for the chat model,
  taken from Mistral's pricing page, with the date they were read as a comment.
- `CostCalculator` (plain Java, `BigDecimal`): tokens x price.
- After each chat call, log one line:
  `requestId, promptTokens, completionTokens, costUsd, latencyMs, sources=[33]`.
- Unit test for the calculator.

**Done when**
- One request produces exactly one cost line, and the cost matches a hand calculation from the token counts.
- The log line contains no part of the question text.

**Do not:** add metrics dashboards or a cost database. The embedding call's cost is left out; say so in the README.

## Step 5 — The 20-question test set, scored automatically (3.5 h) — NEVER CUT

**Goal:** one command prints a score out of 20. This is project item 3.

**Build**
- `eval/questions.json`: 20 entries,
  `{"id": 1, "question": "...", "expected": [{"document": "GDPR", "article": 33}]}`.
  Mix: 14 direct questions, 4 worded unlike the regulation's own language, 2 that the document does not
  answer (`expected: []`). Verify every expected article by reading it.
  Examples: breach notification -> 33; right to erasure -> 17; data portability -> 20; conditions for
  consent -> 7; lawful bases for processing -> 6; when a DPO is required -> 37; administrative fines -> 83.
- `EvalRunner`, active only under the Spring profile `eval`: runs all 20 through `AnswerService`, and for each
  records two results:
  - retrieval hit: an expected article is among the 4 retrieved;
  - citation hit: an expected article is in the returned `sources` (for the 2 uncovered questions: `sources` is empty).
- Output: a table in the console and `eval/report.md` with both scores, total tokens, total cost, and the date.

**Done when**
- `mvn spring-boot:run -Dspring-boot.run.profiles=eval` finishes and writes `eval/report.md`.
- Running it twice gives the same score (temperature 0), or the difference is noted.
- Target: citation score of at least 16/20. If lower, look at the retrieval score first: if retrieval misses, the
  fix is chunking (split long articles by paragraph); if retrieval hits but citation misses, the fix is the prompt.
  Spend at most 1 hour on fixes, then report the real number.

**Do not:** let the eval run inside the normal `mvn test`; it costs money and needs a key.
Do not change questions to raise the score.

## Step 6 — README, first version (2 h) — NEVER CUT

**Goal:** a visitor understands in 2 minutes what this is and what was measured.

**Build**
- Remove the debug endpoint from Step 2.
- `README.md` sections, in this order:
  1. What it does, in 3 sentences, with one example request and response.
  2. Measured results: the Step 5 scores and the cost per request, each with its date.
  3. How it works: the flow from section 2 of this plan. Masking is not built yet; mark it "planned" and do not
     describe it as working.
  4. Privacy, as it stands today: which text leaves the service and to which provider; that question text is
     never logged; that masking is planned. Never write "GDPR compliant".
  5. Limits: 20 questions is a small test set; one document; answers are a lookup aid, not legal advice;
     EU text only; embedding cost is not logged.
  6. How to run it, including the `MISTRAL_API_KEY` variable and the eval command.
  7. Corpus source and reuse notice (check the EUR-Lex legal notice wording before writing this).
- Before writing section 4, read Mistral's current data processing terms and state only what they say.

**Done when**
- A fresh clone, with only a key set, runs the service and the eval by following the README.
- Every number in the README matches `eval/report.md`.
- The README claims nothing that is not built.

---

# Stage 2

## Step 7 — Mask personal data, and measure it (3.5 h)

**Goal:** no personal data reaches the AI provider or the logs, and the masking accuracy is a measured number.

**Build**
- `PiiMasker` interface + `PiiMaskerImpl`, plain Java:
  - emails -> `[EMAIL]`; phone numbers -> `[PHONE]`; IBANs -> `[IBAN]`;
    ID numbers (13-digit IDNP and other digit runs of 9 or more) -> `[ID]`;
  - names -> `[NAME]`, by simple cues only ("my name is ...", "Mr/Mrs/Ms/Dr ..."). This will miss many names.
    That is expected and gets reported, not hidden.
- Call the masker as the first line of `AnswerService`, before the embedding call as well as the chat call.
  Both calls leave the service.
- `eval/masking-cases.json`: 30 sentences with the personal data in each one labelled by type.
- `MaskingAccuracyTest` (normal unit test, no AI, free): for each type, counts found / expected and false alarms,
  and prints a small table.
- A test that sends a question containing an email through `AnswerService` with mocked AI calls and asserts the
  text passed to both mocks contains `[EMAIL]` and not the address.

**Done when**
- The accuracy table prints per type (email, phone, IBAN, ID, name).
- Target: emails, phones, IBANs, IDs at 95% found or better. Names: whatever it measures.
- The 20-question score from Step 5 is unchanged after masking is switched on.

**Do not:** add an NER library or a second AI call to find names.

## Step 8 — Add a second document: the EU AI Act (3.5 h)

**Goal:** the service answers from two documents and names the right one in each source.

**Why:** with one document, "works for any document" is only a claim. This step tests it and records what
actually had to change.

**Build**
- Save the English text of Regulation (EU) 2024/1689 from EUR-Lex
  (`https://eur-lex.europa.eu/eli/reg/2024/1689/oj`) as `src/main/resources/corpus/ai-act-en.txt`.
  Add it to `corpus/SOURCE.md` with date and URL. Recitals and annexes are excluded.
- Make `CorpusLoader` load a list of documents, each with a name and a file. Parser rules that differ between
  the two texts go in per-document configuration, not in copied code.
- Delete `data/vector-store.json` and re-embed. Sources now read "GDPR, Article 33" or "AI Act, Article 5".
- The prompt labels each passage with document and article; validation checks both.
- Loader test for the new document.
- Write down in `eval/report.md` which files changed for this step. That list is the evidence for the
  "generic engine" claim, whatever it shows.

**Done when**
- The AI Act loads as exactly 113 articles, numbered 1 to 113; Article 5 has the title "Prohibited AI practices";
  Article 113 has the title "Entry into force and application".
- The store holds 212 chunks (99 + 113).
- "Which AI practices are prohibited?" returns AI Act, Article 5.
- The original 20 questions are re-run. A lower score is possible, because two documents now compete in search.
  Report the new number; do not hide a drop.

**Do not:** add a third document, a document-upload endpoint, or a per-request document filter.

## Step 9 — Grow the test set to 50 questions (2 h)

**Goal:** a score that is harder to dismiss as luck.

**Build**
- Add 30 entries to `eval/questions.json`: 12 GDPR, 15 AI Act, 3 that neither document answers.
  Totals: 30 GDPR, 15 AI Act, 5 uncovered. Verify every expected article by reading it.
- `eval/report.md` gains a breakdown: score per document, and for uncovered questions.

**Done when**
- The eval command reports a score out of 50 with the breakdown, total tokens, and total cost.
- The 20 original questions are unchanged in the file.

**Do not:** change or remove questions to raise the score. Spend at most 1 hour on fixes, then report the real number.

## Step 10 — Timeouts, retries, fallback (2 h) — first to cut

**Goal:** the service degrades cleanly when the provider fails.

**Build**
- A timeout on both AI calls; 2 retries with backoff on timeouts and HTTP 5xx only.
- Fallback when the chat call still fails: return the retrieved articles as sources with a message that no answer
  could be generated (HTTP 200), and log the failure without the question text.
- One test per behaviour using a mocked failing call.

**Done when:** the three tests pass.

## Step 11 — README, final version (1 h) — NEVER CUT

**Goal:** the README matches the finished repo.

**Build**
- Update the measured results: the 50-question score with its breakdown, cost per request, and the masking table.
- Privacy section: "built with data minimization in mind". What is masked, what is logged, and the stated limit
  that name masking is weak. Never write "GDPR compliant".
- "How it works": masking is now built; add two sentences on timeouts and fallback if Step 10 was done.
- Limits: update the test set size and the number of documents; keep "not legal advice".
- One short section, "Adding a document", based on the file list recorded in Step 8.

**Done when**
- Every number in the README matches `eval/report.md` and the masking table.
- A fresh clone runs the service and the eval by following the README.
