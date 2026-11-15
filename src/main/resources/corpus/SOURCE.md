# Corpus sources

## gdpr-en.txt

- Document: Regulation (EU) 2016/679 (General Data Protection Regulation), English text
- Source URL: https://eur-lex.europa.eu/eli/reg/2016/679/oj
- Downloaded: 2026-11-13
- Version: original Official Journal text of 4 May 2016 (OJ L 119), without later corrigenda

## ai-act-en.txt

- Document: Regulation (EU) 2024/1689 (Artificial Intelligence Act), English text
- Source URL (as in PLAN.md): https://eur-lex.europa.eu/eli/reg/2024/1689/oj
- Actually saved from: http://publications.europa.eu/resource/cellar/dc8116a1-3fe6-11ef-865a-01aa75ed71a1.0006.03/DOC_1
  (the Publications Office repository that `http://publications.europa.eu/resource/celex/32024R1689` redirects to;
  EUR-Lex was "temporarily not fully available" on the download date)
- Downloaded: 2026-11-15
- Version: Official Journal text, OJ L, 2024/1689, 12.7.2024 (document `L_202401689EN`)
- Recitals and annexes are in the file but are not loaded (the loader reads only between the start and end markers)
- The title of Article 1 reads "Subject matter`" with a trailing backtick in the official text; kept as published
- Corrected 2026-11-15: Article 51(2) read "greater than 1025" because the browser copy flattened the superscript; the
  official text has 10 with superscript 25. Written as "10^25". It is the only non-footnote superscript in the act
  (checked in the source XHTML, class `oj-super`).
