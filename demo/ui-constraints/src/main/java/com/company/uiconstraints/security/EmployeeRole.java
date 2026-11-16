package com.company.uiconstraints.security;

import com.company.uiconstraints.entity.Customer;
import com.company.uiconstraints.view.customer.CustomerDetailView;
import com.company.uiconstraints.view.customer.CustomerListView;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;
import io.jmix.uiconstraints.annotation.UiComponentPolicy;
import io.jmix.uiconstraints.annotation.UiComponentPolicyAction;
import io.jmix.uiconstraints.annotation.UiComponentPolicyEffect;

@ResourceRole(name = "EmployeeRole", code = EmployeeRole.CODE, scope = "UI")
public interface EmployeeRole {
    String CODE = "employee";

    @EntityAttributePolicy(entityClass = Customer.class, attributes = {"id", "version", "name", "email"}, action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Customer.class, actions = EntityPolicyAction.ALL)
    void customer();

    @MenuPolicy(menuIds = "Customer.list")
    @ViewPolicy(viewIds = {"Customer.list", "Customer.detail"})
    void screens();

    @UiComponentPolicy(viewClass = CustomerDetailView.class, componentIds = "commentsPane", action = UiComponentPolicyAction.VISIBLE, effect = UiComponentPolicyEffect.DENY)
    void customerDetail();

    @UiComponentPolicy(viewClass = CustomerListView.class, componentIds = "customersDataGrid.importAction", action = UiComponentPolicyAction.VISIBLE, effect = UiComponentPolicyEffect.DENY)
    void customerList();
}