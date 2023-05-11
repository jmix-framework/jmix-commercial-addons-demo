package com.company.demo.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(name = "Basic", code = BasicAppRole.CODE, scope = "UI")
public interface BasicAppRole {

    String CODE = "app-basic";

    @ScreenPolicy(screenIds = "InfoScreen")
    void screens();
}