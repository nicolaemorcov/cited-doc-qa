# Eval report

- Date: 2026-11-15
- Chat model: mistral-small-2603; embedding model: mistral-embed-2312
- Questions: 50 (32 direct, 13 reworded, 5 uncovered). Question text is in eval/questions.json and is not repeated here.

## Scores

| Score | Result |
|---|---|
| Retrieval: an expected article is among the 4 retrieved (covered questions only) | **44/45** |
| Citation: an expected article is in `sources`; for uncovered questions, `sources` is empty | **49/50** |

## Scores by document

| Group | Questions | Retrieval | Citation |
|---|---|---|---|
| GDPR | 30 | 30/30 | 30/30 |
| AI Act | 15 | 14/15 | 14/15 |
| uncovered | 5 | n/a | 5/5 |

## Citation score by question type

| Type | Citation hits |
|---|---|
| direct | 32/32 |
| reworded | 12/13 |
| uncovered | 5/5 |

## Usage

- Chat tokens: 102608 prompt, 9146 completion
- Chat cost: EUR 0.01800350 (embedding calls are not included)

## Per question

| id | type | expected | retrieved | cited | retrieval | citation | tokens in/out | cost EUR | error |
|---|---|---|---|---|---|---|---|---|---|
| 1 | direct | GDPR 33 | GDPR 33, GDPR 34, GDPR 51, GDPR 19 | GDPR 33 | hit | hit | 1271/62 | 0.00019685 |  |
| 2 | direct | GDPR 17 | GDPR 17, GDPR 16, GDPR 15, GDPR 19 | GDPR 17 | hit | hit | 1416/191 | 0.00028149 |  |
| 3 | direct | GDPR 20 | GDPR 20, GDPR 15, GDPR 16, GDPR 1 | GDPR 20 | hit | hit | 1126/129 | 0.00021217 |  |
| 4 | direct | GDPR 7 | GDPR 7, GDPR 8, AI Act 61, GDPR 53 | GDPR 7, GDPR 8 | hit | hit | 1170/281 | 0.00029541 |  |
| 5 | direct | GDPR 6 | GDPR 6, GDPR 5, GDPR 10, GDPR 29 | GDPR 6 | hit | hit | 1608/234 | 0.00032838 |  |
| 6 | direct | GDPR 37 | GDPR 37, GDPR 38, GDPR 39, GDPR 27 | GDPR 37 | hit | hit | 1458/183 | 0.00028287 |  |
| 7 | direct | GDPR 83 | GDPR 83, GDPR 84, AI Act 100, GDPR 5 | GDPR 83 | hit | hit | 2709/98 | 0.00040215 |  |
| 8 | direct | GDPR 13 | GDPR 13, GDPR 14, GDPR 11, GDPR 15 | GDPR 13 | hit | hit | 2529/388 | 0.00052665 |  |
| 9 | direct | GDPR 35 | GDPR 35, GDPR 24, GDPR 39, GDPR 36 | GDPR 35 | hit | hit | 2029/253 | 0.00039280 |  |
| 10 | direct | GDPR 28 | GDPR 28, GDPR 29, GDPR 24, GDPR 27 | GDPR 28 | hit | hit | 1831/277 | 0.00037930 |  |
| 11 | direct | GDPR 34 | GDPR 34, GDPR 33, GDPR 19, GDPR 14 | GDPR 34 | hit | hit | 2031/78 | 0.00030381 |  |
| 12 | direct | GDPR 5 | GDPR 5, GDPR 10, GDPR 29, GDPR 32 | GDPR 5 | hit | hit | 1128/301 | 0.00030015 |  |
| 13 | direct | GDPR 45 | GDPR 45, GDPR 44, GDPR 48, GDPR 46 | GDPR 45 | hit | hit | 1966/90 | 0.00030148 |  |
| 14 | direct | GDPR 8 | GDPR 8, GDPR 7, AI Act 61, GDPR 12 | GDPR 8 | hit | hit | 1714/88 | 0.00026770 |  |
| 15 | reworded | GDPR 15 | GDPR 15, GDPR 13, GDPR 14, AI Act 91 | GDPR 15 | hit | hit | 2692/109 | 0.00040555 |  |
| 16 | reworded | GDPR 30 | AI Act 12, GDPR 30, AI Act 19, GDPR 24 | GDPR 30, AI Act 12, AI Act 19 | hit | hit | 1451/220 | 0.00030083 |  |
| 17 | reworded | GDPR 22 | GDPR 22, AI Act 86, AI Act 14, AI Act 5 | GDPR 22 | hit | hit | 3543/227 | 0.00057636 |  |
| 18 | reworded | GDPR 77 | GDPR 77, GDPR 80, AI Act 85, GDPR 34 | GDPR 77 | hit | hit | 1059/112 | 0.00019479 |  |
| 19 | uncovered | none | AI Act 64, GDPR 99, GDPR 3, GDPR 84 | none | n/a | hit | 618/20 | 0.00009054 |  |
| 20 | uncovered | none | GDPR 8, GDPR 83, GDPR 84, GDPR 82 | none | n/a | hit | 2152/33 | 0.00029659 |  |
| 21 | direct | GDPR 16 | GDPR 16, GDPR 15, GDPR 21, GDPR 5 | GDPR 16 | hit | hit | 1513/85 | 0.00024004 |  |
| 22 | direct | GDPR 18 | GDPR 18, GDPR 23, GDPR 19, GDPR 15 | GDPR 18 | hit | hit | 1587/178 | 0.00029709 |  |
| 23 | direct | GDPR 21 | GDPR 21, GDPR 1, GDPR 11, GDPR 18 | GDPR 21 | hit | hit | 1156/108 | 0.00020536 |  |
| 24 | direct | GDPR 9 | GDPR 9, GDPR 5, GDPR 10, GDPR 49 | GDPR 9 | hit | hit | 2390/379 | 0.00050399 |  |
| 25 | direct | GDPR 25 | GDPR 25, GDPR 24, GDPR 37, GDPR 38 | GDPR 25 | hit | hit | 1339/205 | 0.00027862 |  |
| 26 | direct | GDPR 32 | GDPR 32, GDPR 24, GDPR 10, GDPR 25 | GDPR 32 | hit | hit | 1113/283 | 0.00028902 |  |
| 27 | direct | GDPR 39 | GDPR 39, GDPR 38, GDPR 37, GDPR 57 | GDPR 39 | hit | hit | 2089/278 | 0.00041335 |  |
| 28 | direct | GDPR 46 | GDPR 46, GDPR 45, GDPR 48, GDPR 44 | GDPR 46 | hit | hit | 1967/249 | 0.00038270 |  |
| 29 | reworded | GDPR 12 | GDPR 15, GDPR 12, GDPR 20, GDPR 16 | GDPR 12, GDPR 15 | hit | hit | 1756/133 | 0.00029611 |  |
| 30 | reworded | GDPR 82 | GDPR 82, GDPR 34, GDPR 33, GDPR 48 | GDPR 82 | hit | hit | 1380/93 | 0.00022683 |  |
| 31 | reworded | GDPR 3 | GDPR 3, GDPR 2, GDPR 48, GDPR 27 | GDPR 3 | hit | hit | 1121/157 | 0.00022580 |  |
| 32 | reworded | GDPR 14 | GDPR 14, GDPR 34, GDPR 13, GDPR 19 | GDPR 14 | hit | hit | 2366/393 | 0.00050801 |  |
| 33 | direct | AI Act 3 | AI Act 3, AI Act 4, AI Act 8, AI Act 20 | AI Act 3 | hit | hit | 4574/93 | 0.00064205 |  |
| 34 | direct | AI Act 6 | AI Act 6, AI Act 80, AI Act 51, AI Act 9 | AI Act 6, AI Act 51, AI Act 9 | hit | hit | 2706/392 | 0.00055170 |  |
| 35 | direct | AI Act 9 | AI Act 9, AI Act 16, AI Act 82, AI Act 8 | AI Act 9 | hit | hit | 2043/232 | 0.00038391 |  |
| 36 | direct | AI Act 10 | AI Act 10, AI Act 15, AI Act 17, AI Act 9 | AI Act 10 | hit | hit | 3183/453 | 0.00064482 |  |
| 37 | direct | AI Act 14 | AI Act 14, AI Act 9, AI Act 82, AI Act 16 | AI Act 14 | hit | hit | 2471/230 | 0.00043853 |  |
| 38 | direct | AI Act 26 | AI Act 26, AI Act 16, AI Act 13, AI Act 20 | AI Act 26 | hit | hit | 2996/412 | 0.00059960 |  |
| 39 | direct | AI Act 27 | AI Act 27, GDPR 35, AI Act 26, AI Act 86 | AI Act 27 | hit | hit | 3426/118 | 0.00050556 |  |
| 40 | direct | AI Act 51 | AI Act 51, AI Act 55, AI Act 52, AI Act 90 | AI Act 51, AI Act 52 | hit | hit | 1657/256 | 0.00034597 |  |
| 41 | direct | AI Act 53 | AI Act 53, AI Act 55, AI Act 94, AI Act 25 | AI Act 53 | hit | hit | 2146/323 | 0.00044371 |  |
| 42 | direct | AI Act 57 | AI Act 57, AI Act 58, AI Act 59, AI Act 64 | AI Act 57 | hit | hit | 3621/58 | 0.00050031 |  |
| 43 | reworded | AI Act 50 | AI Act 50, AI Act 4, AI Act 13, AI Act 19 | AI Act 50 | hit | hit | 2004/94 | 0.00030846 |  |
| 44 | reworded | AI Act 4 | AI Act 4, AI Act 20, AI Act 13, AI Act 8 | AI Act 4 | hit | hit | 1400/106 | 0.00023606 |  |
| 45 | reworded | AI Act 99 | AI Act 101, AI Act 99, AI Act 100, AI Act 5 | AI Act 99, AI Act 5 | hit | hit | 4739/105 | 0.00066962 |  |
| 46 | reworded | AI Act 73 | AI Act 73, AI Act 20, AI Act 82, AI Act 79 | AI Act 73 | hit | hit | 2745/161 | 0.00043896 |  |
| 47 | reworded | AI Act 113 | AI Act 5, AI Act 111, AI Act 83, AI Act 20 | AI Act 111, AI Act 5 | miss | miss | 3226/115 | 0.00047803 |  |
| 48 | uncovered | none | GDPR 88, AI Act 18, AI Act 81, AI Act 44 | none | n/a | hit | 1416/29 | 0.00019887 |  |
| 49 | uncovered | none | AI Act 85, AI Act 18, AI Act 47, AI Act 81 | none | n/a | hit | 1337/28 | 0.00018809 |  |
| 50 | uncovered | none | AI Act 18, GDPR 47, GDPR 71, GDPR 96 | none | n/a | hit | 1640/26 | 0.00022646 |  |

