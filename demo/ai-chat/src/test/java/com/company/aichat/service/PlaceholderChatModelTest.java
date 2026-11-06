package com.company.aichat.service;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaceholderChatModelTest {

    private final PlaceholderChatModel model = new PlaceholderChatModel(Duration.ZERO, Duration.ZERO);

    @Test
    void picksAnswerByKeyword() {
        assertThat(answer("Summarize the weekly status")).contains("Website redesign — weekly summary");
        assertThat(answer("Write an EMAIL to the team")).contains("**Subject:**");
        assertThat(answer("Show me some code")).contains("```java");
        assertThat(answer("Write a long guide")).contains("# How to write a good prompt");
    }

    @Test
    void matchesKeywordsAtWordStartOnly() {
        // "along" and "decode" contain the keywords, but not at the start of a word
        assertThat(answer("Walk along the river and decode the map")).contains("placeholder model");
    }

    @Test
    void answersWithKeywordListByDefault() {
        String answer = answer("Hello");
        assertThat(answer).contains("`summary`", "`email`", "`code`", "`long`", "`error`", "`fail`");
    }

    @Test
    void streamsTheSameTextAsCall() {
        String streamed = stream(new Prompt("Show me some code"));
        assertThat(streamed).isEqualTo(model.call(new Prompt("Show me some code")).getResult().getOutput().getText());
    }

    @Test
    void failsBeforeFirstToken() {
        Flux<ChatResponse> stream = model.stream(new Prompt("Make it fail"));
        assertThat(stream.onErrorResume(e -> Flux.empty()).collectList().block()).isEmpty();
        assertThatThrownBy(stream::blockLast).hasMessageContaining("before the first token");
    }

    @Test
    void failsAfterPartialAnswer() {
        Flux<ChatResponse> stream = model.stream(new Prompt("Show an error"));
        assertThat(stream.onErrorResume(e -> Flux.empty()).collectList().block()).isNotEmpty();
        assertThatThrownBy(stream::blockLast).hasMessageContaining("middle of an answer");
    }

    @Test
    void listsAttachments() {
        Media file = Media.builder()
                .mimeType(MimeTypeUtils.TEXT_PLAIN)
                .data(new byte[2048])
                .name("notes|draft.txt")
                .build();
        UserMessage message = UserMessage.builder()
                .text("fail")
                .media(file)
                .build();

        String answer = stream(new Prompt(List.of(message)));

        // Attachments win over the failure keywords, and the pipe is escaped for the table
        assertThat(answer).contains("Attachments received", "notes\\|draft.txt", "text/plain", "2.0 KB");
    }

    private String answer(String prompt) {
        return stream(new Prompt(prompt));
    }

    private String stream(Prompt prompt) {
        return model.stream(prompt)
                .map(response -> response.getResult().getOutput().getText())
                .collect(Collectors.joining())
                .block();
    }
}
