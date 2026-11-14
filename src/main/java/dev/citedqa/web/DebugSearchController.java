package dev.citedqa.web;

import java.util.List;

import dev.citedqa.retrieval.RetrievalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Temporary endpoint for checking search results by hand. Removed in Step 6.
 */
@RestController
@RequestMapping("/api/debug")
public class DebugSearchController {

    private final RetrievalService retrievalService;

    public DebugSearchController(RetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<SearchHit>> search(@RequestParam("q") String question) {
        List<SearchHit> searchHits = retrievalService.search(question).stream()
                .map(article -> new SearchHit(article.number(), article.title()))
                .toList();
        return ResponseEntity.ok(searchHits);
    }
}
