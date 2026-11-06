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
import io.jmix.flowui.component.sidepanellayout.SidePanelLayout;
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
     * would flash past. Must outlast {@link #publishThinkingSteps(int)}.
     */
    private static final Duration THINKING_STEPS_WINDOW = Duration.ofMillis(3500);

    @ViewComponent
    private AiChat<ChatMessage> chat;
    @ViewComponent
    private CollectionLoader<ChatMessage> chatMessagesDl;
    @ViewComponent
    private SidePanelLayout helpPanelLayout;
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
     * Every answer plays the thinking steps first and then streams token by token.
     */
    @Install(to = "chat", subject = "llmProvider")
    private Flux<String> chatLlmProvider(final LLMProvider.LLMRequest request) {
        List<AIAttachment> attachments = request.attachments() != null ? request.attachments() : List.of();
        Flux<String> answer = chatService.stream(request.userMessage(), attachments);
        publishThinkingSteps(attachments.size());
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
        chat.prompt(messageBundle.getMessage("summaryPrompt"));
    }

    @Subscribe("suggestEmailButton")
    public void onSuggestEmailButtonClick(final ClickEvent<Button> event) {
        chat.prompt(messageBundle.getMessage("emailPrompt"));
    }

    @Subscribe("suggestCodeButton")
    public void onSuggestCodeButtonClick(final ClickEvent<Button> event) {
        chat.prompt(messageBundle.getMessage("codePrompt"));
    }

    @Subscribe("helpButton")
    public void onHelpButtonClick(final ClickEvent<Button> event) {
        helpPanelLayout.toggleSidePanel();
    }

    @Subscribe("trySummaryButton")
    public void onTrySummaryButtonClick(final ClickEvent<Button> event) {
        chat.prompt(messageBundle.getMessage("summaryPrompt"));
    }

    @Subscribe("tryCodeButton")
    public void onTryCodeButtonClick(final ClickEvent<Button> event) {
        chat.prompt(messageBundle.getMessage("codePrompt"));
    }

    @Subscribe("tryEmailButton")
    public void onTryEmailButtonClick(final ClickEvent<Button> event) {
        chat.prompt(messageBundle.getMessage("emailPrompt"));
    }

    @Subscribe("tryLongButton")
    public void onTryLongButtonClick(final ClickEvent<Button> event) {
        chat.prompt(messageBundle.getMessage("longPrompt"));
    }

    @Subscribe("tryErrorButton")
    public void onTryErrorButtonClick(final ClickEvent<Button> event) {
        chat.prompt(messageBundle.getMessage("errorPrompt"));
    }

    @Subscribe("tryFailButton")
    public void onTryFailButtonClick(final ClickEvent<Button> event) {
        chat.prompt(messageBundle.getMessage("failPrompt"));
    }

    /**
     * Publishes a scripted tool-progress feed from a background thread, with no {@code ui.access}
     * by this caller — the publisher owns that. Two statuses overlap, one completes out of order,
     * and one reports progress through {@code update()}. The whole script fits inside
     * {@link #THINKING_STEPS_WINDOW}.
     *
     * @param attachmentCount number of files attached to the message
     */
    private void publishThinkingSteps(int attachmentCount) {
        ThinkingStatusPublisher statuses = chat.getThinkingStatusPublisher();
        Thread worker = new Thread(() -> {
            try {
                if (attachmentCount > 0) {
                    ThinkingStatusItem upload = statuses.start("Receiving the files");
                    Thread.sleep(700);
                    ThinkingStatusItem reading = statuses.start("Reading file 1 of " + attachmentCount);
                    Thread.sleep(500);
                    upload.complete(attachmentCount == 1 ? "1 file" : attachmentCount + " files");
                    for (int i = 2; i <= attachmentCount; i++) {
                        Thread.sleep(500);
                        reading.update("Reading file " + i + " of " + attachmentCount);
                    }
                    Thread.sleep(500);
                    reading.complete();
                    ThinkingStatusItem listing = statuses.start("Preparing the file list");
                    Thread.sleep(600);
                    listing.complete();
                } else {
                    ThinkingStatusItem request = statuses.start("Understanding the request");
                    Thread.sleep(500);
                    ThinkingStatusItem search = statuses.start("Searching the knowledge base");
                    Thread.sleep(800);
                    search.complete("3 sources");
                    Thread.sleep(400);
                    request.complete();
                    ThinkingStatusItem reading = statuses.start("Reading source 1 of 3");
                    for (int i = 2; i <= 3; i++) {
                        Thread.sleep(500);
                        reading.update("Reading source " + i + " of 3");
                    }
                    Thread.sleep(400);
                    reading.complete("3 sources");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "thinking-steps");
        worker.setDaemon(true);
        worker.start();
    }
}
