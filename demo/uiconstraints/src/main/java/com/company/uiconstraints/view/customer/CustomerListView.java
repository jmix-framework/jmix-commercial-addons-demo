package com.company.uiconstraints.view.customer;

import com.company.uiconstraints.entity.Customer;
import com.company.uiconstraints.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;


@Route(value = "customers", layout = MainView.class)
@ViewController(id = "Customer.list")
@ViewDescriptor(path = "customer-list-view.xml")
@LookupComponent("customersDataGrid")
@DialogMode(width = "64em")
public class CustomerListView extends StandardListView<Customer> {

    @Autowired
    private DataManager dataManager;
    @ViewComponent
    private CollectionLoader<Customer> customersDl;

    @Subscribe("customersDataGrid.importAction")
    public void onCustomersDataGridImportAction(final ActionPerformedEvent event) {
        Customer customer = dataManager.create(Customer.class);
        customer.setName("Imported customer " + System.currentTimeMillis());
        customer.setComments("Imported at " + LocalDateTime.now());
        dataManager.saveWithoutReload(customer);

        customersDl.load();
    }
}