## Notes (hand-written, kept by EvalRunner)

Everything from this heading down is written by hand; EvalRunner copies it into each new report.

### Files changed to add the EU AI Act (Step 8, 2026-11-15)

Only data and configuration, the first time a document is added:
- `src/main/resources/corpus/ai-act-en.txt` (new: the text)
- `src/main/resources/corpus/SOURCE.md` (provenance entry)
- `src/main/resources/application.yml` (one `citedqa.corpus.documents` entry; the only parser rule that differs is the
  section heading: GDPR `Section 1`, AI Act `SECTION 1`)

One-time code changes, needed because the engine had assumed a single document:
- `corpus/DocumentSource.java`, `corpus/CorpusProperties.java` (new: per-document parser settings, bound from YAML)
- `corpus/CorpusLoaderImpl.java` (constants replaced by the configured list of documents)
- `CitedDocQaApplication.java` (`@ConfigurationPropertiesScan`)
- `answer/Citation.java` (new), `answer/ModelAnswer.java`, `answer/AnswerServiceImpl.java` (passages labelled with
  document and article; validation matches both, because article numbers repeat across documents)
- Tests: `CorpusLoaderImplTest.java`, `AnswerServiceImplTest.java`

Effect on the original 20 questions (two runs, same scores): citation 20/20 -> 19/20, retrieval 18/18 -> 18/18.
Question 16 (expects GDPR Article 30) still retrieves Article 30, now at rank 2 instead of 1, but no valid citation
is returned. The Step 8 report listed article numbers without the document; Step 9 added the document to every label.

