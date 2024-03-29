package com.company.demo.security;

import com.company.demo.entity.*;
import io.jmix.bpmui.security.role.BpmProcessActorRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(name = "HR Manager", code = HrManagerRole.CODE, scope = "UI")
public interface HrManagerRole extends BpmProcessActorRole, BasicAppRole {

    String CODE = "hr-manager";

    @MenuPolicy(menuIds = {"User.browse", "WorkspaceRequest.browse", "Department.browse"})
    @ScreenPolicy(screenIds = {"User.browse", "User.edit", "WorkspaceRequest.browse", "Department.browse", "WorkspaceRequest.edit", "Department.edit", "SoftwareRequest.edit"})
    void screens();

    @EntityAttributePolicy(entityClass = User.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = User.class, actions = EntityPolicyAction.ALL)
    void user();

    @EntityAttributePolicy(entityClass = Department.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Department.class, actions = EntityPolicyAction.READ)
    void department();

    @EntityAttributePolicy(entityClass = Software.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Software.class, actions = EntityPolicyAction.READ)
    void software();

    @EntityAttributePolicy(entityClass = WorkspaceRequest.class, attributes = "processLog", action = EntityAttributePolicyAction.VIEW)
    @EntityAttributePolicy(entityClass = WorkspaceRequest.class, attributes = {"id", "version", "date", "hrManager", "employee", "workType", "softwareRequests"}, action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = WorkspaceRequest.class, actions = {EntityPolicyAction.READ, EntityPolicyAction.UPDATE, EntityPolicyAction.CREATE})
    void workspaceRequest();

    @EntityAttributePolicy(entityClass = SoftwareRequest.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = SoftwareRequest.class, actions = EntityPolicyAction.READ)
    void softwareRequest();
}