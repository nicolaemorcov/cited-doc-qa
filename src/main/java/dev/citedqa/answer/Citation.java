package dev.citedqa.answer;

/**
 * One source the chat model says it used: a document name and an article number.
 * Both parts are needed because article numbers repeat across documents.
 */
public record Citation(String document, int article) {
}
