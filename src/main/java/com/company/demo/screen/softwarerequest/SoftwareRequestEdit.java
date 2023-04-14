package com.company.demo.screen.softwarerequest;

import io.jmix.ui.screen.*;
import com.company.demo.entity.SoftwareRequest;

@UiController("SoftwareRequest.edit")
@UiDescriptor("software-request-edit.xml")
@EditedEntityContainer("softwareRequestDc")
public class SoftwareRequestEdit extends StandardEditor<SoftwareRequest> {
}