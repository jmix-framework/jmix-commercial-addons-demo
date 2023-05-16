package com.company.demo.init;

import com.company.demo.entity.Department;
import com.company.demo.entity.Software;
import com.company.demo.entity.User;
import com.company.demo.security.BasicAppRole;
import com.company.demo.security.CoordinatorRole;
import com.company.demo.security.HrManagerRole;
import com.company.demo.security.SystemAdministratorRole;
import io.jmix.bpm.entity.UserGroup;
import io.jmix.bpm.entity.UserGroupRole;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.security.Authenticated;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securitydata.entity.UserSubstitutionEntity;
import io.jmix.securityui.role.UiMinimalRole;
import org.flowable.engine.RepositoryService;
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
    private final RepositoryService repositoryService;

    public DemoDataInitializer(DataManager dataManager, PasswordEncoder passwordEncoder, RepositoryService repositoryService) {
        this.dataManager = dataManager;
        this.passwordEncoder = passwordEncoder;
        this.repositoryService = repositoryService;
    }

    @EventListener
    @Authenticated
    public void onApplicationStarted(ApplicationStartedEvent event) {
        if (dataManager.load(Department.class).all().maxResults(1).list().size() > 0) {
            return;
        }
        initSoftware();
        List<Department> departments = initDepartments();
        List<User> users = initUsers(departments);
        initUserSubstitutions();
        assignRoles(users);
        initBpmUserGroups();
        deployProcess();
    }

    private void initUserSubstitutions() {
        Arrays.asList("alice", "bob", "linda", "susan").forEach(name -> {
            UserSubstitutionEntity userSubstitution = dataManager.create(UserSubstitutionEntity.class);
            userSubstitution.setUsername("admin");
            userSubstitution.setSubstitutedUsername(name);
            dataManager.save(userSubstitution);
        });
    }

    private void initSoftware() {
        Arrays.asList("Timesheets", "Company portal", "Email", "Messenger", "Git").forEach(name -> {
            Software software = dataManager.create(Software.class);
            software.setName(name);
            dataManager.save(software);
        });
    }

    private List<Department> initDepartments() {
        Department department;
        List<Department> list = new ArrayList<>();

        department = dataManager.create(Department.class);
        department.setName("Human Resources");
        list.add(dataManager.save(department));

        department = dataManager.create(Department.class);
        department.setName("IT");
        list.add(dataManager.save(department));

        department = dataManager.create(Department.class);
        department.setName("Marketing");
        list.add(dataManager.save(department));

        department = dataManager.create(Department.class);
        department.setName("Operations");
        list.add(dataManager.save(department));

        return list;
    }

    private List<User> initUsers(List<Department> departments) {
        User user;
        SaveContext saveContext;
        List<User> list = new ArrayList<>();

        saveContext = new SaveContext();
        user = dataManager.create(User.class);
        user.setUsername("alice");
        user.setPassword(createPassword());
        user.setFirstName("Alice");
        user.setLastName("Brown");
        user.setDepartment(departments.get(0));
        saveContext.saving(user);
        list.add(user);
        dataManager.save(saveContext);

        saveContext = new SaveContext();
        user = dataManager.create(User.class);
        user.setUsername("bob");
        user.setPassword(createPassword());
        user.setFirstName("Robert");
        user.setLastName("Taylor");
        user.setDepartment(departments.get(1));
        saveContext.saving(user);
        list.add(user);
        dataManager.save(saveContext);

        Department operationsDept = departments.get(3);

        saveContext = new SaveContext();
        user = dataManager.create(User.class);
        user.setUsername("linda");
        user.setPassword(createPassword());
        user.setFirstName("Linda");
        user.setLastName("Evans");
        user.setDepartment(operationsDept);
        saveContext.saving(user);
        list.add(user);
        dataManager.save(saveContext);

        operationsDept.setCoordinator(user);
        dataManager.save(operationsDept);

        saveContext = new SaveContext();
        user = dataManager.create(User.class);
        user.setUsername("susan");
        user.setPassword(createPassword());
        user.setFirstName("Susan");
        user.setLastName("Baker");
        user.setDepartment(operationsDept);
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
            dataManager.save(roleAssignment);

            roleAssignment = dataManager.create(RoleAssignmentEntity.class);
            roleAssignment.setUsername(user.getUsername());
            roleAssignment.setRoleCode("notifications-in-app-notifications-user");
            roleAssignment.setRoleType(RoleAssignmentRoleType.RESOURCE);
            dataManager.save(roleAssignment);

            String roleCode = getUserRole(user);
            if (roleCode != null) {
                roleAssignment = dataManager.create(RoleAssignmentEntity.class);
                roleAssignment.setUsername(user.getUsername());
                roleAssignment.setRoleCode(roleCode);
                roleAssignment.setRoleType(RoleAssignmentRoleType.RESOURCE);
                dataManager.save(roleAssignment);
            }
        }
    }

    private String getUserRole(User user) {
        if (user.getUsername().equals("susan"))
            return BasicAppRole.CODE;

        if (user.getUsername().equals("alice"))
            return HrManagerRole.CODE;

        if (user.getUsername().equals("bob"))
            return SystemAdministratorRole.CODE;

        if (user.equals(user.getDepartment().getCoordinator()))
            return CoordinatorRole.CODE;

        return null;
    }

    private void initBpmUserGroups() {
        UserGroup userGroup;
        UserGroupRole userGroupRole;

        userGroup = dataManager.create(UserGroup.class);
        userGroup.setName("HR Managers");
        userGroup.setCode("hr-managers");

        userGroupRole = dataManager.create(UserGroupRole.class);
        userGroupRole.setUserGroup(userGroup);
        userGroupRole.setRoleCode(HrManagerRole.CODE);

        dataManager.save(userGroup, userGroupRole);

        userGroup = dataManager.create(UserGroup.class);
        userGroup.setName("System Administrators");
        userGroup.setCode("system-administrators");

        userGroupRole = dataManager.create(UserGroupRole.class);
        userGroupRole.setUserGroup(userGroup);
        userGroupRole.setRoleCode(SystemAdministratorRole.CODE);

        dataManager.save(userGroup, userGroupRole);
    }

    private void deployProcess() {
        repositoryService.createDeployment()
                .addClasspathResource("process-drafts/workspace-preparation.bpmn20.xml")
                .deploy();
    }

    private String createPassword() {
        return passwordEncoder.encode("1");
    }
}