package com.company.aichat.service;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local Spring AI chat model that answers without contacting any provider. It picks a prepared
 * Markdown answer by keywords in the user prompt, to show streaming, tables, code blocks, long
 * answers, attachments and the error state.
 *
 * <p>Keywords are case-insensitive and match at the start of any word, so {@code summary} also
 * matches "summarize" and {@code code} matches "codes". The first match wins:
 * <ul>
 *     <li>a message with attachments — a list of the files received;</li>
 *     <li>{@code fail} — the stream fails before the first token;</li>
 *     <li>{@code error} — a partial answer, then a failure;</li>
 *     <li>{@code code} — Java and SQL code blocks;</li>
 *     <li>{@code email} — a long email, slow enough to press Stop;</li>
 *     <li>{@code summary} — a project summary with a Markdown table;</li>
 *     <li>{@code long} — a long article, to build a scrollable conversation;</li>
 *     <li>anything else — the default answer listing the keywords.</li>
 * </ul>
 * The keyword badges and the help panel of the chat view list the same keywords.
 */
public class PlaceholderChatModel implements ChatModel {

    private static final Pattern KEYWORD_FAIL = keyword("fail");
    private static final Pattern KEYWORD_ERROR = keyword("error");
    private static final Pattern KEYWORD_CODE = keyword("code");
    private static final Pattern KEYWORD_EMAIL = keyword("e-?mail");
    private static final Pattern KEYWORD_SUMMARY = keyword("summar");
    private static final Pattern KEYWORD_LONG = keyword("long");

    private static final String ANSWER_DEFAULT = """
            Hi! I'm a **placeholder model**: I don't call any AI provider, I answer with prepared \
            replies picked by keywords in your message. Try one of these:

            | Keyword | What you get |
            |---------|--------------|
            | `summary` | A short project summary with a table |
            | `email` | A long email that streams slowly — press **Stop** midway |
            | `code` | Code blocks with syntax highlighting and a copy button |
            | `long` | A long article to scroll through |
            | `error` | An answer that breaks off with an error, then **Retry** |
            | `fail` | A failure before the first word arrives |

            You can also **attach a file** from the **+** menu or drop it onto the chat — I'll list \
            what I received. Open the **?** panel next to the send button for the full tour.
            """;

    private static final String ANSWER_SUMMARY = """
            ### Website redesign — weekly summary

            The team finished the new navigation and the product pages this week. The launch is \
            planned for **November 20**, one week later than first scheduled.

            | Workstream | Owner | Progress | Status |
            |------------|-------|----------|--------|
            | Visual design | Anna | 100% | Done |
            | Navigation and layout | Ben | 90% | In review |
            | Product pages | Chloe | 75% | On track |
            | Blog migration | David | 40% | **At risk** |
            | Accessibility audit | Emma | 0% | Starts Monday |

            **Main risk:** about 300 old blog posts still need manual fixes after the import. \
            Ben will help David next week, so the migration can still finish before the launch.

            > Next review: Thursday at 10:00, with the first preview of the home page.
            """;

    private static final String ANSWER_EMAIL = """
            **Subject:** New launch date for the website — November 20

            Hi everyone,

            Thank you all for the great work over the last few weeks. The new navigation looks \
            fantastic, and the first feedback from the pilot users has been very positive.

            After Thursday's review we decided to move the launch of the new website from \
            **November 13** to **November 20**. Here is why, and what it means for each of you.

            **Why we're moving the date**

            The blog migration turned out to be bigger than we expected. About 300 older posts use \
            a layout the new site doesn't support, and fixing them by hand takes time. We'd rather \
            launch a week later than send visitors to broken pages.

            **What changes**

            - **Design** — no changes, the visual design is final.
            - **Development** — Ben joins David on the blog migration for the next week.
            - **Content** — Chloe finishes the product pages as planned, then helps with proofreading.
            - **Quality** — Emma starts the accessibility audit on Monday instead of Wednesday.

            **The new plan**

            1. November 10–14: finish the blog migration and the product pages.
            2. November 17: content freeze; only bug fixes after this point.
            3. November 18–19: final testing on phones, tablets and desktop browsers.
            4. November 20: launch in the morning, with the whole team on call until noon.

            If anything in this plan doesn't work for you, please tell me by Friday, so we can \
            adjust it while there's still time. And if you have ideas for making the migration \
            faster, I'd love to hear them — every hour we save is an hour of testing we gain.

            Thanks again for all the effort. We're close, and it's going to be a great launch.

            Best regards,

            **Maria**
            Project Manager
            """;

