package dev.citedqa.web;

import java.util.List;

public record AnswerResponse(String requestId, String answer, List<Source> sources) {
}
