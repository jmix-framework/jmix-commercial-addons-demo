package com.company.aichat.service;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local Spring AI chat model that plays a claims assistant without contacting any provider.
 * It answers with prepared, insurance-flavoured Markdown chosen by keywords in the user prompt,
 * to show streaming, tables, code blocks, long answers, attachments and the error state.
 *
 * <p>Keywords (case-insensitive, matched anywhere in the prompt), first match wins:
 * <ul>
 *     <li>a message with an image attached — a damage assessment of the photo;</li>
 *     <li>{@code error} — a partial answer, then a failure;</li>
 *     <li>{@code code} — a Java code block;</li>
 *     <li>{@code letter} — a long letter to the customer, slow enough to press Stop;</li>
 *     <li>{@code summar} — a claim summary with a Markdown table;</li>
 *     <li>{@code long text} — a long claims-handling guide;</li>
 *     <li>anything else — the default answer listing what the assistant can do.</li>
 * </ul>
 */
public class PlaceholderChatModel implements ChatModel {

    private static final String ANSWER_DEFAULT = """
            I'm your claims assistant. Here is what I can help you with:

            1. **Summarize a claim** — status, coverage, damage and the expected payout at a glance.
            2. **Draft customer letters** — document requests, decisions and payout notices.
            3. **Assess damage from photos** — attach a photo and I'll list the damaged zones.
            4. **Check policy terms** — coverage limits, deductibles and validity periods.

            ---

            ### Your queue today

            | Claim | Customer | Status | Next step |
            |-------|----------|--------|-----------|
            | CLM-1042 | Emily Carter | Awaiting documents | Get the repair estimate from Harbor Auto Body |
            | CLM-1047 | Daniel Brooks | In review | Confirm the damage percent with the photos |
            | CLM-1049 | Sophia Turner | New | Assign an adjuster and request the police report |
            | CLM-1038 | James Whitaker | Approved | Schedule the payout |

            Tip: ask me to **summarize a claim**, **draft a letter to the customer**, or attach a photo of the damage.
            """;

    private static final String ANSWER_SUMMARY = """
            ### Claim CLM-1042 — summary

            Front-left collision at the intersection of Maple Avenue and 5th Street on **October 18, 2026**. \
            A delivery van ran a red light; the police report confirms the other driver was at fault.

            | Item | Value |
            |------|-------|
            | Customer | Emily Carter |
            | Policy | POL-2026-0187 — Comprehensive |
            | Vehicle | 2022 Toyota RAV4, plate 8KTR412 |
            | Market value | $32,500.00 |
            | Damage | **35%** — front bumper (severe), left side (moderate), right side (minor) |
            | Coverage limit / deductible | $40,000.00 / $500.00 |
            | Estimated payout | **$10,875.00** |
            | Status | Awaiting documents |

            **What's missing:** the itemized repair estimate from *Harbor Auto Body*. \
            Once it arrives, the claim can move to approval — the estimate is well within the coverage limit.

            > The other driver's insurer has been notified, so we can pursue subrogation after the payout.
            """;

    private static final String ANSWER_LETTER = """
            **Subject:** Your claim CLM-1042 — one document needed to complete the review

            Dear Ms. Carter,

            Thank you for reporting the accident of October 18, 2026, and for sending the photos of your \
            2022 Toyota RAV4 so promptly. I'm sorry you had to go through this, and I'm glad to hear that you \
            were not seriously hurt.

            I have reviewed your claim under policy **POL-2026-0187**. Your comprehensive coverage applies to \
            this incident, and the police report confirms that the other driver ran a red light at the \
            intersection of Maple Avenue and 5th Street.

            Based on the photos and the inspection notes, we currently assess the damage at about **35%** of \
            the vehicle's market value. After your $500 deductible, the estimated payout is **$10,875.00**.

            **To finalize the review, we need one more document:**

            - An itemized **repair estimate** from Harbor Auto Body (or another licensed repair shop of your \
            choice), showing parts, labor hours and paint costs.

            You can upload the estimate in the customer portal, reply to this email with the file attached, \
            or ask the shop to send it to us directly at claims@northwind-insurance.example.

            **What happens next:**

            1. Once we receive the estimate, I will compare it with our assessment within two business days.
            2. If the estimate is within the assessed amount, we will approve the claim right away.
            3. The payment will be sent to the repair shop or to you, whichever you prefer.

            Because the other driver was at fault, we will also seek recovery of your deductible from their \
            insurer. If we succeed, we will refund the $500 to you — there is nothing you need to do for that.

            If you need a rental car while your vehicle is being repaired, please let me know: your policy \
            includes rental reimbursement of up to $40 per day for 30 days.

            Please don't hesitate to call me at (555) 014-2290 if you have any questions.

            Kind regards,

            **Alex Morgan**
            Senior Claims Adjuster
            Northwind Insurance
            """;

