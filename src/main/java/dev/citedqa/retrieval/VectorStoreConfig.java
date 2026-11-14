package dev.citedqa.retrieval;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.citedqa.corpus.Article;
import dev.citedqa.corpus.CorpusLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates the vector store. On startup it loads data/vector-store.json if the file
 * exists; otherwise it embeds every article, saves the file, and logs the count.
 */
@Configuration
public class VectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

    static final String METADATA_DOCUMENT = "document";
    static final String METADATA_ARTICLE_NUMBER = "articleNumber";
    static final String METADATA_TITLE = "title";

    private static final File STORE_FILE = new File("data/vector-store.json");

    @Bean
    public SimpleVectorStore vectorStore(EmbeddingModel embeddingModel, CorpusLoader corpusLoader) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        if (STORE_FILE.exists()) {
            simpleVectorStore.load(STORE_FILE);
            log.info("Embedded 0 articles; loaded vector store from {}", STORE_FILE.getPath());
            return simpleVectorStore;
        }
        List<Document> documents = corpusLoader.load().stream()
                .map(this::toDocument)
                .toList();
        simpleVectorStore.add(documents);
        createParentDirectory();
        simpleVectorStore.save(STORE_FILE);
        log.info("Embedded {} articles; saved vector store to {}", documents.size(), STORE_FILE.getPath());
        return simpleVectorStore;
    }

    /**
     * The document text is the article body only. The title still reaches the
     * embedding model: Spring AI's Mistral embedding model formats each document
     * with MetadataMode.EMBED by default, which prepends the metadata to the text.
     * The id is derived from document and number, so it is the same on every rebuild.
     */
    private Document toDocument(Article article) {
        String id = UUID.nameUUIDFromBytes((article.document() + ":" + article.number())
                .getBytes(StandardCharsets.UTF_8)).toString();
        Map<String, Object> metadata = Map.of(
                METADATA_DOCUMENT, article.document(),
                METADATA_ARTICLE_NUMBER, article.number(),
                METADATA_TITLE, article.title());
        return new Document(id, article.text(), metadata);
    }

    private void createParentDirectory() {
        try {
            Files.createDirectories(STORE_FILE.toPath().toAbsolutePath().getParent());
        } catch (IOException ioException) {
            throw new UncheckedIOException("Cannot create directory for " + STORE_FILE.getPath(), ioException);
        }
    }
}
