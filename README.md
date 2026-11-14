# cited-doc-qa

A Spring Boot service that answers questions about a public document and cites the article each answer came from.
It finds the four most related articles, asks an AI model to answer only from them, and then checks in plain Java
that every cited article was really among those four. The first document is the GDPR (Regulation (EU) 2016/679, English text).

```
POST /api/questions
{"question": "How long do I have to report a data breach?"}
```

```json
{
  "requestId": "460c2867-a897-4be8-8790-f435ecc89bfa",
  "answer": "The controller shall without undue delay and, where feasible, not later than 72 hours after having become aware of a personal data breach, notify the personal data breach to the supervisory authority, unless the personal data breach is unlikely to result in a risk to the rights and freedoms of natural persons. Where the notification to the supervisory authority is not made within 72 hours, it shall be accompanied by reasons for the delay.",
  "sources": [
    {"document": "GDPR", "section": "Article 33", "title": "Notification of a personal data breach to the supervisory authority"}
  ]
}
```

A question the document does not answer returns `"The document does not cover this question."` with empty `sources`.

## Measured results

Measured on 2026-11-14 with `mistral-small-2603` (chat, temperature 0) and `mistral-embed-2312` (embeddings).
Source: [`eval/report.md`](eval/report.md).

| Measure | Result |
|---|---|
| Citation score: an expected article is in `sources`; for the 2 questions the GDPR does not answer, `sources` is empty | **20/20** |
| Retrieval score: an expected article is among the 4 retrieved (18 questions the GDPR answers) | **18/18** |
| Chat tokens for the 20 questions | 33,044 prompt, 2,417 completion |
| Chat cost for the 20 questions | EUR 0.00552839 |
| Average per question (report totals divided by 20) | about 1,652 prompt and 121 completion tokens, about EUR 0.00028 |

