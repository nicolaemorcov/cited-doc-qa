package dev.citedqa.web;

/**
 * One result of the temporary debug search endpoint.
 */
public record SearchHit(int articleNumber, String title) {
}
