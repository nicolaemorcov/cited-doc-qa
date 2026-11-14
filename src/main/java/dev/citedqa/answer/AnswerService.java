package dev.citedqa.answer;

/**
 * Answers a question from the retrieved articles and returns the cited sources.
 */
public interface AnswerService {

    AnswerResult answer(String question);
}
