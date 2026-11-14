package dev.citedqa.answer;

import dev.citedqa.web.AnswerResponse;

/**
 * Answers a question from the retrieved articles and returns the cited sources.
 */
public interface AnswerService {

    AnswerResponse answer(String question);
}