    private static final String ANSWER_CODE = """
            Here is how the estimated payout of a claim is calculated in this application:

            ```java
            @Service
            public class PayoutCalculator {

                public BigDecimal calculate(BigDecimal marketValue, Integer damagePercent,
                                            BigDecimal deductible, BigDecimal coverageLimit) {
                    BigDecimal damage = marketValue.multiply(BigDecimal.valueOf(damagePercent))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal payout = damage.subtract(deductible).max(BigDecimal.ZERO);
                    return payout.min(coverageLimit);
                }
            }
            ```

            For **CLM-1042**: `32,500 × 35% − 500 = 10,875`, which is below the `40,000` coverage limit, \
            so the estimated payout is **$10,875.00**.

            The same formula expressed as a JPQL query over all claims awaiting documents:

            ```sql
            select c.number, c.damagePercent, c.estimatedPayout
            from Claim c
            where c.status = 'AWAITING_DOCUMENTS'
            order by c.estimatedPayout desc
            ```
            """;

    private static final String ANSWER_PHOTO = """
            ### Damage assessment from the photo

            I can see a mid-size SUV with impact damage concentrated on the **front-left corner**:

            - **Front bumper** — cracked cover, pushed in by about 5 cm on the left. *Severe* — replace.
            - **Left headlight** — housing broken, lens missing. *Severe* — replace.
            - **Hood** — crease along the left edge, paint chipped. *Moderate* — repair and repaint.
            - **Left front fender** — dented above the wheel arch. *Moderate* — repair.
            - **Windshield** — no visible cracks. *No damage.*

            **Estimated damage:** about **30–35%** of the vehicle's market value, which is consistent with \
            the 35% recorded on claim CLM-1042.

            **Recommended next steps:**

            1. Ask the repair shop to check the radiator support and the left headlight bracket — front-left \
            impacts at this angle often damage them as well.
            2. Request the itemized repair estimate to confirm the parts list.
            """;

    private static final String ANSWER_LONG = """
            # Claims handling guide

            This guide walks through the life of a motor claim, from the first notice of loss to the payout.

            ## 1. First notice of loss

            Register the claim the same day it is reported. Capture the date, time and place of the incident, \
            the vehicles and people involved, and whether the police attended. Ask the customer for photos \
            while the damage is still fresh.

            ## 2. Coverage check

            Confirm that the policy was valid on the incident date and that the coverage type applies: \
            third-party policies do not pay for the insured's own vehicle, collision covers impacts, and \
            comprehensive also covers theft, fire, hail and vandalism.

            ## 3. Documents

            | Document | Required for |
            |----------|--------------|
            | Police report | Any collision with another vehicle or injury |
            | Photos | Every claim |
            | Repair estimate | Every claim with repairable damage |
            | Driver's license | Claims where the driver is not the policyholder |

            ## 4. Damage assessment

            Assess the damage as a percentage of the vehicle's market value. Record each damaged zone \
            separately, with a severity and an estimated cost, so the percentage can be explained later.

            ## 5. Payout estimate

            The estimated payout is the assessed damage minus the deductible, capped by the coverage limit. \
            A payout above 70% of the market value usually means the vehicle should be treated as a total loss.

            ## 6. Decision

            Approve the claim when the documents are complete and the estimate matches the assessment. \
            Reject it only with a written reason that cites the policy terms.

            ## 7. Payment and recovery

            Pay the repair shop or the customer, then pursue subrogation against the at-fault party's insurer \
            to recover the payout and the customer's deductible.

            ## Summary

            Register fast, check coverage early, collect documents once, and explain every number. A claim \
            handled this way can be closed in under ten business days.
            """;

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
        String text = promptText(prompt);
        if (!hasMedia(prompt) && text.contains("error")
                && !text.contains("code") && !text.contains("long text")) {
            // Emit a partial answer, then fail — exercises the error banner + retry.
            return emit("Checking the policy terms and the damage report — ")
                    .concatWith(Flux.error(new RuntimeException("Simulated generation failure")));
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
        if (hasMedia(prompt)) {
            return ANSWER_PHOTO;
        }
        String text = promptText(prompt);
        if (text.contains("code")) {
            return ANSWER_CODE;
        }
        if (text.contains("letter")) {
            return ANSWER_LETTER;
        }
        if (text.contains("summar")) {
            return ANSWER_SUMMARY;
        }
        if (text.contains("long text")) {
            return ANSWER_LONG;
        }
        return ANSWER_DEFAULT;
    }

    private boolean hasMedia(Prompt prompt) {
        return prompt.getInstructions().stream()
                .anyMatch(message -> message instanceof UserMessage userMessage
                        && !userMessage.getMedia().isEmpty());
    }

    private String promptText(Prompt prompt) {
        return prompt.getInstructions().stream()
                .map(message -> message.getText() == null ? "" : message.getText())
                .reduce("", (a, b) -> a + " " + b)
                .toLowerCase(Locale.ROOT);
    }

    private ChatResponse response(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }
}
