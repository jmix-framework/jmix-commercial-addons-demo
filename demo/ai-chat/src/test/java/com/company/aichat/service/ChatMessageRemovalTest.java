package com.company.aichat.service;

import com.company.aichat.entity.ChatMessage;
import com.company.aichat.entity.MessageFeedback;
import com.company.aichat.test_support.AuthenticatedAsAdmin;
import io.jmix.aichat.entity.AiMessageRole;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Removing a rated answer — as regenerate and clear history do — removes its feedback too,
 * instead of failing on the foreign key.
 */
@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
public class ChatMessageRemovalTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    ChatService chatService;

    @AfterEach
    void tearDown() {
        chatService.clearHistory();
    }

    @Test
    void removingRatedAnswerRemovesFeedback() {
        MessageFeedback feedback = rateNewAnswer();

        dataManager.remove(feedback.getChatMessage());

        assertThat(dataManager.load(Id.of(feedback)).optional()).isEmpty();
    }

    @Test
    void clearHistoryRemovesRatedAnswers() {
        MessageFeedback feedback = rateNewAnswer();

        chatService.clearHistory();

        assertThat(dataManager.load(ChatMessage.class).all().list()).isEmpty();
        assertThat(dataManager.load(Id.of(feedback)).optional()).isEmpty();
    }

    private MessageFeedback rateNewAnswer() {
        ChatMessage answer = dataManager.create(ChatMessage.class);
        answer.setRole(AiMessageRole.ASSISTANT);
        answer.setContent("An answer");
        answer = dataManager.save(answer);

        MessageFeedback feedback = dataManager.create(MessageFeedback.class);
        feedback.setChatMessage(answer);
        feedback.setLiked(true);
        return dataManager.save(feedback);
    }
}
