package com.company.dynamicmodel.demo;

import com.company.dynamicmodel.entity.Department;
import com.company.dynamicmodel.entity.User;
import com.company.dynamicmodel.security.EmployeeRole;
import com.company.dynamicmodel.security.ManagerRole;
import com.company.dynamicmodel.security.UiMinimalRole;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.security.Authenticated;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securitydata.entity.UserSubstitutionEntity;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DemoDataInitializer {

    private final DataManager dataManager;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(DataManager dataManager, PasswordEncoder passwordEncoder) {
        this.dataManager = dataManager;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener
    @Authenticated
    public void onApplicationStarted(ApplicationStartedEvent event) {
        if (!dataManager.load(Department.class).all().maxResults(1).list().isEmpty()) {
            return;
        }
        List<User> users = initUsers();
        initDepartments(users);
        initUserSubstitutions();
        assignRoles(users);
    }

    private void initUserSubstitutions() {
        Arrays.asList("alice", "bob").forEach(name -> {
            UserSubstitutionEntity userSubstitution = dataManager.create(UserSubstitutionEntity.class);
            userSubstitution.setUsername("admin");
            userSubstitution.setSubstitutedUsername(name);
            dataManager.save(userSubstitution);
        });
    }

    private List<Department> initDepartments(List<User> users) {
        Department department;
        List<Department> list = new ArrayList<>();

        department = dataManager.create(Department.class);
        department.setName("Marketing");
        department.setManager(users.get(0));
        list.add(dataManager.save(department));

        department = dataManager.create(Department.class);
        department.setName("Operations");
        department.setManager(users.get(1));
        list.add(dataManager.save(department));

        return list;
    }

    private List<User> initUsers() {
        User user;
        SaveContext saveContext;
        List<User> list = new ArrayList<>();

        saveContext = new SaveContext();
        user = dataManager.create(User.class);
        user.setUsername("alice");
        user.setPassword(createPassword());
        user.setFirstName("Alice");
        user.setLastName("Brown");
        saveContext.saving(user);
        list.add(user);
        dataManager.save(saveContext);

        saveContext = new SaveContext();
        user = dataManager.create(User.class);
        user.setUsername("bob");
        user.setPassword(createPassword());
        user.setFirstName("Robert");
        user.setLastName("Taylor");
        saveContext.saving(user);
        list.add(user);
        dataManager.save(saveContext);

        return list;
    }

    private void assignRoles(List<User> users) {
        for (User user : users) {
            RoleAssignmentEntity roleAssignment;

            roleAssignment = dataManager.create(RoleAssignmentEntity.class);
            roleAssignment.setUsername(user.getUsername());
            roleAssignment.setRoleCode(UiMinimalRole.CODE);
            roleAssignment.setRoleType(RoleAssignmentRoleType.RESOURCE);
            dataManager.saveWithoutReload(roleAssignment);

            String roleCode = getUserRole(user);
            if (roleCode != null) {
                roleAssignment = dataManager.create(RoleAssignmentEntity.class);
                roleAssignment.setUsername(user.getUsername());
                roleAssignment.setRoleCode(roleCode);
                roleAssignment.setRoleType(RoleAssignmentRoleType.RESOURCE);
                dataManager.saveWithoutReload(roleAssignment);
            }
        }
    }

    private String getUserRole(User user) {
        return switch (user.getUsername()) {
            case "alice" -> ManagerRole.CODE;
            case "bob" -> EmployeeRole.CODE;
            default -> null;
        };

    }

    private String createPassword() {
        return passwordEncoder.encode("1");
    }
}