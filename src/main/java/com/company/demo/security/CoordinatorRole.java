package com.company.demo.security;

import com.company.demo.entity.Department;
import com.company.demo.entity.User;
import com.company.demo.entity.WorkspaceRequest;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

import javax.annotation.Nonnull;

@Nonnull
@ResourceRole(name = "Coordinator", code = CoordinatorRole.CODE, scope = "UI")
public interface CoordinatorRole {

    String CODE = "coordinator";

    @MenuPolicy(menuIds = "WorkspaceRequest.browse")
    @ScreenPolicy(screenIds = {"WorkspaceRequest.browse", "WorkspaceRequest.edit"})
    void screens();

    @EntityAttributePolicy(entityClass = Department.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Department.class, actions = EntityPolicyAction.READ)
    void department();

    @EntityAttributePolicy(entityClass = User.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = User.class, actions = EntityPolicyAction.READ)
    void user();

    @EntityAttributePolicy(entityClass = WorkspaceRequest.class, attributes = {"id", "version", "date", "hrManager", "employee", "workType"}, action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = WorkspaceRequest.class, actions = {EntityPolicyAction.READ, EntityPolicyAction.UPDATE})
    void workspaceRequest();
}