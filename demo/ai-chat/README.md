# Jmix AI Chat Demo

## Overview

The application's data model includes the [ChatMessage](src/main/java/com/company/aichat/entity/ChatMessage.java) and [MessageFeedback](src/main/java/com/company/aichat/entity/MessageFeedback.java) entities. `ChatMessage` implements the `AiChatMessage` interface of the add-on and stores the conversation: the message role, its content and the attached files. `MessageFeedback` stores the rating and the comment given to an assistant answer.

The [chat-view](src/main/resources/com/company/aichat/view/chat/chat-view.xml) contains the `aiChat` UI component bound to a collection container of `ChatMessage` entities with `autoSave`, so the conversation is saved to the database. The view also defines suggestion buttons for the empty state, per-message actions, an attachment menu, keyword badges above the input and a side panel with the demo scenarios.

[ChatView](src/main/java/com/company/aichat/view/chat/ChatView.java) provides the `llmProvider` and `messageFactory` handlers, publishes the thinking steps shown before an answer, stores the attached files in the file storage and saves the feedback given to the answers.

The answers come from [PlaceholderChatModel](src/main/java/com/company/aichat/service/PlaceholderChatModel.java), a Spring AI chat model that picks a prepared reply by keywords in the user message, so the demo runs without an AI provider or an API key. [ChatService](src/main/java/com/company/aichat/service/ChatService.java) streams its answers through the Spring AI `ChatClient`. To connect a real model, replace `PlaceholderChatModel` with a `ChatModel` of any Spring AI provider.

## Demo Scenario

1. Run the application and go to <http://localhost:8110> in your browser.
2. Log in as `admin` with password `admin`.
3. Open the *AI Chat* view.
4. Click the *Summarize the project status* suggestion. The chat shows the thinking steps first, then streams the answer and renders it as Markdown with a table.
5. Send a message with one of the keywords shown above the input:
   - `code` to get code blocks with syntax highlighting and a copy button;
   - `email` to get a long answer that streams slowly. Click *Stop* while it streams: the partial answer stays on the screen but isn't saved;
   - `long` to get a long article;
   - `error` or `fail` to get a failed generation with the error banner and the *Retry* button.
6. Click *+ → Attach a file* or drop a file onto the chat, then send the message. The answer lists the received files. Click the file in the conversation to download it.
7. Use the actions under the messages: copy a message, regenerate an answer, or rate it with thumbs up or down and leave a comment.
8. Reload the page. The conversation is restored from the database. Click *+ → Clear history* to delete it.
9. Click the *?* button in the input to open the side panel that describes all demo scenarios. Click *Try it* in a scenario to send its prompt.

## See Also

- [AI Chat Documentation](https://docs.jmix.io/jmix/ai-chat/index.html)
