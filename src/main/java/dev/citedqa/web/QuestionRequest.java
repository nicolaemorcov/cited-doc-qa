package dev.citedqa.web;

import jakarta.validation.constraints.NotBlank;

public record QuestionRequest(@NotBlank String question) {
}
