package com.company.uirecovery.view.orderline;

import com.company.uirecovery.entity.OrderLine;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@ViewController(id = "OrderLine.detail")
@ViewDescriptor(path = "order-line-detail-view.xml")
@EditedEntityContainer("orderLineDc")
@DialogMode(width = "30em")
public class OrderLineDetailView extends StandardDetailView<OrderLine> {
}
