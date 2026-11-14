package dev.citedqa.retrieval;

import java.util.List;

import dev.citedqa.corpus.Article;

/**
 * Finds the articles most related to a question.
 */
public interface RetrievalService {

    /**
     * @return the 4 most related articles, most related first
     */
    List<Article> search(String question);
}
