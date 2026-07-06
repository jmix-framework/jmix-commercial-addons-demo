package com.company.dynamicmodel.security;

import com.company.dynamicmodel.entity.User;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Manager", code = ManagerRole.CODE, scope = "UI")
public interface ManagerRole {
    String CODE = "manager";

    @MenuPolicy(menuIds = "User.list")
    @ViewPolicy(viewIds = {
            "User.list",
            "User.detail"
    })
    void screens();

    @EntityAttributePolicy(entityClass = User.class,
            attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = User.class, actions = EntityPolicyAction.ALL)
    void user();
}