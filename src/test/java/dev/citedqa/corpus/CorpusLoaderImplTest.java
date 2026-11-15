package dev.citedqa.corpus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CorpusLoaderImplTest {

    private static final String START_MARKER = "HAVE ADOPTED THIS REGULATION:";
    private static final String END_MARKER_PREFIX = "This Regulation shall be binding in its entirety";
    private static final DocumentSource GDPR_SOURCE = new DocumentSource("GDPR", "corpus/gdpr-en.txt",
            START_MARKER, END_MARKER_PREFIX, "CHAPTER [IVXLC]+|Section \\d+");
    private static final DocumentSource AI_ACT_SOURCE = new DocumentSource("AI Act", "corpus/ai-act-en.txt",
            START_MARKER, END_MARKER_PREFIX, "CHAPTER [IVXLC]+|SECTION \\d+");

    private final CorpusLoader corpusLoader = new CorpusLoaderImpl(new CorpusProperties(List.of(GDPR_SOURCE)));
    private final CorpusLoader aiActCorpusLoader = new CorpusLoaderImpl(new CorpusProperties(List.of(AI_ACT_SOURCE)));

    @Test
    void shouldReturn99ArticlesNumbered1To99WithoutGaps() {
        // Given
        int expectedCount = 99;

        // When
        List<Article> articles = corpusLoader.load();

        // Then
        assertEquals(expectedCount, articles.size());
        for (int index = 0; index < articles.size(); index++) {
            assertEquals(index + 1, articles.get(index).number());
        }
    }

    @Test
    void shouldTitleArticle1SubjectMatterAndObjectives() {
        // Given
        int articleNumber = 1;

        // When
        Article article = findArticle(corpusLoader.load(), articleNumber);

        // Then
        assertEquals("Subject-matter and objectives", article.title());
    }

    @Test
    void shouldIncludeSeventyTwoHoursInArticle33Text() {
        // Given
        int articleNumber = 33;

        // When
        Article article = findArticle(corpusLoader.load(), articleNumber);

        // Then
        assertTrue(article.text().contains("72 hours"));
    }

    @Test
    void shouldTitleArticle99EntryIntoForceAndApplication() {
        // Given
        int articleNumber = 99;

        // When
        Article article = findArticle(corpusLoader.load(), articleNumber);

        // Then
        assertEquals("Entry into force and application", article.title());
    }

    @Test
    void shouldReturnNoArticleWithEmptyText() {
        // Given: the GDPR corpus file on the classpath

        // When
        List<Article> articles = corpusLoader.load();

        // Then
        assertFalse(articles.isEmpty());
        for (Article article : articles) {
            assertFalse(article.text().isBlank(), "Article " + article.number() + " has empty text");
        }
    }

    @Test
    void shouldReturn113AiActArticlesNumbered1To113WithoutGaps() {
        // Given
        int expectedCount = 113;

        // When
        List<Article> articles = aiActCorpusLoader.load();

        // Then
        assertEquals(expectedCount, articles.size());
        for (int index = 0; index < articles.size(); index++) {
            assertEquals(index + 1, articles.get(index).number());
            assertEquals("AI Act", articles.get(index).document());
        }
    }

    @Test
    void shouldTitleAiActArticle5ProhibitedAiPractices() {
        // Given
        int articleNumber = 5;

        // When
        Article article = findArticle(aiActCorpusLoader.load(), articleNumber);

        // Then
        assertEquals("Prohibited AI practices", article.title());
    }

    @Test
    void shouldTitleAiActArticle113EntryIntoForceAndApplication() {
        // Given
        int articleNumber = 113;

        // When
        Article article = findArticle(aiActCorpusLoader.load(), articleNumber);

        // Then
        assertEquals("Entry into force and application", article.title());
    }

    @Test
    void shouldReturnNoAiActArticleWithEmptyText() {
        // Given: the AI Act corpus file on the classpath

        // When
        List<Article> articles = aiActCorpusLoader.load();

        // Then
        assertFalse(articles.isEmpty());
        for (Article article : articles) {
            assertFalse(article.text().isBlank(), "AI Act Article " + article.number() + " has empty text");
        }
    }

    private Article findArticle(List<Article> articles, int articleNumber) {
        return articles.stream()
                .filter(article -> article.number() == articleNumber)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Article " + articleNumber + " not found"));
    }
}