    private static final String ANSWER_CODE = """
            Here is a small Java service that counts tasks by status:

            ```java
            public record Task(String title, Status status, LocalDate dueDate) {
            }

            @Service
            public class TaskStatistics {

                public Map<Status, Long> countByStatus(List<Task> tasks) {
                    return tasks.stream()
                            .collect(Collectors.groupingBy(Task::status,
                                    TreeMap::new, Collectors.counting()));
                }

                public List<Task> overdue(List<Task> tasks, LocalDate today) {
                    return tasks.stream()
                            .filter(task -> task.status() != Status.DONE)
                            .filter(task -> task.dueDate().isBefore(today))
                            .sorted(Comparator.comparing(Task::dueDate))
                            .toList();
                }
            }
            ```

            The same statistics as a SQL query over the `TASK` table:

            ```sql
            select status, count(*) as total
            from task
            where due_date < current_date
              and status <> 'DONE'
            group by status
            order by total desc
            ```

            Each code block has its own **copy** button, and the language is highlighted.
            """;

    private static final String ANSWER_LONG = """
            # How to write a good prompt

            A language model only knows what you tell it. This guide shows how to ask so that the \
            first answer is already close to what you need.

            ## 1. Start with the goal

            Say what you want to achieve before you say what to do. "Write an email" leaves the \
            model guessing; "Write an email that asks the team to move the launch by one week" \
            gives it a direction. One clear sentence about the goal saves several rounds of edits.

            ## 2. Give the context

            The model doesn't know your project, your audience or your constraints. Mention the \
            ones that matter: who will read the answer, what they already know, and what must not \
            change. If you have a document or a table, attach it instead of describing it.

            ## 3. Describe the format

            Ask for the shape of the answer explicitly:

            | You want | Ask for |
            |----------|---------|
            | A quick overview | "Three bullet points, one line each" |
            | A comparison | "A table with one row per option" |
            | Something to paste | "Only the code, no explanation" |
            | A draft to edit | "A short email, friendly but formal" |

            ## 4. Show an example

            One good example is worth a paragraph of rules. If you need a specific tone or \
            structure, paste a sample and ask the model to follow it.

            ## 5. Split big tasks

            A long task in one prompt tends to produce a long, vague answer. Ask for an outline \
            first, check it, and then ask for each part separately. You stay in control of the \
            direction, and each answer is easier to review.

            ## 6. Iterate

            Treat the first answer as a draft. Point out what to keep and what to change: "keep the \
            structure, make it shorter, and drop the second paragraph". Small, specific corrections \
            work better than starting over.

            ## 7. Check the facts

            Models can sound confident and still be wrong. Verify numbers, names and dates before \
            you use the answer, especially when it goes to a customer.

            ## Summary

            State the goal, give the context, describe the format, show an example, split big \
            tasks, iterate, and check the facts. Prompts written this way get useful answers on \
            the first try much more often.
            """;

    private static final String ANSWER_PARTIAL = "Let me look into that. First, I'll check ";

    /** A word (or whitespace run) plus its trailing whitespace. */
    private static final Pattern TOKEN = Pattern.compile("\\S+\\s*|\\s+");
    private static final int MAX_TOKEN_LENGTH = 8;

    private final Duration subscriptionDelay;
    private final Duration tokenDelay;

    public PlaceholderChatModel() {
        this(Duration.ofSeconds(1), Duration.ofMillis(60));
    }

