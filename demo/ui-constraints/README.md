# Jmix UI Constraints Demo

## Overview

The project includes the 
[Customer](src/main/java/com/company/uiconstraints/entity/Customer.java) entity and two resource roles:
- [EmployeeRole](src/main/java/com/company/uiconstraints/security/EmployeeRole.java) is defined using the annotated Java interface. 
- `AdHocEmployeeRole` is defined in the database and demonstrates a role that can be configured at runtime. 

The [customer-detail-view](src/main/resources/com/company/uiconstraints/view/customer/customer-detail-view.xml) displays the `comments` attribute inside the `details` container with `commentsPane` id. If the user has no permission to the `comments` attribute, the corresponding `textArea` component is hidden automatically. But the `commentsPane` container is still displayed, because for the security subsystem it's not related to the attribute.

The `EmployeeRole` hides the `commentsPane` container using the `@UiComponentPolicy` annotation provided by the UI Constraints add-on. 

The role also hides the `customersDataGrid.importAction` action of the [customer-list-view](src/main/resources/com/company/uiconstraints/view/customer/customer-list-view.xml). This action is not related to any entity, so access to it can be declaratively configured only using the `@UiControllerPolicy` annotation.

The `AdHocEmployeeRole` additionally hides the `genericFilter` component of [customer-list-view](src/main/resources/com/company/uiconstraints/view/customer/customer-list-view.xml).

## Demo Scenario

1. Run the application and go to <http://localhost:8106> in your browser.
2. Log in as `admin` with password `admin`.
3. Open the *Application → Customers* view. Check that the filter component and the *Import* action are visible.
4. Edit a customer. Check that the *Comments* panel and text area are visible.
5. Log in as `bob` with password `1`.
6. Open the *Application → Customers* view. Check that the filter component and the *Import* action are not available.
7. Edit a customer. Check that the *Comments* panel and text area are not available.

## See Also

- [UI Constraints Documentation](https://docs.jmix.io/jmix/uiconstraints/index.html)
- [UI Constraints on Marketplace](https://www.jmix.io/marketplace/ui-constraints/)