### Step 9: 50 questions and the question 16 prompt fix (2026-11-15)

Questions 21-50 were added (12 GDPR, 15 AI Act, 3 uncovered); questions 1-20 are unchanged. The new questions were
scored once with the Step 8 prompt before the prompt was changed, so the fix was not tuned on them.

| Run | Prompt rule 3 | Retrieval | Citation | GDPR | AI Act | Uncovered | Citation misses |
|---|---|---|---|---|---|---|---|
| Baseline | Step 8 | 44/45 | 48/50 | 29/30 | 14/15 | 5/5 | 16, 47 |
| Fix, run 1 | new | 44/45 | 48/50 | 30/30 | 13/15 | 5/5 | 42, 47 |
| Fix, run 2 (tables above) | new | 44/45 | 49/50 | 30/30 | 14/15 | 5/5 | 47 |

- Question 16 (GDPR 30): retrieval hit, but the model returned no sources. Replayed outside the app with the same
  prompt and passages: 3/3 empty with AI Act 12 and 19 in the context, 2/2 citing GDPR 30 with the four GDPR-only
  articles retrieved before Step 8. Rule 3 now says to answer and cite when an article answers "even in part". After
  the change it hits in both runs, but also cites AI Act 12 and 19, which the citation score does not penalise.
- Question 42 (AI Act 57): missed once out of two runs with the new prompt; a replay gave AI Act 57 in 4/4 runs with
  each prompt. Treated as run-to-run variation at temperature 0, not as an effect of the change.
- Question 47 (AI Act 113): retrieval miss in every run (Article 113 is not in the top 4). By the plan's rule the fix
  would be chunking, not the prompt; not attempted within the one-hour fix budget.
- The score checks citations, not the answer text. In one replay of question 16 the model cited GDPR 30 correctly
  but wrote "25 or more persons" where Article 30(5) says 250.
- AI Act Article 51(2) was corrected in the corpus ("1025" -> "10^25", see `corpus/SOURCE.md`) and the store was
  re-embedded before these runs.
