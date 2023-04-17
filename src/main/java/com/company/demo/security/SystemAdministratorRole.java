package com.company.demo.security;

import com.company.demo.entity.*;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

import javax.annotation.Nonnull;

@Nonnull
@ResourceRole(name = "SystemAdministrator", code = SystemAdministratorRole.CODE, scope = "UI")
public interface SystemAdministratorRole {

    String CODE = "system-administrator";

    @MenuPolicy(menuIds = {"WorkspaceRequest.browse", "Software.browse"})
    @ScreenPolicy(screenIds = {"WorkspaceRequest.browse", "Software.browse", "Software.edit", "SoftwareRequest.edit", "WorkspaceRequest.edit"})
    void screens();

    @EntityAttributePolicy(entityClass = Department.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Department.class, actions = EntityPolicyAction.READ)
    void department();

    @EntityAttributePolicy(entityClass = Software.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Software.class, actions = EntityPolicyAction.ALL)
    void software();

    @EntityAttributePolicy(entityClass = SoftwareRequest.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = SoftwareRequest.class, actions = EntityPolicyAction.ALL)
    void softwareRequest();

    @EntityAttributePolicy(entityClass = User.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = User.class, actions = EntityPolicyAction.READ)
    void user();

    @EntityAttributePolicy(entityClass = WorkspaceRequest.class, attributes = {"id", "version", "date", "hrManager", "employee", "workType", "softwareRequests"}, action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = WorkspaceRequest.class, actions = {EntityPolicyAction.READ, EntityPolicyAction.UPDATE})
    void workspaceRequest();
}