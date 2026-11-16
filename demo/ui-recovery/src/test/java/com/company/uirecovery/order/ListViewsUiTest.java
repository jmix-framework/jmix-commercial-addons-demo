package com.company.uirecovery.order;

import com.company.uirecovery.UiRecoveryApplication;
import com.company.uirecovery.view.customer.CustomerListView;
import com.company.uirecovery.view.order.OrderListView;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@UiTest
@SpringBootTest(classes = {UiRecoveryApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
public class ListViewsUiTest {

    @Autowired
    ViewNavigationSupport navigationSupport;

    @Test
    void test_listViewsOpen() {
        navigationSupport.navigate(CustomerListView.class);
        assertInstanceOf(CustomerListView.class, UiTestUtils.getCurrentView());

        navigationSupport.navigate(OrderListView.class);
        assertInstanceOf(OrderListView.class, UiTestUtils.getCurrentView());
    }
}
