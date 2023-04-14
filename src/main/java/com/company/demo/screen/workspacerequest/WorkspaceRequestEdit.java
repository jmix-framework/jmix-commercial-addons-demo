package com.company.demo.screen.workspacerequest;

import com.company.demo.entity.SoftwareRequest;
import com.company.demo.entity.User;
import io.jmix.core.TimeSource;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.screen.*;
import com.company.demo.entity.WorkspaceRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;

@UiController("WorkspaceRequest.edit")
@UiDescriptor("workspace-request-edit.xml")
@EditedEntityContainer("workspaceRequestDc")
public class WorkspaceRequestEdit extends StandardEditor<WorkspaceRequest> {

    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private TimeSource timeSource;
    @Autowired
    private CollectionPropertyContainer<SoftwareRequest> softwareRequestsDc;

    @Subscribe
    public void onInitEntity(InitEntityEvent<WorkspaceRequest> event) {
        WorkspaceRequest workspaceRequest = event.getEntity();
        workspaceRequest.setHrManager((User) currentAuthentication.getUser());
        workspaceRequest.setDate(timeSource.now().toLocalDate());
    }

    @Install(to = "softwareRequestsTable.create", subject = "initializer")
    private void softwareRequestsTableCreateInitializer(SoftwareRequest softwareRequest) {
        Integer maxSortValue = softwareRequestsDc.getItems().stream()
                .map(SoftwareRequest::getSortValue)
                .max(Integer::compareTo)
                .orElse(0);
        softwareRequest.setSortValue(maxSortValue + 1);
    }
}