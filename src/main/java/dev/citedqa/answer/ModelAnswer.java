package dev.citedqa.answer;

import java.util.List;

/**
 * The structured reply the chat model must return: the answer text and the
 * document and article of each source it says it used. The sources are checked before use.
 */
public record ModelAnswer(String answer, List<Citation> sources) {
}
