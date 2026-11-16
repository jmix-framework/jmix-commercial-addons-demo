package com.company.uirecovery.demo;

import com.company.uirecovery.entity.Customer;
import com.company.uirecovery.entity.Order;
import com.company.uirecovery.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
public class DemoDataInitializerTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    DemoDataInitializer demoDataInitializer;

    @Test
    void test_dataExistsAfterStartup() {
        List<Order> orders = dataManager.load(Order.class)
                .query("select e from Order_ e where e.number like 'ORD-%'")
                .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE).add("lines", FetchPlan.BASE))
                .list();

        assertThat(orders).isNotEmpty();
        assertThat(orders).allSatisfy(order ->
                assertThat(order.getLines()).hasSizeBetween(2, 3));
    }

    @Test
    void test_initDataDoesNotDuplicate() {
        int before = dataManager.load(Customer.class).all().list().size();

        demoDataInitializer.initData();

        int after = dataManager.load(Customer.class).all().list().size();
        assertThat(after).isEqualTo(before);
    }
}
