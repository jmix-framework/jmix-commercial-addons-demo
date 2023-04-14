package com.company.demo.screen.workspacerequest;

import io.jmix.ui.screen.*;
import com.company.demo.entity.WorkspaceRequest;

@UiController("WorkspaceRequest.browse")
@UiDescriptor("workspace-request-browse.xml")
@LookupComponent("workspaceRequestsTable")
public class WorkspaceRequestBrowse extends StandardLookup<WorkspaceRequest> {
}