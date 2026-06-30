package com.company.tabbedmode.demo;

import com.company.tabbedmode.entity.*;
import com.company.tabbedmode.security.FullAccessRole;
import io.jmix.core.DataManager;
import io.jmix.core.security.Authenticated;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DemoDataInitializer {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @EventListener
    @Authenticated
    public void onApplicationStarted(ApplicationStartedEvent event) {
        if (dataManager.load(Project.class).all().maxResults(1).list().isEmpty()) {
            List<User> users = initUsers();
            assignRoles(users);
            generateProjects();
        }
    }

    private List<User> initUsers() {
        User user;
        List<User> list = new ArrayList<>();

        user = dataManager.create(User.class);
        user.setUsername("alice");
        user.setPassword(createPassword());
        user.setFirstName("Alice");
        user.setLastName("Brown");
        dataManager.saveWithoutReload(user);
        list.add(user);

        user = dataManager.create(User.class);
        user.setUsername("james");
        user.setPassword(createPassword());
        user.setFirstName("James");
        user.setLastName("Wilson");
        dataManager.saveWithoutReload(user);
        list.add(user);

        user = dataManager.create(User.class);
        user.setUsername("mary");
        user.setPassword(createPassword());
        user.setFirstName("Mary");
        user.setLastName("Jones");
        dataManager.saveWithoutReload(user);
        list.add(user);

        user = dataManager.create(User.class);
        user.setUsername("linda");
        user.setPassword(createPassword());
        user.setFirstName("Linda");
        user.setLastName("Evans");
        dataManager.saveWithoutReload(user);
        list.add(user);

        user = dataManager.create(User.class);
        user.setUsername("susan");
        user.setPassword(createPassword());
        user.setFirstName("Susan");
        user.setLastName("Baker");
        dataManager.saveWithoutReload(user);
        list.add(user);

        user = dataManager.create(User.class);
        user.setUsername("bob");
        user.setPassword(createPassword());
        user.setFirstName("Robert");
        user.setLastName("Taylor");
        dataManager.saveWithoutReload(user);
        list.add(user);

        return list;
    }

    private String createPassword() {
        return passwordEncoder.encode("1");
    }

    private void assignRoles(List<User> users) {
        for (User user : users) {
            RoleAssignmentEntity roleAssignment = dataManager.create(RoleAssignmentEntity.class);
            roleAssignment.setUsername(user.getUsername());
            roleAssignment.setRoleCode(FullAccessRole.CODE);
            roleAssignment.setRoleType(RoleAssignmentRoleType.RESOURCE);
            dataManager.saveWithoutReload(roleAssignment);
        }
    }

    private void generateProjects() {
        createProject("Petclinic", "Petclinic is a simple demo application built with Jmix. " +
                "It is based on the commonly known Spring Petclinic example.");

        createProject("Book Store", "Bookstore is a great example of the advanced capabilities " +
                "that Jmix provides to application developers.");

        createProject("Wind Turbines", "Wind Turbines application is a practical example of how " +
                "to build mobile-optimized business applications with Jmix.");
    }

    private void createProject(String name, String description) {
        Project project = dataManager.create(Project.class);
        project.setName(name);
        project.setDescription(description);
        project.setStatus(ProjectStatus.OPEN);

        project = dataManager.save(project);

        createTasks(project);
    }

    private void createTasks(Project project) {
        createTask("Development", "Development", project);
        createTask("Testing", "Testing", project);
        createTask("Analysis", "Analysis", project);
    }

    private void createTask(String name, String description, Project project) {
        Task task = dataManager.create(Task.class);
        task.setName(name);
        task.setDescription(description);
        task.setStatus(TaskStatus.ACTIVE);
        task.setProject(project);
        dataManager.saveWithoutReload(task);
    }
}