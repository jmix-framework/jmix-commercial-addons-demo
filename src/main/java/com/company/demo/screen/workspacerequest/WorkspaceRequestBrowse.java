package com.company.demo.screen.workspacerequest;

import com.company.demo.entity.User;
import com.company.demo.entity.WorkspaceRequest;
import com.company.demo.security.HrManagerRole;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.screen.*;
import org.flowable.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@UiController("WorkspaceRequest.browse")
@UiDescriptor("workspace-request-browse.xml")
@LookupComponent("workspaceRequestsTable")
public class WorkspaceRequestBrowse extends StandardLookup<WorkspaceRequest> {

    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private CurrentUserSubstitution currentUserSubstitution;
    @Autowired
    private GroupTable<WorkspaceRequest> workspaceRequestsTable;
    @Autowired
    private Button startProcessBtn;
    @Autowired
    private Notifications notifications;

    @Subscribe
    public void onInit(InitEvent event) {
        User user = (User) currentUserSubstitution.getEffectiveUser();

        // Enable starting process if the user has "HR Manager" role
        boolean isHrManager = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(name -> name.equals(HrManagerRole.CODE));
        startProcessBtn.setEnabled(isHrManager);
    }


    @Subscribe("startProcessBtn")
    public void onStartProcessBtnClick(Button.ClickEvent event) {
        // Show WorkspaceRequest edit screen with a new entity and start process when the entity is saved
        screenBuilders.editor(workspaceRequestsTable)
                .newEntity()
                .withScreenClass(WorkspaceRequestEdit.class)
                .withAfterCloseListener(afterCloseEvent -> {
                    if (afterCloseEvent.closedWith(StandardOutcome.COMMIT)) {
                        startProcess(afterCloseEvent.getSource().getEditedEntity());
                    }
                })
                .show();
    }

    private void startProcess(WorkspaceRequest workspaceRequest) {
        String businessKey = workspaceRequest.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + " - "
                + workspaceRequest.getEmployee().getFirstName() + " "
                + workspaceRequest.getEmployee().getLastName();
        Map<String, Object> variables = ParamsMap.of(
                "date", workspaceRequest.getDate(),
                "employee", workspaceRequest.getEmployee(),
                "workspaceRequest", workspaceRequest);
        runtimeService.startProcessInstanceByKey("workspace-preparation", businessKey, variables);

        notifications.create()
                .withCaption("Process started")
                .show();
    }
}