package com.company.demo.screen.software;

import io.jmix.ui.screen.*;
import com.company.demo.entity.Software;

@UiController("Software.edit")
@UiDescriptor("software-edit.xml")
@EditedEntityContainer("softwareDc")
public class SoftwareEdit extends StandardEditor<Software> {
}