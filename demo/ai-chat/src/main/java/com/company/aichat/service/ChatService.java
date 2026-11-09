package com.company.aichat.service;

import com.company.aichat.entity.ChatMessage;
import com.vaadin.flow.component.ai.common.AIAttachment;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Streams the assistant's answers and manages the stored conversation. The chat always answers
 * from {@link PlaceholderChatModel}: prepared, predictable replies for the demo, no provider needed.
 */
@Service
public class ChatService {

    /** Sent to the model when the user attaches files without typing anything. */
    private static final String ATTACHMENTS_ONLY_PROMPT = "Please look at the attached files.";

    private final ChatClient chatClient;
    private final DataManager dataManager;

    public ChatService(DataManager dataManager) {
        this.chatClient = ChatClient.create(new PlaceholderChatModel());
        this.dataManager = dataManager;
    }

    /**
     * Streams an assistant answer to a user message. Attachments are passed to the model as
     * media, which makes the placeholder model answer with a list of the files received.
     *
     * @param userMessage user prompt text
     * @param attachments files attached to the message
     * @return streamed answer chunks
     */
    public Flux<String> stream(@Nullable String userMessage, List<AIAttachment> attachments) {
        String text = userMessage == null || userMessage.isBlank() ? ATTACHMENTS_ONLY_PROMPT : userMessage;
        Media[] media = attachments.stream()
                .map(ChatService::toMedia)
                .toArray(Media[]::new);
        return chatClient.prompt()
                .user(user -> user.text(text).media(media))
                .stream()
                .content();
    }

    /**
     * Deletes the whole stored conversation, with the feedback given on its answers.
     */
    @Transactional
    public void clearHistory() {
        List<ChatMessage> messages = dataManager.load(ChatMessage.class)
                .all()
                .fetchPlan(FetchPlan.BASE)
                .list();
        dataManager.remove(messages);
    }

    private static Media toMedia(AIAttachment attachment) {
        MimeType mimeType = attachment.mimeType() != null
                ? MimeType.valueOf(attachment.mimeType())
                : MimeTypeUtils.APPLICATION_OCTET_STREAM;
        return Media.builder()
                .mimeType(mimeType)
                .data(attachment.data())
                .name(attachment.name())
                .build();
    }
}
