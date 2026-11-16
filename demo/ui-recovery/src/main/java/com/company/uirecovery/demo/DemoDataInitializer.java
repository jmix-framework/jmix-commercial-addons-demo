package com.company.uirecovery.demo;

import com.company.uirecovery.entity.Customer;
import com.company.uirecovery.entity.Order;
import com.company.uirecovery.entity.OrderLine;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.security.Authenticated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DemoDataInitializer {

    @Autowired
    private DataManager dataManager;

    @EventListener
    @Authenticated
    public void onApplicationStarted(ApplicationStartedEvent event) {
        initData();
    }

    public void initData() {
        if (!dataManager.load(Customer.class).all().maxResults(1).list().isEmpty()) {
            return;
        }

        Customer horizon = createCustomer("Horizon Technologies Inc.", "contact@horizontech.com");
        Customer pinnacle = createCustomer("Pinnacle Solutions Group", "info@pinnaclesg.com");
        Customer evergreen = createCustomer("Evergreen Manufacturing Ltd.", "support@evergreenmanuf.com");
        Customer bluewave = createCustomer("Bluewave Logistics", "orders@bluewavelogistics.com");

        SaveContext saveContext = new SaveContext().saving(horizon, pinnacle, evergreen, bluewave);

        addOrder(saveContext, "ORD-1001", 28, horizon,
                "Deliver to the reception desk on the 3rd floor.",
                line("Ballpoint pen, blue (box of 50)", 4, "12.90"),
                line("A4 copy paper (500 sheets)", 20, "5.49"),
                line("Stapler", 2, "8.75"));
        addOrder(saveContext, "ORD-1002", 21, pinnacle,
                null,
                line("Sticky notes 76x76 mm (pack of 12)", 10, "6.20"),
                line("Whiteboard marker set", 5, "9.99"));
        addOrder(saveContext, "ORD-1003", 14, evergreen,
                "Monthly supply for the production office.",
                line("A4 copy paper (500 sheets)", 50, "5.49"),
                line("Ring binder A4", 30, "2.35"),
                line("Toner cartridge, black", 3, "64.00"));
        addOrder(saveContext, "ORD-1004", 7, bluewave,
                null,
                line("Shipping labels (pack of 100)", 12, "11.40"),
                line("Packing tape", 24, "1.95"));
        addOrder(saveContext, "ORD-1005", 2, horizon,
                "Urgent: needed before the board meeting.",
                line("Notebook A5, hard cover", 15, "4.80"),
                line("Desk organizer", 3, "18.50"),
                line("Ballpoint pen, black (box of 50)", 2, "12.90"));

        dataManager.save(saveContext);
    }

    private Customer createCustomer(String name, String email) {
        Customer customer = dataManager.create(Customer.class);
        customer.setName(name);
        customer.setEmail(email);
        return customer;
    }

    private void addOrder(SaveContext saveContext, String number, int daysAgo, Customer customer,
                          String comment, OrderLine... lines) {
        Order order = dataManager.create(Order.class);
        order.setNumber(number);
        order.setDate(LocalDate.now().minusDays(daysAgo));
        order.setCustomer(customer);
        order.setComment(comment);
        saveContext.saving(order);

        for (OrderLine line : lines) {
            line.setOrder(order);
            saveContext.saving(line);
        }
    }

    private OrderLine line(String product, int quantity, String price) {
        OrderLine line = dataManager.create(OrderLine.class);
        line.setProduct(product);
        line.setQuantity(quantity);
        line.setPrice(new BigDecimal(price));
        return line;
    }
}
