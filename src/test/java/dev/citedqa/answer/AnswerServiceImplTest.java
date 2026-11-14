package dev.citedqa.answer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import dev.citedqa.corpus.Article;
import dev.citedqa.cost.CostCalculator;
import dev.citedqa.cost.CostCalculatorImpl;
import dev.citedqa.retrieval.RetrievalService;
import dev.citedqa.web.AnswerResponse;
import dev.citedqa.web.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mistralai.MistralAiChatOptions;

@ExtendWith(MockitoExtension.class)
class AnswerServiceImplTest {

    private static final Article ARTICLE_33 = new Article("GDPR", 33,
            "Notification of a personal data breach to the supervisory authority", "body 33");
    private static final Article ARTICLE_34 = new Article("GDPR", 34,
            "Communication of a personal data breach to the data subject", "body 34");

    @Mock
    private RetrievalService retrievalService;

    @Mock
    private ChatModel chatModel;

    private AnswerService answerService;

    @BeforeEach
    void setUp() {
        CostCalculator costCalculator = new CostCalculatorImpl(new BigDecimal("0.13"), new BigDecimal("0.51"));
        answerService = new AnswerServiceImpl(retrievalService, ChatClient.builder(chatModel), costCalculator);
    }

    @Test
    void shouldDropCitedArticleThatWasNotRetrieved() {
        // Given
        when(retrievalService.search(anyString())).thenReturn(List.of(ARTICLE_33, ARTICLE_34));
        givenModelReplies("{\"answer\": \"Within 72 hours.\", \"articles\": [33, 32]}");

        // When
        AnswerResponse answerResponse = answerService.answer("test question").response();

        // Then
        assertEquals(1, answerResponse.sources().size());
        assertEquals("Article 33", answerResponse.sources().get(0).section());
    }

    @Test
    void shouldReturnNotCoveredWithEmptySourcesWhenNoCitedArticleWasRetrieved() {
        // Given
        when(retrievalService.search(anyString())).thenReturn(List.of(ARTICLE_33, ARTICLE_34));
        givenModelReplies("{\"answer\": \"Paris.\", \"articles\": [99]}");

        // When
        AnswerResponse answerResponse = answerService.answer("test question").response();

        // Then
        assertEquals(AnswerServiceImpl.NOT_COVERED_ANSWER, answerResponse.answer());
        assertTrue(answerResponse.sources().isEmpty());
    }

    @Test
    void shouldMapValidCitationToSourceAndKeepModelAnswer() {
        // Given
        when(retrievalService.search(anyString())).thenReturn(List.of(ARTICLE_33, ARTICLE_34));
        givenModelReplies("{\"answer\": \"Within 72 hours.\", \"articles\": [33]}");

        // When
        AnswerResponse answerResponse = answerService.answer("test question").response();

        // Then
        assertEquals("Within 72 hours.", answerResponse.answer());
        assertEquals(List.of(new Source("GDPR", "Article 33",
                "Notification of a personal data breach to the supervisory authority")), answerResponse.sources());
    }

    private void givenModelReplies(String json) {
        ChatResponse chatResponse = new ChatResponse(List.of(new Generation(new AssistantMessage(json))));
        when(chatModel.getOptions()).thenReturn(MistralAiChatOptions.builder().build());
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
    }
}
