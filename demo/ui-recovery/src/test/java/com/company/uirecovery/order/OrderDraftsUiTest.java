package com.company.uirecovery.order;

import com.company.uirecovery.UiRecoveryApplication;
import com.company.uirecovery.entity.Customer;
import com.company.uirecovery.entity.Order;
import com.company.uirecovery.entity.OrderLine;
import com.company.uirecovery.view.main.MainView;
import com.company.uirecovery.view.order.OrderDetailView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.FetchPlan;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.dialog.DialogInfo;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.uirecovery.entity.ViewDraft;
import io.jmix.uirecoveryflowui.facet.DraftsFacet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@UiTest
@SpringBootTest(classes = {UiRecoveryApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
public class OrderDraftsUiTest {

    @Autowired
    UnconstrainedDataManager dataManager;

    @Autowired
    ViewNavigationSupport navigationSupport;

    Customer customer;
    Order order;

    @BeforeEach
    void setUp() {
        customer = dataManager.create(Customer.class);
        customer.setName("Draft Test Customer");

        order = dataManager.create(Order.class);
        order.setNumber("initial");
        order.setCustomer(customer);

        OrderLine line = dataManager.create(OrderLine.class);
        line.setOrder(order);
        line.setProduct("Pen");
        line.setQuantity(2);
        line.setPrice(new BigDecimal("1.50"));

        dataManager.save(new SaveContext().saving(customer, order, line));
    }

    @AfterEach
    void tearDown() {
        dataManager.load(ViewDraft.class).all().list().forEach(dataManager::remove);
        dataManager.load(Order.class)
                .query("select e from Order_ e where e.number in :numbers or e.id = :id")
                .parameter("numbers", List.of("N-1"))
                .parameter("id", order.getId())
                .list()
                .forEach(dataManager::remove);
        dataManager.load(Customer.class).id(customer.getId()).optional().ifPresent(dataManager::remove);
    }

    @Test
    void test_editWritesDraft() {
        OrderDetailView view = openOrder(order.getId().toString());
        view.getEditedEntity().setComment("typed");

        List<ViewDraft> drafts = drafts();
        assertEquals(1, drafts.size());
        assertEquals("Order_.detail", drafts.get(0).getViewId());
        assertNotNull(drafts.get(0).getEntityId());
        assertNotEquals("new", drafts.get(0).getEntityId());
    }

    @Test
    void test_restoreBringsBackFieldsAndLines() {
        createFieldsAndLinesDraft();

        OrderDetailView view = openOrder(order.getId().toString());
        assertEquals("initial", view.getEditedEntity().getNumber());

        clickRestore();

        Order restored = view.getEditedEntity();
        assertEquals("drafted", restored.getNumber());
        assertEquals("long text", restored.getComment());
        assertThat(restored.getLines()).extracting(OrderLine::getProduct)
                .containsExactlyInAnyOrder("Pen", "Stapler");
        assertEquals(5, findLine(restored, "Pen").getQuantity());
    }

    @Test
    void test_saveAfterRestorePersistsAndDeletesDraft() {
        createFieldsAndLinesDraft();

        OrderDetailView view = openOrder(order.getId().toString());
        clickRestore();
        view.save();

        assertTrue(drafts().isEmpty());
        Order saved = dataManager.load(Order.class)
                .id(order.getId())
                .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE).add("lines", FetchPlan.BASE))
                .one();
        assertEquals("drafted", saved.getNumber());
        assertThat(saved.getLines()).hasSize(2);
        assertEquals(5, findLine(saved, "Pen").getQuantity());
    }

    @Test
    void test_removedLineStaysRemovedAfterRestore() {
        OrderDetailView view = openOrder(order.getId().toString());
        OrderLine pen = findLine(view.getEditedEntity(), "Pen");
        linesDc(view).getMutableItems().remove(pen);
        dataContext(view).remove(pen);
        leaveWithoutSaving(view);

        OrderDetailView reopened = openOrder(order.getId().toString());
        clickRestore();

        assertThat(reopened.getEditedEntity().getLines()).isEmpty();
    }

    @Test
    void test_newOrderDraftRestored() {
        OrderDetailView view = openOrder("new");
        view.getEditedEntity().setNumber("N-1");
        leaveWithoutSaving(view);

        List<ViewDraft> drafts = drafts();
        assertEquals(1, drafts.size());
        assertEquals("new", drafts.get(0).getEntityId());

        OrderDetailView reopened = openOrder("new");
        clickRestore();

        assertEquals("N-1", reopened.getEditedEntity().getNumber());
    }

    @Test
    void test_commentSendsValueWhileTyping() {
        OrderDetailView view = openOrder(order.getId().toString());
        JmixTextArea commentField = UiTestUtils.getComponent(view, "commentField");

        assertEquals(ValueChangeMode.LAZY, commentField.getValueChangeMode());
    }

    @Test
    void test_saveDeletesDraft() {
        OrderDetailView view = openOrder(order.getId().toString());
        view.getEditedEntity().setNumber("changed");
        assertEquals(1, drafts().size());

        view.save();

        assertTrue(drafts().isEmpty());
    }

    private void createFieldsAndLinesDraft() {
        OrderDetailView view = openOrder(order.getId().toString());
        Order edited = view.getEditedEntity();
        edited.setNumber("drafted");
        edited.setComment("long text");
        findLine(edited, "Pen").setQuantity(5);

        OrderLine stapler = dataContext(view).create(OrderLine.class);
        stapler.setOrder(edited);
        stapler.setProduct("Stapler");
        stapler.setQuantity(1);
        stapler.setPrice(new BigDecimal("9.99"));
        linesDc(view).getMutableItems().add(stapler);

        leaveWithoutSaving(view);
    }

    private OrderDetailView openOrder(String id) {
        // Open another view first, so that a new view instance is created each time
        navigationSupport.navigate(MainView.class);
        navigationSupport.navigate(OrderDetailView.class, new RouteParameters("id", id));
        return UiTestUtils.getCurrentView();
    }

    private void leaveWithoutSaving(OrderDetailView view) {
        // Write all pending changes at once, as the facet does when the view is detached
        DraftsFacet draftsFacet = (DraftsFacet) ViewControllerUtils.getViewFacets(view).getFacet("draftsFacet");
        draftsFacet.saveDraftNow();
        assertEquals(1, drafts().size());
        // Leave the view without saving or discarding, as if the session was lost
        view.clearChanges();
    }

    private void clickRestore() {
        DialogInfo dialog = UiTestUtils.getLastOpenedDialog();
        assertNotNull(dialog);
        Button restoreButton = dialog.getButtons().get(0);
        assertEquals("Restore", restoreButton.getText());
        restoreButton.click();
    }

    private static OrderLine findLine(Order order, String product) {
        return order.getLines().stream()
                .filter(line -> product.equals(line.getProduct()))
                .findFirst()
                .orElseThrow();
    }

    private static DataContext dataContext(OrderDetailView view) {
        return ViewControllerUtils.getViewData(view).getDataContext();
    }

    @SuppressWarnings("unchecked")
    private static CollectionPropertyContainer<OrderLine> linesDc(OrderDetailView view) {
        return (CollectionPropertyContainer<OrderLine>) ViewControllerUtils.getViewData(view).getContainer("linesDc");
    }

    private List<ViewDraft> drafts() {
        return dataManager.load(ViewDraft.class).all().list();
    }
}
