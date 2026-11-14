package dev.citedqa.eval;

import java.util.List;

/**
 * One entry of eval/questions.json. An empty expected list means the document
 * does not answer the question.
 */
public record EvalQuestion(int id, String type, String question, List<ExpectedSource> expected) {
}
