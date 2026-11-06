package com.company.aichat.service;

import com.company.aichat.entity.ChatMessage;
import com.vaadin.flow.component.ai.common.AIAttachment;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * Streams the Claims Assistant's answers and manages the stored conversation. The chat always answers
 * from {@link PlaceholderChatModel}: canned, predictable replies for the demo, no provider needed.
 */
@Service
public class ChatService {

    /** Prompt that fails the stream asynchronously before any token is emitted. */
    private static final String PROMPT_FAIL = "fail";

    /** Prompt that emits two tokens and only then fails the stream. */
    private static final String PROMPT_FAIL_LATE = "fail late";

    /** Prompt that fails inside the provider call itself, before a {@code Flux} exists. */
    private static final String PROMPT_FAIL_SYNC = "fail sync";

    /** Prompt whose first token arrives late, leaving a window to stop the generation. */
    private static final String PROMPT_SLOW = "slow";

    /** Like {@link #PROMPT_FAIL}, but the failure lands ten seconds in, with still no token. */
    private static final String PROMPT_FAIL_SLOW = "fail slow";

    /** Like {@link #PROMPT_FAIL_LATE}, but the failure lands ten seconds after the tokens. */
    private static final String PROMPT_FAIL_LATE_SLOW = "fail late slow";

    /** Prompt answered instantly with a long text, to build a scrollable conversation. */
    private static final String PROMPT_LONG = "long";

    /** Prompt answered with a long, slowly paced stream, to act on the list mid-generation. */
    private static final String PROMPT_STREAM = "stream";

    /** Sent to the model when the user attaches files without typing anything. */
    private static final String ATTACHMENTS_ONLY_PROMPT = "Please assess the attached files.";

    private static final String LONG_ANSWER = """
            **Claim notes, October 2026** — a running log of the week's motor claims.

            Monday started with three new first notices of loss: two rear-end collisions at low speed \
            and one windshield crack from road debris. All three customers sent photos on the same day, \
            which made the initial assessment quick.

            On Tuesday the review of CLM-1042 continued. The police report confirms the other driver ran \
            a red light, so the claim is clearly covered and subrogation against the other insurer is \
            likely. The only open item is the repair estimate from Harbor Auto Body.

            Wednesday was spent on the hail claims from the storm on October 9. Most vehicles have roof \
            and hood damage below ten percent, so they can be settled with a cash payment without \
            waiting for a repair shop.

            Thursday brought a rejected claim: the policy had expired two weeks before the incident, and \
            the customer had not renewed it. The rejection letter cites the validity dates of the policy.

            On Friday two approved claims were paid. Both payments went directly to the repair shops, \
            which the customers preferred, and the claims were closed the same afternoon.
            """;

    private final ChatClient chatClient;
    private final DataManager dataManager;

    public ChatService(DataManager dataManager) {
        this.chatClient = ChatClient.create(new PlaceholderChatModel());
        this.dataManager = dataManager;
    }

    /**
     * Streams an assistant answer to a user message. Image attachments are passed to the model as
     * media, which makes the placeholder model answer with a damage assessment.
     *
     * @param userMessage user prompt text
     * @param attachments files attached to the message
     * @return streamed answer chunks
     */
    public Flux<String> stream(@Nullable String userMessage, List<AIAttachment> attachments) {
        String text = userMessage == null || userMessage.isBlank() ? ATTACHMENTS_ONLY_PROMPT : userMessage;
        Flux<String> scripted = scriptedResponse(text);
        if (scripted != null) {
            return scripted;
        }
        List<AIAttachment> images = attachments.stream()
                .filter(attachment -> attachment.mimeType() != null && attachment.mimeType().startsWith("image/"))
                .toList();
        return chatClient.prompt()
                .user(user -> {
                    user.text(text);
                    for (AIAttachment image : images) {
                        user.media(MimeType.valueOf(image.mimeType()), new ByteArrayResource(image.data()));
                    }
                })
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

    /**
     * Turns eight magic prompts into scripted provider behaviour, so the chat's error and
     * cancellation handling can be shown on any profile without breaking a provider on purpose:
     * <ul>
     *     <li>{@code fail} — the stream fails asynchronously, with no token emitted at all.</li>
     *     <li>{@code fail late} — two tokens arrive, then the stream fails.</li>
     *     <li>{@code fail sync} — the failure is thrown inside the provider call.</li>
     *     <li>{@code slow} — the first token arrives only after ten seconds.</li>
     *     <li>{@code long} — a long answer, emitted instantly, to build a scrollable conversation.</li>
     *     <li>{@code fail slow} / {@code fail late slow} — the two failure shapes, ten seconds later.</li>
     *     <li>{@code stream} — the long answer paced at three tokens a second for half a minute.</li>
     * </ul>
     * Matching is on the whole trimmed prompt, so ordinary prompts that merely mention these words
     * stream normally.
     *
     * @param userMessage the prompt as typed
     * @return the scripted stream, or {@code null} when the prompt is not a magic one
     */
    @Nullable
    private Flux<String> scriptedResponse(String userMessage) {
        String prompt = userMessage.trim().toLowerCase(Locale.ROOT);
        if (PROMPT_FAIL_LATE_SLOW.equals(prompt)) {
            return Flux.just("Checking the policy", " — one moment")
                    .delayElements(Duration.ofMillis(300))
                    .concatWith(Flux.<String>error(new RuntimeException("Claims system timeout (after two tokens)"))
                            .delaySubscription(Duration.ofSeconds(10)));
        }
        if (PROMPT_FAIL_SLOW.equals(prompt)) {
            return Flux.<String>error(new RuntimeException("Claims system timeout"))
                    .delaySubscription(Duration.ofSeconds(10));
        }
        if (PROMPT_FAIL_LATE.equals(prompt)) {
            return Flux.just("Checking the policy", " — one moment")
                    .delayElements(Duration.ofMillis(300))
                    .concatWith(Flux.error(new RuntimeException("Claims system unavailable (after two tokens)")));
        }
        if (PROMPT_FAIL.equals(prompt)) {
            return Flux.<String>error(new RuntimeException("Claims system unavailable"))
                    .delaySubscription(Duration.ofMillis(300));
        }
        if (PROMPT_FAIL_SYNC.equals(prompt)) {
            throw new RuntimeException("Claims system unavailable (thrown by the provider)");
        }
        if (PROMPT_STREAM.equals(prompt)) {
            return Flux.fromArray(LONG_ANSWER.split("(?<=\\s)"))
                    .delaySubscription(Duration.ofSeconds(1))
                    .delayElements(Duration.ofMillis(330));
        }
        if (PROMPT_LONG.equals(prompt)) {
            return Flux.fromArray(LONG_ANSWER.split("(?<=\\n\\n)"));
        }
        if (PROMPT_SLOW.equals(prompt)) {
            return Flux.just("Here", " is", " the", " slow", " answer.")
                    .delaySubscription(Duration.ofSeconds(10))
                    .delayElements(Duration.ofMillis(500));
        }
        return null;
    }
}
