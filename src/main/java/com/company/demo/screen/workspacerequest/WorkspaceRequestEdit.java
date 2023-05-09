package com.company.demo.screen.workspacerequest;

import com.company.demo.entity.SoftwareRequest;
import com.company.demo.entity.User;
import io.jmix.bpmui.processform.ProcessFormContext;
import io.jmix.bpmui.processform.annotation.ProcessForm;
import io.jmix.bpmui.processform.annotation.ProcessVariable;
import io.jmix.core.TimeSource;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.screen.*;
import com.company.demo.entity.WorkspaceRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;

@UiController("WorkspaceRequest.edit")
@UiDescriptor("workspace-request-edit.xml")
@EditedEntityContainer("workspaceRequestDc")
@ProcessForm(allowedProcessKeys = {"workspace-preparation"})
public class WorkspaceRequestEdit extends StandardEditor<WorkspaceRequest> {

    @Autowired
    private CurrentUserSubstitution currentUserSubstitution;

    @Autowired
    private TimeSource timeSource;

    @Autowired
    private CollectionPropertyContainer<SoftwareRequest> softwareRequestsDc;

    @ProcessVariable
    protected WorkspaceRequest workspaceRequest;

    @Autowired
    protected ProcessFormContext processFormContext;

    @Subscribe
    public void onInit(InitEvent event) {
        // Set entity to edit from process variable
        setEntityToEdit(workspaceRequest);
    }

    @Subscribe
    public void onInitEntity(InitEntityEvent<WorkspaceRequest> event) {
        WorkspaceRequest workspaceRequest = event.getEntity();
        workspaceRequest.setHrManager((User) currentUserSubstitution.getEffectiveUser());
        workspaceRequest.setDate(timeSource.now().toLocalDate());
    }

    @Install(to = "softwareRequestsTable.create", subject = "initializer")
    private void softwareRequestsTableCreateInitializer(SoftwareRequest softwareRequest) {
        // Assign sequential numbers when creating SoftwareRequest instances
        Integer maxSortValue = softwareRequestsDc.getItems().stream()
                .map(SoftwareRequest::getSortValue)
                .max(Integer::compareTo)
                .orElse(0);
        softwareRequest.setSortValue(maxSortValue + 1);
    }

    @Subscribe("completeBtn")
    public void onCompleteBtnClick(Button.ClickEvent event) {
        // Save changes and complete the task
        commitChanges()
                .then(() -> {
                    if (processFormContext.getTask() != null) // in case the editor is opened outside process
                        processFormContext.taskCompletion().complete();
                    close(StandardOutcome.COMMIT);
                });
    }
}