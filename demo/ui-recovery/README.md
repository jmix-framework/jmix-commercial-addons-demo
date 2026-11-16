# Jmix UI Recovery Demo

## Overview

The UI Recovery add-on protects users from losing unsaved changes. While the user edits data in a view, the add-on saves the changes to the database as a _draft_. If the work is interrupted, for example because the browser tab was closed, the session expired, or the server was restarted, the user can restore the draft when opening the view again.

The demo project includes the [Customer](src/main/java/com/company/uirecovery/entity/Customer.java), [Order](src/main/java/com/company/uirecovery/entity/Order.java) and [OrderLine](src/main/java/com/company/uirecovery/entity/OrderLine.java) entities. Order lines are a composition of the order and are edited in a dialog inside the order view.

The [order-detail-view](src/main/resources/com/company/uirecovery/view/order/order-detail-view.xml) contains the `uirec:drafts` facet. This is the only thing needed to save drafts of the order, including its lines. No Java code is required.

The *Comment* text area has `valueChangeMode="LAZY"`, so the text is sent to the server when the user stops typing, without leaving the field. This lets the add-on save long typed text too.

The `jmix.uirecovery.drafts.show-on-login=true` property in [application.properties](src/main/resources/application.properties) opens the *Unsaved drafts* view after login if the user has drafts.

## Demo Scenario

### Restore changes of an existing order

1. Run the application and go to <http://localhost:8111> in your browser.
2. Log in as `admin` with password `admin`.
3. Open the *Application → Orders* view and edit an order.
4. Change the *Number* field.
5. In the *Lines* table, edit a line and change its quantity, add a new line, and remove one line. Click *OK* in the line dialog each time.
6. Type a few words in the *Comment* field. Don't leave the field. Wait a few seconds.
7. Don't click *OK* in the order view. Close the browser tab.
8. Open <http://localhost:8111> in a new tab and edit the same order. The *Unsaved changes found* dialog appears.
9. Click *Restore*. All your changes, including the comment and the changes of the lines, are back in the form.
10. Click *OK* to save the order.

### Restore a new order after login

1. Open the *Application → Orders* view and click *Create*.
2. Fill in the *Number* field and add a line. Click *OK* in the line dialog.
3. Don't click *OK* in the order view. Open the user menu in the top right corner and click *Log out*.
4. Log in again. The *Unsaved drafts* view opens and shows the draft of the new order.
5. Click the draft card. The order view opens with the *Unsaved changes found* dialog.
6. Click *Not now*. The dialog closes, and the order form is empty. The draft is kept.
7. Go to the *Orders* view and click *Create* again. The dialog appears again.
8. Click *Restore*. The order number and the line are back. Click *OK* to save the order.

### Discard a draft

1. Edit an order, change its *Number* field, and close the browser tab without saving.
2. Open the application in a new tab and edit the same order.
3. Click *Discard* in the dialog. The form shows the saved values.
4. Close the order view and edit it again. The dialog doesn't appear, because the draft was deleted.

## See Also

- [UI Recovery Documentation](https://docs.jmix.io/jmix/ui-recovery/index.html)
- [UI Recovery on Marketplace](https://www.jmix.io/marketplace/uirecovery/)
