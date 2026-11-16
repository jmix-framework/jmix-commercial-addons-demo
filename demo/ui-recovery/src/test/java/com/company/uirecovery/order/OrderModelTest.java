package com.company.uirecovery.order;

import com.company.uirecovery.entity.Customer;
import com.company.uirecovery.entity.Order;
import com.company.uirecovery.entity.OrderLine;
import com.company.uirecovery.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.SaveContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
public class OrderModelTest {

    @Autowired
    DataManager dataManager;

    final List<Order> orders = new ArrayList<>();
    final List<Customer> customers = new ArrayList<>();

    @AfterEach
    void tearDown() {
        orders.forEach(order -> dataManager.load(Order.class).id(order.getId()).optional()
                .ifPresent(dataManager::remove));
        customers.forEach(customer -> dataManager.load(Customer.class).id(customer.getId()).optional()
                .ifPresent(dataManager::remove));
    }

    @Test
    void test_saveOrderWithLines() {
        Customer customer = dataManager.create(Customer.class);
        customer.setName("Test Customer");
        customers.add(customer);

        Order order = dataManager.create(Order.class);
        order.setNumber("T-1");
        order.setCustomer(customer);
        orders.add(order);

        OrderLine line1 = createLine(order, "Pen", 2, "1.50");
        OrderLine line2 = createLine(order, "Paper", 1, "4.00");

        dataManager.save(new SaveContext().saving(customer, order, line1, line2));

        Order loaded = dataManager.load(Order.class)
                .id(order.getId())
                .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE)
                        .add("customer", FetchPlan.BASE)
                        .add("lines", FetchPlan.BASE))
                .one();

        assertThat(loaded.getLines()).hasSize(2);
        assertThat(loaded.getCustomer().getName()).isEqualTo("Test Customer");
    }

    @Test
    void test_removeOrderRemovesLines() {
        Order order = dataManager.create(Order.class);
        order.setNumber("T-2");
        orders.add(order);
        OrderLine line = createLine(order, "Pen", 1, "1.50");
        dataManager.save(new SaveContext().saving(order, line));

        dataManager.remove(dataManager.load(Order.class).id(order.getId()).one());

        assertThat(dataManager.load(OrderLine.class).id(line.getId()).optional()).isEmpty();
    }

    private OrderLine createLine(Order order, String product, int quantity, String price) {
        OrderLine line = dataManager.create(OrderLine.class);
        line.setOrder(order);
        line.setProduct(product);
        line.setQuantity(quantity);
        line.setPrice(new BigDecimal(price));
        return line;
    }
}
