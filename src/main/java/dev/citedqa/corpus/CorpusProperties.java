package dev.citedqa.corpus;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The documents to load, bound from citedqa.corpus.documents in application.yml.
 * Adding a document means a text file plus one entry here, not new code.
 */
@ConfigurationProperties("citedqa.corpus")
public record CorpusProperties(List<DocumentSource> documents) {
}
