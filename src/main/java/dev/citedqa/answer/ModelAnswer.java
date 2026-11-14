package dev.citedqa.answer;

import java.util.List;

/**
 * The structured reply the chat model must return: the answer text and the
 * numbers of the articles it says it used. The numbers are checked before use.
 */
public record ModelAnswer(String answer, List<Integer> articles) {
}
