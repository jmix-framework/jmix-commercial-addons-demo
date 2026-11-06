package com.company.aichat.chat;

import com.company.aichat.AiChatApplication;
import com.company.aichat.view.chat.ChatView;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.dom.Element;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.sidepanellayout.SidePanelLayout;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test of the chat view layout: the view loads, shows the keyword badges and toggles the
 * help panel from the button in the message input.
 */
@UiTest
@SpringBootTest(classes = {AiChatApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
public class ChatViewUiTest {

    @Autowired
    ViewNavigators viewNavigators;

    private ChatView view;

    @BeforeEach
    void openChatView() {
        viewNavigators.view(UiTestUtils.getCurrentView(), ChatView.class).navigate();
        view = UiTestUtils.getCurrentView();
    }

    @Test
    void helpButtonTogglesHelpPanel() {
        SidePanelLayout helpPanelLayout = UiTestUtils.getComponent(view, "helpPanelLayout");
        JmixButton helpButton = UiTestUtils.getComponent(view, "helpButton");
        assertThat(helpPanelLayout.isSidePanelOpened()).isFalse();

        helpButton.click();
        assertThat(helpPanelLayout.isSidePanelOpened()).isTrue();

        helpButton.click();
        assertThat(helpPanelLayout.isSidePanelOpened()).isFalse();
    }

    @Test
    void showsKeywordBadges() {
        List<String> badgeTexts = descendants(view.getElement())
                .flatMap(element -> element.getComponent().stream())
                .filter(Badge.class::isInstance)
                .map(component -> ((Badge) component).getText())
                .toList();

        assertThat(badgeTexts).containsExactly("summary", "email", "code", "long", "error", "fail");
    }

    private static Stream<Element> descendants(Element element) {
        return Stream.concat(Stream.of(element), element.getChildren().flatMap(ChatViewUiTest::descendants));
    }
}
