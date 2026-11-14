package dev.citedqa.corpus;

/**
 * One numbered article of a loaded document.
 *
 * @param document short name of the source document, for example "GDPR"
 * @param number   article number as printed in the document
 * @param title    article title, the line that follows the "Article N" heading
 * @param text     article body, without heading and title
 */
public record Article(String document, int number, String title, String text) {
}
