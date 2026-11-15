# Eval report

- Date: 2026-11-15
- Chat model: mistral-small-2603; embedding model: mistral-embed-2312
- Questions: 20 (14 direct, 4 reworded, 2 uncovered). Question text is in eval/questions.json and is not repeated here.

## Scores

| Score | Result |
|---|---|
| Retrieval: an expected article is among the 4 retrieved (covered questions only) | **18/18** |
| Citation: an expected article is in `sources`; for uncovered questions, `sources` is empty | **19/20** |

## Citation score by question type

| Type | Citation hits |
|---|---|
| direct | 14/14 |
| reworded | 3/4 |
| uncovered | 2/2 |

## Usage

- Chat tokens: 35188 prompt, 3455 completion
- Chat cost: EUR 0.00633649 (embedding calls are not included)

## Per question

| id | type | expected | retrieved | cited | retrieval | citation | tokens in/out | cost EUR | error |
|---|---|---|---|---|---|---|---|---|---|
| 1 | direct | 33 | 33, 34, 51, 19 | 33 | hit | hit | 1250/60 | 0.00019310 |  |
| 2 | direct | 17 | 17, 16, 15, 19 | 17 | hit | hit | 1395/206 | 0.00028641 |  |
| 3 | direct | 20 | 20, 15, 16, 1 | 20 | hit | hit | 1105/130 | 0.00020995 |  |
| 4 | direct | 7 | 7, 8, 61, 53 | 7, 8 | hit | hit | 1149/282 | 0.00029319 |  |
| 5 | direct | 6 | 6, 5, 10, 29 | 6, 10 | hit | hit | 1587/249 | 0.00033330 |  |
| 6 | direct | 37 | 37, 38, 39, 27 | 37 | hit | hit | 1437/269 | 0.00032400 |  |
| 7 | direct | 83 | 83, 84, 100, 5 | 83, 5 | hit | hit | 2688/110 | 0.00040554 |  |
| 8 | direct | 13 | 13, 14, 11, 15 | 13 | hit | hit | 2508/422 | 0.00054126 |  |
| 9 | direct | 35 | 35, 24, 39, 36 | 35 | hit | hit | 2008/273 | 0.00040027 |  |
| 10 | direct | 28 | 28, 29, 24, 27 | 28 | hit | hit | 1810/268 | 0.00037198 |  |
| 11 | direct | 34 | 34, 33, 19, 14 | 34 | hit | hit | 2010/79 | 0.00030159 |  |
| 12 | direct | 5 | 5, 10, 29, 32 | 5 | hit | hit | 1107/394 | 0.00034485 |  |
| 13 | direct | 45 | 45, 44, 48, 46 | 45, 44 | hit | hit | 1945/105 | 0.00030640 |  |
| 14 | direct | 8 | 8, 7, 61, 12 | 8 | hit | hit | 1693/80 | 0.00026089 |  |
| 15 | reworded | 15 | 15, 13, 14, 91 | 15 | hit | hit | 2671/93 | 0.00039466 |  |
| 16 | reworded | 30 | 12, 30, 19, 24 | none | hit | miss | 1430/93 | 0.00023333 |  |
| 17 | reworded | 22 | 22, 86, 14, 5 | 22 | hit | hit | 3522/161 | 0.00053997 |  |
| 18 | reworded | 77 | 77, 80, 85, 34 | 77, 80 | hit | hit | 1038/96 | 0.00018390 |  |
| 19 | uncovered | none | 64, 99, 3, 51 | none | n/a | hit | 704/24 | 0.00010376 |  |
| 20 | uncovered | none | 8, 83, 84, 82 | none | n/a | hit | 2131/61 | 0.00030814 |  |

## Files changed to add the EU AI Act (Step 8, 2026-11-15)

Added by hand after the run; EvalRunner does not write this section and overwrites the file on its next run.

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
is returned. The table above lists article numbers without the document, so it does not show which document the
other retrieved articles came from.