The test set has 14 direct questions, 4 worded unlike the regulation ("Can a company decide about me using only an
algorithm?") and 2 the GDPR does not answer. Every expected article was checked against its text.

Two caveats. The set is small, and the direct questions use the regulation's own vocabulary; in every answered question
the expected article was ranked first, so a perfect score here says little about harder questions. Two runs gave the
same scores and identical retrieval, but the model's wording (completion tokens) varied between runs.

Prices are the EUR prices of the Mistral console price catalog read on 2026-11-14 (EUR 0.13 per million input tokens,
EUR 0.51 per million output tokens). The cost of the embedding calls is not included.

## How it works

```
POST /api/questions  {"question": "..."}
  1. Mask      planned (not built yet): replace personal data with [EMAIL], [PHONE], ...
  2. Search    1 embedding call   find the 4 most related articles
  3. Ask       1 chat call        question + those 4 articles -> answer + cited article numbers
  4. Validate  plain Java         keep only cited articles that were really retrieved
  5. Log       plain Java         request id, tokens, cost, latency, cited articles (never the question text)
-> {"requestId": "...", "answer": "...", "sources": [{"document": "...", "section": "Article 33", "title": "..."}]}
```

- The regulation text is split into its 99 articles at startup (recitals excluded). Each article is one search unit.
- On first start each article is embedded once and the result is saved to `data/vector-store.json`; later starts load
  that file and embed nothing.
- The chat model returns JSON (`answer` plus a list of article numbers) through Spring AI structured output. If none of
  the cited numbers was among the 4 retrieved articles, the service returns the "not covered" answer.
- Each request writes one log line, for example:
  `requestId=d9bb3902-..., promptTokens=1277, completionTokens=101, costEur=0.00021752, latencyMs=3009, sources=[33]`.

Built with Java 21, Spring Boot 4.1.2, Spring AI 2.0.2 and Mistral AI. The vector store is Spring AI's in-memory
`SimpleVectorStore`.

## Privacy, as it stands today

What leaves the service, and where it goes (Mistral AI, `api.mistral.ai`):

- On first start: the 99 article texts and titles, to Mistral's embeddings API.
- For each question: the question text to Mistral's embeddings API, then the question text together with the 4 retrieved
  article texts to Mistral's chat API.

Nothing else is sent. Personal data in a question is **not masked yet**: masking is planned and not built, so do not
put personal data in questions.

What is logged: one line per request with the request id, token counts, cost, latency and the cited article numbers.
The question text is never written to logs or files; the eval report identifies questions by id only.

What Mistral's terms say: Mistral's Data Processing Addendum (effective 27 July 2026, read 2026-11-14) states that the
customer is the controller and "Mistral AI Processes the Personal Data on behalf of Customer as a Processor". It also
allows Mistral to process data as a controller for "Training its artificial intelligence models ... unless Customer is
or has opted-out of training". For the account used to build and measure this project, the API setting "Allow the use
of your API calls to train Mistral's AI models" was off before any request was sent. This is an account setting, not
something the code controls: if you run the service with your own key, check it in your Mistral console
(Admin, Privacy). The addendum does not state where data is hosted, so this README makes no claim about it.

## Limits

- 20 questions is a small test set.
- One document, and only EU regulation text in English: no national laws, guidelines or case law.
- Answers are a lookup aid, not legal advice. Check the cited article in the Official Journal.
- The cost of embedding calls is not logged; only the chat call's cost is.
- `SimpleVectorStore` keeps all vectors in memory. That fits 99 articles; a larger corpus would need a vector database.

## How to run it

Requirements: Java 21, Maven (tested with Maven 3.9.9), and a Mistral API key. On 2026-11-14 the Mistral console did
not activate API keys on the Free plan; this project used the Pay-as-you-go plan.

Set the key as the environment variable `MISTRAL_API_KEY`. Reading it from a prompt keeps it out of your shell history:

```powershell
# PowerShell
$env:MISTRAL_API_KEY = Read-Host "Mistral API key"
```

```bash
# bash
read -rs -p "Mistral API key: " MISTRAL_API_KEY && export MISTRAL_API_KEY
```

Start the service (port 8080) from the repository root, in the same terminal:

```bash
mvn spring-boot:run
```

The first start embeds the 99 articles (on 2026-11-14 this took about 80 seconds and about 42,000 embedding tokens,
roughly EUR 0.003) and logs `Embedded 99 articles`. Later starts log `Embedded 0 articles`. If you change the embedding
model, delete `data/vector-store.json` so the articles are embedded again with the new model.

Ask a question:

```bash
curl -s -X POST http://localhost:8080/api/questions -H "Content-Type: application/json" -d '{"question":"How long do I have to report a data breach?"}'
```

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/questions -ContentType "application/json" -Body '{"question":"How long do I have to report a data breach?"}'
```

A blank question returns HTTP 400.

Run the eval (20 questions, about EUR 0.006 per run on 2026-11-14 prices). It starts no web server, writes
`eval/report.md` and exits:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=eval
```

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=eval"
```

Run the unit tests (no API key needed, no calls to Mistral):

```bash
mvn test
```

## Corpus source and reuse

- Document: Regulation (EU) 2016/679 (General Data Protection Regulation), English text.
- Source: EUR-Lex, https://eur-lex.europa.eu/eli/reg/2016/679/oj, downloaded 2026-11-13.
- Version: the original Official Journal text of 4 May 2016 (OJ L 119), without later corrigenda.
- Copyright: © European Union, https://eur-lex.europa.eu. Reuse of EU documents is governed by Commission Decision
  2011/833/EU of 12 December 2011 on the reuse of Commission documents, which allows reuse provided the source is
  acknowledged and changes are indicated.
- Changes made here: the text file in `src/main/resources/corpus/gdpr-en.txt` is stored as downloaded. At startup the
  service splits it into articles and leaves out the recitals and the closing formula; answers quote or paraphrase
  those articles.
- Only the Official Journal of the European Union, including its electronic edition on EUR-Lex, is authentic and
  produces legal effects.

## Licence

Apache License 2.0. See [LICENSE](LICENSE).
