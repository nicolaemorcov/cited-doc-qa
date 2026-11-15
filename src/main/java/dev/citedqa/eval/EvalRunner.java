package dev.citedqa.eval;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import dev.citedqa.answer.AnswerResult;
import dev.citedqa.answer.AnswerService;
import dev.citedqa.corpus.Article;
import dev.citedqa.web.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Runs eval/questions.json through AnswerService and writes eval/report.md.
 * Active only under the "eval" profile, so it never runs in normal starts or in mvn test.
 * The report identifies questions by id only; question text is never written out.
 * A hand-written section that starts with NOTES_HEADING is carried over from the previous report.
 */
@Component
@Profile("eval")
public class EvalRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EvalRunner.class);

    private static final Path QUESTIONS_FILE = Path.of("eval", "questions.json");
    private static final Path REPORT_FILE = Path.of("eval", "report.md");
    private static final String SECTION_PREFIX = "Article ";
    private static final String UNCOVERED_GROUP = "uncovered";
    private static final String NOTES_HEADING = "## Notes (hand-written, kept by EvalRunner)";

    private final AnswerService answerService;
    private final JsonMapper jsonMapper;
    private final String chatModel;
    private final String embeddingModel;

    public EvalRunner(AnswerService answerService, JsonMapper jsonMapper,
                      @Value("${spring.ai.mistralai.chat.model}") String chatModel,
                      @Value("${spring.ai.mistralai.embedding.model}") String embeddingModel) {
        this.answerService = answerService;
        this.jsonMapper = jsonMapper;
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws IOException {
        List<EvalQuestion> evalQuestions = readQuestions();
        List<EvalResult> evalResults = new ArrayList<>();
        for (EvalQuestion evalQuestion : evalQuestions) {
            evalResults.add(evaluate(evalQuestion));
        }
        String report = buildReport(evalResults) + keptNotes();
        Files.writeString(REPORT_FILE, report, StandardCharsets.UTF_8);
        log.info("Eval report written to {}\n{}", REPORT_FILE, report);

        long failedCount = evalResults.stream().filter(evalResult -> evalResult.error() != null).count();
        if (failedCount > 0) {
            throw new IllegalStateException(failedCount + " eval question(s) failed; see " + REPORT_FILE);
        }
    }

    /**
     * Returns the hand-written notes section of the existing report, from NOTES_HEADING
     * to the end, or an empty string when there is no report or no such section.
     */
    private String keptNotes() throws IOException {
        if (!Files.exists(REPORT_FILE)) {
            return "";
        }
        String existingReport = Files.readString(REPORT_FILE, StandardCharsets.UTF_8);
        int notesIndex = existingReport.indexOf(NOTES_HEADING);
        return notesIndex < 0 ? "" : "\n" + existingReport.substring(notesIndex);
    }

    private List<EvalQuestion> readQuestions() throws IOException {
        try (InputStream inputStream = Files.newInputStream(QUESTIONS_FILE)) {
            return jsonMapper.readValue(inputStream, new TypeReference<List<EvalQuestion>>() {
            });
        }
    }

    private EvalResult evaluate(EvalQuestion evalQuestion) {
        List<String> expected = evalQuestion.expected().stream()
                .map(expectedSource -> label(expectedSource.document(), expectedSource.article()))
                .toList();
        boolean uncovered = expected.isEmpty();
        String group = uncovered ? UNCOVERED_GROUP : evalQuestion.expected().get(0).document();
        try {
            AnswerResult answerResult = answerService.answer(evalQuestion.question());
            List<Source> sources = answerResult.response().sources();
            Boolean retrievalHit = uncovered ? null : evalQuestion.expected().stream()
                    .anyMatch(expectedSource -> answerResult.retrievedArticles().stream()
                            .anyMatch(article -> matches(expectedSource, article)));
            boolean citationHit = uncovered ? sources.isEmpty() : evalQuestion.expected().stream()
                    .anyMatch(expectedSource -> sources.stream()
                            .anyMatch(source -> matches(expectedSource, source)));
            return new EvalResult(evalQuestion.id(), evalQuestion.type(), group, expected,
                    answerResult.retrievedArticles().stream()
                            .map(article -> label(article.document(), article.number()))
                            .toList(),
                    sources.stream()
                            .map(source -> label(source.document(), source.section().replace(SECTION_PREFIX, "")))
                            .toList(),
                    retrievalHit, citationHit,
                    answerResult.promptTokens(), answerResult.completionTokens(), answerResult.costEur(), null);
        } catch (RuntimeException runtimeException) {
            // Only the exception class is recorded: provider error messages are not shown, to keep any request content out of files.
            log.error("Eval question {} failed with {}", evalQuestion.id(), runtimeException.getClass().getName());
            return new EvalResult(evalQuestion.id(), evalQuestion.type(), group, expected, List.of(), List.of(),
                    uncovered ? null : Boolean.FALSE, false, null, null, null,
                    runtimeException.getClass().getSimpleName());
        }
    }

    private String label(String document, Object number) {
        return document + " " + number;
    }

    private boolean matches(ExpectedSource expectedSource, Article article) {
        return expectedSource.document().equals(article.document()) && expectedSource.article() == article.number();
    }

    private boolean matches(ExpectedSource expectedSource, Source source) {
        return expectedSource.document().equals(source.document())
                && source.section().equals(SECTION_PREFIX + expectedSource.article());
    }

    private String buildReport(List<EvalResult> evalResults) {
        List<EvalResult> coveredResults = evalResults.stream()
                .filter(evalResult -> !evalResult.expected().isEmpty())
                .toList();
        long retrievalHits = coveredResults.stream().filter(evalResult -> Boolean.TRUE.equals(evalResult.retrievalHit())).count();
        long citationHits = evalResults.stream().filter(EvalResult::citationHit).count();

        Map<String, List<EvalResult>> resultsByType = evalResults.stream()
                .collect(Collectors.groupingBy(EvalResult::type, LinkedHashMap::new, Collectors.toList()));

        List<EvalResult> withUsage = evalResults.stream().filter(evalResult -> evalResult.costEur() != null).toList();
        int promptTokens = withUsage.stream().mapToInt(EvalResult::promptTokens).sum();
        int completionTokens = withUsage.stream().mapToInt(EvalResult::completionTokens).sum();
        BigDecimal totalCostEur = withUsage.stream().map(EvalResult::costEur).reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder report = new StringBuilder();
        report.append("# Eval report\n\n");
        report.append("- Date: ").append(LocalDate.now()).append('\n');
        report.append("- Chat model: ").append(chatModel).append("; embedding model: ").append(embeddingModel).append('\n');
        report.append("- Questions: ").append(evalResults.size()).append(" (")
                .append(resultsByType.entrySet().stream()
                        .map(entry -> entry.getValue().size() + " " + entry.getKey())
                        .collect(Collectors.joining(", ")))
                .append("). Question text is in eval/questions.json and is not repeated here.\n\n");

        report.append("## Scores\n\n");
        report.append("| Score | Result |\n|---|---|\n");
        report.append("| Retrieval: an expected article is among the 4 retrieved (covered questions only) | **")
                .append(retrievalHits).append('/').append(coveredResults.size()).append("** |\n");
        report.append("| Citation: an expected article is in `sources`; for uncovered questions, `sources` is empty | **")
                .append(citationHits).append('/').append(evalResults.size()).append("** |\n\n");

        report.append("## Scores by document\n\n");
        report.append("| Group | Questions | Retrieval | Citation |\n|---|---|---|---|\n");
        for (Map.Entry<String, List<EvalResult>> entry : resultsByGroup(evalResults).entrySet()) {
            List<EvalResult> groupResults = entry.getValue();
            long groupRetrievalHits = groupResults.stream()
                    .filter(evalResult -> Boolean.TRUE.equals(evalResult.retrievalHit()))
                    .count();
            long groupCitationHits = groupResults.stream().filter(EvalResult::citationHit).count();
            report.append("| ").append(entry.getKey())
                    .append(" | ").append(groupResults.size())
                    .append(" | ").append(UNCOVERED_GROUP.equals(entry.getKey())
                            ? "n/a" : groupRetrievalHits + "/" + groupResults.size())
                    .append(" | ").append(groupCitationHits).append('/').append(groupResults.size())
                    .append(" |\n");
        }
        report.append('\n');

        report.append("## Citation score by question type\n\n");
        report.append("| Type | Citation hits |\n|---|---|\n");
        for (Map.Entry<String, List<EvalResult>> entry : resultsByType.entrySet()) {
            long typeHits = entry.getValue().stream().filter(EvalResult::citationHit).count();
            report.append("| ").append(entry.getKey()).append(" | ")
                    .append(typeHits).append('/').append(entry.getValue().size()).append(" |\n");
        }

        report.append("\n## Usage\n\n");
        report.append("- Chat tokens: ").append(promptTokens).append(" prompt, ")
                .append(completionTokens).append(" completion");
        int missingUsage = evalResults.size() - withUsage.size();
        if (missingUsage > 0) {
            report.append(" (").append(missingUsage).append(" question(s) without reported usage, not counted)");
        }
        report.append('\n');
        report.append("- Chat cost: EUR ").append(totalCostEur.toPlainString())
                .append(" (embedding calls are not included)\n\n");

        report.append("## Per question\n\n");
        report.append("| id | type | expected | retrieved | cited | retrieval | citation | tokens in/out | cost EUR | error |\n");
        report.append("|---|---|---|---|---|---|---|---|---|---|\n");
        for (EvalResult evalResult : evalResults) {
            report.append("| ").append(evalResult.id())
                    .append(" | ").append(evalResult.type())
                    .append(" | ").append(evalResult.expected().isEmpty() ? "none" : join(evalResult.expected()))
                    .append(" | ").append(join(evalResult.retrieved()))
                    .append(" | ").append(evalResult.cited().isEmpty() ? "none" : join(evalResult.cited()))
                    .append(" | ").append(evalResult.retrievalHit() == null ? "n/a" : hitOrMiss(evalResult.retrievalHit()))
                    .append(" | ").append(hitOrMiss(evalResult.citationHit()))
                    .append(" | ").append(evalResult.costEur() == null ? "unknown"
                            : evalResult.promptTokens() + "/" + evalResult.completionTokens())
                    .append(" | ").append(evalResult.costEur() == null ? "unknown" : evalResult.costEur().toPlainString())
                    .append(" | ").append(Objects.requireNonNullElse(evalResult.error(), ""))
                    .append(" |\n");
        }
        return report.toString();
    }

    /**
     * Groups results by expected document in order of first appearance, with "uncovered" last.
     */
    private Map<String, List<EvalResult>> resultsByGroup(List<EvalResult> evalResults) {
        Map<String, List<EvalResult>> resultsByGroup = evalResults.stream()
                .collect(Collectors.groupingBy(EvalResult::group, LinkedHashMap::new, Collectors.toList()));
        List<EvalResult> uncoveredResults = resultsByGroup.remove(UNCOVERED_GROUP);
        if (uncoveredResults != null) {
            resultsByGroup.put(UNCOVERED_GROUP, uncoveredResults);
        }
        return resultsByGroup;
    }

    private String join(List<?> values) {
        return values.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    private String hitOrMiss(boolean hit) {
        return hit ? "hit" : "miss";
    }
}
