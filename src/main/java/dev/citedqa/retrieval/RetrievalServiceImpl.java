package dev.citedqa.retrieval;

import java.util.List;

import dev.citedqa.corpus.Article;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RetrievalServiceImpl implements RetrievalService {

    private static final int TOP_K = 4;

    private final VectorStore vectorStore;

    public RetrievalServiceImpl(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public List<Article> search(String question) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(TOP_K)
                .build();
        return vectorStore.similaritySearch(searchRequest).stream()
                .map(this::toArticle)
                .toList();
    }

    /**
     * Number is read as Number, not Integer: after a save and load through JSON
     * the stored type is chosen by the JSON reader.
     */
    private Article toArticle(Document document) {
        Number number = (Number) document.getMetadata().get(VectorStoreConfig.METADATA_ARTICLE_NUMBER);
        return new Article(
                (String) document.getMetadata().get(VectorStoreConfig.METADATA_DOCUMENT),
                number.intValue(),
                (String) document.getMetadata().get(VectorStoreConfig.METADATA_TITLE),
                document.getText());
    }
}
