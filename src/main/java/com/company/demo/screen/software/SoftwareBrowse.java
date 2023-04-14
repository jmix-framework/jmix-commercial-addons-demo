package com.company.demo.screen.software;

import io.jmix.ui.screen.*;
import com.company.demo.entity.Software;

@UiController("Software.browse")
@UiDescriptor("software-browse.xml")
@LookupComponent("softwaresTable")
public class SoftwareBrowse extends StandardLookup<Software> {
}