    PlaceholderChatModel(Duration subscriptionDelay, Duration tokenDelay) {
        this.subscriptionDelay = subscriptionDelay;
        this.tokenDelay = tokenDelay;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return response(answerFor(prompt));
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        if (media(prompt).isEmpty()) {
            String text = promptText(prompt);
            if (KEYWORD_FAIL.matcher(text).find()) {
                // Fail before any token — the error banner shows over an empty answer.
                return Flux.<ChatResponse>error(new RuntimeException("Simulated failure before the first token"))
                        .delaySubscription(subscriptionDelay);
            }
            if (KEYWORD_ERROR.matcher(text).find()) {
                // Emit a partial answer, then fail — the partial text stays above the error banner.
                return emit(ANSWER_PARTIAL)
                        .concatWith(Flux.error(new RuntimeException("Simulated failure in the middle of an answer")));
            }
        }
        return emit(answerFor(prompt));
    }

    private Flux<ChatResponse> emit(String answer) {
        return Flux.fromIterable(tokenize(answer))
                .delaySubscription(subscriptionDelay)
                .delayElements(tokenDelay)
                .map(this::response);
    }

    /**
     * Splits the answer into small LLM-like tokens whose concatenation is exactly the original
     * text. Cutting after each whitespace run and capping the token length makes code stream as
     * evenly as prose.
     */
    private static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(text);
        while (matcher.find()) {
            String piece = matcher.group();
            for (int i = 0; i < piece.length(); i += MAX_TOKEN_LENGTH) {
                tokens.add(piece.substring(i, Math.min(piece.length(), i + MAX_TOKEN_LENGTH)));
            }
        }
        return tokens;
    }

    private String answerFor(Prompt prompt) {
        List<Media> media = media(prompt);
        if (!media.isEmpty()) {
            return attachmentsAnswer(media);
        }
        String text = promptText(prompt);
        if (KEYWORD_CODE.matcher(text).find()) {
            return ANSWER_CODE;
        }
        if (KEYWORD_EMAIL.matcher(text).find()) {
            return ANSWER_EMAIL;
        }
        if (KEYWORD_SUMMARY.matcher(text).find()) {
            return ANSWER_SUMMARY;
        }
        if (KEYWORD_LONG.matcher(text).find()) {
            return ANSWER_LONG;
        }
        return ANSWER_DEFAULT;
    }

    /**
     * Lists the attached files. The placeholder doesn't read their contents, so it says so
     * instead of pretending.
     */
    private static String attachmentsAnswer(List<Media> media) {
        StringBuilder answer = new StringBuilder("""
                ### Attachments received

                | # | File | Type | Size |
                |---|------|------|------|
                """);
        for (int i = 0; i < media.size(); i++) {
            Media file = media.get(i);
            answer.append("| ").append(i + 1)
                    .append(" | ").append(escapeCell(file.getName()))
                    .append(" | ").append(file.getMimeType())
                    .append(" | ").append(formatSize(file.getDataAsByteArray().length))
                    .append(" |\n");
        }
        answer.append("""

                I'm a placeholder model, so I don't look inside the files. A real model would get \
                them as media through Spring AI and could describe an image or summarize a document.

                The files are saved to the file storage together with your message: click a file \
                in the conversation to download it.
                """);
        return answer.toString();
    }

    private static String escapeCell(String value) {
        return value.replace("|", "\\|");
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        }
        return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private static List<Media> media(Prompt prompt) {
        return prompt.getInstructions().stream()
                .filter(UserMessage.class::isInstance)
                .map(UserMessage.class::cast)
                .flatMap(userMessage -> userMessage.getMedia().stream())
                .toList();
    }

    private static String promptText(Prompt prompt) {
        return prompt.getInstructions().stream()
                .map(message -> message.getText() == null ? "" : message.getText())
                .reduce("", (a, b) -> a + " " + b)
                .toLowerCase(Locale.ROOT);
    }

    /**
     * @return a pattern that finds the given stem at the start of a word
     */
    private static Pattern keyword(String stem) {
        return Pattern.compile("\\b" + stem);
    }

    private ChatResponse response(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }
}
