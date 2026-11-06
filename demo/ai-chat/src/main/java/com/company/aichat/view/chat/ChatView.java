package com.company.aichat.view.chat;


import com.company.aichat.entity.ChatMessage;
import com.company.aichat.entity.MessageFeedback;
import com.company.aichat.service.ChatService;
import com.company.aichat.view.main.MainView;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import io.jmix.aichat.action.AiMessageActionPerformedEvent;
import io.jmix.aichat.component.chat.AiChat;
import io.jmix.aichat.component.chat.AttachmentClickEvent;
import io.jmix.aichat.component.chat.AttachmentRejectedEvent;
import io.jmix.aichat.component.chat.GenerationFailedEvent;
import io.jmix.aichat.component.messagelist.ThinkingStatusItem;
import io.jmix.aichat.component.messagelist.ThinkingStatusPublisher;
import io.jmix.aichat.data.AiChatMessageContext;
import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.DialogOutcome;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Route(value = "chat", layout = MainView.class)
@ViewController(id = "ChatView")
@ViewDescriptor(path = "chat-view.xml")
public class ChatView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(ChatView.class);

    /**
     * How long the answer waits while the thinking steps run. The feed is wiped when the turn
     * ends and the thinking region hides on the first token, so without this window the statuses
     * would flash past. Must outlast {@link #publishThinkingSteps(boolean)}.
     */
    private static final Duration THINKING_STEPS_WINDOW = Duration.ofMillis(3500);

    @ViewComponent
    private AiChat<ChatMessage> chat;
    @ViewComponent
    private CollectionLoader<ChatMessage> chatMessagesDl;
    @ViewComponent
    private MessageBundle messageBundle;

    @Autowired
    private ChatService chatService;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private FileStorageLocator fileStorageLocator;
    @Autowired
    private Downloader downloader;

    /**
     * Streaming answers play the thinking steps first and then stream token by token. Instant
     * answers skip both, to fill the conversation quickly.
     */
    @Install(to = "chat", subject = "llmProvider")
    private Flux<String> chatLlmProvider(final LLMProvider.LLMRequest request) {
        List<AIAttachment> attachments = request.attachments() != null ? request.attachments() : List.of();
        Flux<String> answer = chatService.stream(request.userMessage(), attachments);
        publishThinkingSteps(!attachments.isEmpty());
        return answer.delaySubscription(THINKING_STEPS_WINDOW);
    }

    @Install(to = "chat", subject = "messageFactory")
    private ChatMessage chatMessageFactory(final AiChatMessageContext context) {
        ChatMessage chatMessage = dataManager.create(ChatMessage.class);
        chatMessage.setRole(context.getRole());
        chatMessage.setContent(context.getContent());
        chatMessage.setAttachments(saveAttachments(context.getAttachments()));
        return chatMessage;
    }

    @Subscribe("chat.clearHistory")
    public void onChatClearHistory(final ActionPerformedEvent event) {
        chatService.clearHistory();
        chatMessagesDl.load();
        notifications.create(messageBundle.getMessage("historyCleared"))
                .withPosition(Notification.Position.BOTTOM_END)
                .show();
    }

    @Subscribe("chat.feedbackUp")
    public void onChatFeedbackUp(final AiMessageActionPerformedEvent event) {
        promptFeedback(event, true);
    }

    @Subscribe("chat.feedbackDown")
    public void onChatFeedbackDown(final AiMessageActionPerformedEvent event) {
        promptFeedback(event, false);
    }

    /**
     * Asks for a comment and stores the verdict against the clicked answer. A {@code null}
     * message means the bubble has no row behind it — the transient one a stopped or failed
     * generation leaves — and there is nothing to attach feedback to.
     */
    private void promptFeedback(AiMessageActionPerformedEvent event, boolean liked) {
        ChatMessage message = event.getMessage();
        if (message == null) {
            notifications.create(messageBundle.getMessage("noAnswerToRate"))
                    .withPosition(Notification.Position.BOTTOM_END)
                    .show();
            return;
        }
        dialogs.createInputDialog(this)
                .withHeader(messageBundle.getMessage(liked ? "feedbackUp.header" : "feedbackDown.header"))
                .withParameters(InputParameter.stringParameter("comment")
                        .withLabel(messageBundle.getMessage("feedbackComment"))
                        .withRequired(false))
                .withActions(DialogActions.OK_CANCEL)
                .withCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(DialogOutcome.OK)) {
                        saveFeedback(message, liked, closeEvent.getValue("comment"));
                    }
                })
                .open();
    }

    private void saveFeedback(ChatMessage message, boolean liked, @Nullable String comment) {
        MessageFeedback feedback = dataManager.create(MessageFeedback.class);
        feedback.setChatMessage(message);
        feedback.setLiked(liked);
        feedback.setText(comment);
        MessageFeedback saved = dataManager.save(feedback);
        log.info("feedback stored: id={} liked={} messageId={}", saved.getId(), liked, message.getId());
        notifications.create(messageBundle.getMessage(liked ? "feedbackUp.saved" : "feedbackDown.saved"))
                .withType(Notifications.Type.SUCCESS)
                .withPosition(Notification.Position.BOTTOM_END)
                .show();
    }

    private List<FileRef> saveAttachments(List<AIAttachment> attachments) {
        List<FileRef> refs = new ArrayList<>();
        if (!attachments.isEmpty()) {
            FileStorage storage = fileStorageLocator.getDefault();
            for (AIAttachment attachment : attachments) {
                refs.add(storage.saveStream(attachment.name(), new ByteArrayInputStream(attachment.data())));
            }
        }
        return refs;
    }

    @Subscribe("chat")
    public void onChatAttachmentClick(final AttachmentClickEvent<?> event) {
        FileRef fileRef = event.getFileRef();
        if (fileRef != null) {
            downloader.download(fileRef);
        }
    }

    @Subscribe("chat")
    public void onChatAttachmentRejected(final AttachmentRejectedEvent<?> event) {
        notifications.create(messageBundle.getMessage("attachmentRejected"),
                        event.getFileName() + " — " + event.getReason())
                .withPosition(Notification.Position.BOTTOM_END)
                .show();
    }

    @Subscribe("chat")
    public void onChatGenerationFailed(final GenerationFailedEvent<?> event) {
        log.info("generation failed: error={} partialLength={}",
                event.getError().getMessage(), event.getPartialText().length());
    }

    @Subscribe("suggestSummaryButton")
    public void onSuggestSummaryButtonClick(final ClickEvent<Button> event) {
        chat.prompt(messageBundle.getMessage("suggestSummaryButton.prompt"));
    }

    @Subscribe("suggestLetterButton")
    public void onSuggestLetterButtonClick(final ClickEvent<Button> event) {
        chat.prompt(messageBundle.getMessage("suggestLetterButton.prompt"));
    }

    @Subscribe("suggestCodeButton")
    public void onSuggestCodeButtonClick(final ClickEvent<Button> event) {
        chat.prompt(messageBundle.getMessage("suggestCodeButton.prompt"));
    }

    /**
     * Publishes a scripted tool-progress feed from a background thread, with no {@code ui.access}
     * by this caller — the publisher owns that. Two statuses overlap, one completes out of order,
     * and one reports progress through {@code update()}. The whole script fits inside
     * {@link #THINKING_STEPS_WINDOW}.
     */
    private void publishThinkingSteps(boolean withAttachments) {
        ThinkingStatusPublisher statuses = chat.getThinkingStatusPublisher();
        Thread worker = new Thread(() -> {
            try {
                if (withAttachments) {
                    ThinkingStatusItem photos = statuses.start("Analyzing photo 1 of 1");
                    Thread.sleep(900);
                    ThinkingStatusItem zones = statuses.start("Matching damaged zones");
                    Thread.sleep(800);
                    photos.complete("1 photo");
                    Thread.sleep(500);
                    zones.complete("5 zones checked");
                    ThinkingStatusItem estimate = statuses.start("Estimating repair cost");
                    Thread.sleep(800);
                    estimate.complete();
                } else {
                    ThinkingStatusItem lookup = statuses.start("Looking up claim CLM-1042");
                    Thread.sleep(500);
                    ThinkingStatusItem policy = statuses.start("Checking policy coverage");
                    Thread.sleep(800);
                    policy.complete("Comprehensive");
                    Thread.sleep(400);
                    lookup.complete();
                    ThinkingStatusItem reading = statuses.start("Reading document 1 of 4");
                    for (int i = 2; i <= 4; i++) {
                        Thread.sleep(400);
                        reading.update("Reading document " + i + " of 4");
                    }
                    reading.complete("4 documents");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "thinking-steps");
        worker.setDaemon(true);
        worker.start();
    }
}
