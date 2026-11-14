package dev.citedqa.corpus;

import java.util.List;

/**
 * Loads a document from the classpath and splits it into articles.
 */
public interface CorpusLoader {

    /**
     * @return the articles of the document, in document order
     */
    List<Article> load();
}
