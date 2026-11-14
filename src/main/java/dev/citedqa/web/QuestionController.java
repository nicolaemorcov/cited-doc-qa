package dev.citedqa.web;

import dev.citedqa.answer.AnswerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final AnswerService answerService;

    public QuestionController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping
    public ResponseEntity<AnswerResponse> ask(@Valid @RequestBody QuestionRequest questionRequest) {
        return ResponseEntity.ok(answerService.answer(questionRequest.question()).response());
    }
}
