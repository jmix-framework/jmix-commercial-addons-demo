package com.company.dynamicmodel.security;

import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Employee", code = EmployeeRole.CODE, scope = "UI")
public interface EmployeeRole {
    String CODE = "employee";
}