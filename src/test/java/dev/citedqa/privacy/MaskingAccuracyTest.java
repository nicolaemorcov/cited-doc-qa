package dev.citedqa.privacy;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Measures PiiMaskerImpl against the labelled sentences in eval/masking-cases.json.
 * Plain Java, no AI call, runs in the normal mvn test.
 *
 * Scoring, per sentence and per type:
 * - found = labels whose value is gone from the masked text, capped at the number of tags of that type produced;
 * - false alarms = tags of that type produced minus found.
 * So a phone masked as [ID] counts as a PHONE miss and an ID false alarm.
 */
class MaskingAccuracyTest {

    private static final Logger log = LoggerFactory.getLogger(MaskingAccuracyTest.class);

    private static final Path CASES_FILE = Path.of("eval", "masking-cases.json");
    private static final List<String> TYPES = List.of("EMAIL", "PHONE", "IBAN", "ID", "NAME");
    private static final List<String> TYPES_WITH_TARGET = List.of("EMAIL", "PHONE", "IBAN", "ID");
    private static final int TARGET_PERCENT = 95;

    private final PiiMasker piiMasker = new PiiMaskerImpl();

    @Test
    void shouldFindAtLeast95PercentOfEmailsPhonesIbansAndIds() throws IOException {
        // Given
        List<MaskingCase> maskingCases = readCases();

        // When
        Map<String, TypeScore> typeScores = new LinkedHashMap<>();
        for (String type : TYPES) {
            typeScores.put(type, score(type, maskingCases));
        }

        // Then
        log.info("Masking accuracy on {} sentences from {}\n{}", maskingCases.size(), CASES_FILE,
                formatTable(typeScores));
        assertAll(TYPES_WITH_TARGET.stream().map(type -> (Executable) () -> {
            TypeScore typeScore = typeScores.get(type);
            assertTrue(typeScore.found() * 100 >= TARGET_PERCENT * typeScore.expected(),
                    type + " found " + typeScore.found() + "/" + typeScore.expected() + ", target " + TARGET_PERCENT + "%");
        }));
    }

    private List<MaskingCase> readCases() throws IOException {
        JsonMapper jsonMapper = JsonMapper.builder().build();
        List<MaskingCase> maskingCases;
        try (InputStream inputStream = Files.newInputStream(CASES_FILE)) {
            maskingCases = jsonMapper.readValue(inputStream, new TypeReference<List<MaskingCase>>() {
            });
        }
        for (MaskingCase maskingCase : maskingCases) {
            for (PiiLabel piiLabel : maskingCase.pii()) {
                if (!TYPES.contains(piiLabel.type())) {
                    throw new IllegalStateException("Case " + maskingCase.id() + " has unknown type " + piiLabel.type());
                }
                if (!maskingCase.text().contains(piiLabel.value())) {
                    throw new IllegalStateException("Case " + maskingCase.id() + " labels a value that is not in its text");
                }
            }
        }
        return maskingCases;
    }

    private TypeScore score(String type, List<MaskingCase> maskingCases) {
        String tag = "[" + type + "]";
        int expected = 0;
        int found = 0;
        int falseAlarms = 0;
        List<Integer> missedCaseIds = new ArrayList<>();
        List<Integer> falseAlarmCaseIds = new ArrayList<>();
        for (MaskingCase maskingCase : maskingCases) {
            String maskedText = piiMasker.mask(maskingCase.text());
            List<PiiLabel> piiLabels = maskingCase.pii().stream()
                    .filter(piiLabel -> piiLabel.type().equals(type))
                    .toList();
            int produced = countOccurrences(maskedText, tag);
            int removed = (int) piiLabels.stream().filter(piiLabel -> !maskedText.contains(piiLabel.value())).count();
            int caseFound = Math.min(removed, produced);
            expected += piiLabels.size();
            found += caseFound;
            falseAlarms += produced - caseFound;
            if (caseFound < piiLabels.size()) {
                missedCaseIds.add(maskingCase.id());
            }
            if (produced > caseFound) {
                falseAlarmCaseIds.add(maskingCase.id());
            }
        }
        return new TypeScore(expected, found, falseAlarms, missedCaseIds, falseAlarmCaseIds);
    }

    private int countOccurrences(String text, String tag) {
        int count = 0;
        int index = text.indexOf(tag);
        while (index >= 0) {
            count++;
            index = text.indexOf(tag, index + tag.length());
        }
        return count;
    }

    private String formatTable(Map<String, TypeScore> typeScores) {
        StringBuilder table = new StringBuilder();
        table.append("| Type | Found | Found % | False alarms | Missed in cases | False alarm in cases |\n");
        table.append("|---|---|---|---|---|---|\n");
        for (Map.Entry<String, TypeScore> entry : typeScores.entrySet()) {
            TypeScore typeScore = entry.getValue();
            double foundPercent = typeScore.expected() == 0 ? 0.0 : 100.0 * typeScore.found() / typeScore.expected();
            table.append(String.format(Locale.ROOT, "| %s | %d/%d | %.1f%% | %d | %s | %s |%n",
                    entry.getKey(), typeScore.found(), typeScore.expected(), foundPercent, typeScore.falseAlarms(),
                    typeScore.missedCaseIds(), typeScore.falseAlarmCaseIds()));
        }
        return table.toString();
    }

    record MaskingCase(int id, String text, List<PiiLabel> pii) {
    }

    record PiiLabel(String type, String value) {
    }

    record TypeScore(int expected, int found, int falseAlarms, List<Integer> missedCaseIds,
                     List<Integer> falseAlarmCaseIds) {
    }
}
