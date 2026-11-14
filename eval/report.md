# Eval report

- Date: 2026-11-14
- Chat model: mistral-small-2603; embedding model: mistral-embed-2312
- Questions: 20 (14 direct, 4 reworded, 2 uncovered). Question text is in eval/questions.json and is not repeated here.

## Scores

| Score | Result |
|---|---|
| Retrieval: an expected article is among the 4 retrieved (covered questions only) | **18/18** |
| Citation: an expected article is in `sources`; for uncovered questions, `sources` is empty | **20/20** |

## Citation score by question type

| Type | Citation hits |
|---|---|
| direct | 14/14 |
| reworded | 4/4 |
| uncovered | 2/2 |

## Usage

- Chat tokens: 33044 prompt, 2417 completion
- Chat cost: EUR 0.00552839 (embedding calls are not included)

## Per question

| id | type | expected | retrieved | cited | retrieval | citation | tokens in/out | cost EUR | error |
|---|---|---|---|---|---|---|---|---|---|
| 1 | direct | 33 | 33, 34, 51, 19 | 33 | hit | hit | 1207/46 | 0.00018037 |  |
| 2 | direct | 17 | 17, 16, 15, 19 | 17 | hit | hit | 1352/198 | 0.00027674 |  |
| 3 | direct | 20 | 20, 15, 16, 1 | 20 | hit | hit | 1062/108 | 0.00019314 |  |
| 4 | direct | 7 | 7, 8, 53, 6 | 7 | hit | hit | 1643/203 | 0.00031712 |  |
| 5 | direct | 6 | 6, 5, 10, 29 | 6 | hit | hit | 1544/102 | 0.00025274 |  |
| 6 | direct | 37 | 37, 38, 39, 27 | 37 | hit | hit | 1394/136 | 0.00025058 |  |
| 7 | direct | 83 | 83, 84, 5, 32 | 83, 5 | hit | hit | 2297/81 | 0.00033992 |  |
| 8 | direct | 13 | 13, 14, 11, 15 | 13 | hit | hit | 2465/373 | 0.00051068 |  |
| 9 | direct | 35 | 35, 24, 39, 36 | 35 | hit | hit | 1965/215 | 0.00036510 |  |
| 10 | direct | 28 | 28, 29, 24, 27 | 28 | hit | hit | 1767/227 | 0.00034548 |  |
| 11 | direct | 34 | 34, 33, 19, 14 | 34 | hit | hit | 1967/60 | 0.00028631 |  |
| 12 | direct | 5 | 5, 10, 29, 32 | 5 | hit | hit | 1064/181 | 0.00023063 |  |
| 13 | direct | 45 | 45, 48, 44, 46 | 45 | hit | hit | 1902/76 | 0.00028602 |  |
| 14 | direct | 8 | 8, 7, 12, 13 | 8 | hit | hit | 2049/59 | 0.00029646 |  |
| 15 | reworded | 15 | 15, 13, 14, 12 | 15 | hit | hit | 3036/75 | 0.00043293 |  |
| 16 | reworded | 30 | 30, 24, 11, 59 | 30 | hit | hit | 1180/51 | 0.00017941 |  |
| 17 | reworded | 22 | 22, 88, 11, 21 | 22 | hit | hit | 1265/115 | 0.00022310 |  |
| 18 | reworded | 77 | 77, 80, 34, 79 | 77, 80 | hit | hit | 1064/64 | 0.00017096 |  |
| 19 | uncovered | none | 99, 3, 84, 51 | none | n/a | hit | 733/19 | 0.00010498 |  |
| 20 | uncovered | none | 8, 83, 84, 82 | none | n/a | hit | 2088/28 | 0.00028572 |  |
