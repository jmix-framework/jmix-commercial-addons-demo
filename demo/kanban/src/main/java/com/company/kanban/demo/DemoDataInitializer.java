package com.company.kanban.demo;

import com.company.kanban.entity.KanbanTask;
import com.company.kanban.entity.User;
import com.company.kanban.security.FullAccessRole;
import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.security.Authenticated;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.company.kanban.entity.TaskStatus;

@Component
public class DemoDataInitializer {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorage fileStorage;

    @EventListener
    @Authenticated
    public void onApplicationStarted(ApplicationStartedEvent event) {
        if (dataManager.load(KanbanTask.class).all().maxResults(1).list().isEmpty()) {
            List<User> users = initUsers();
            assignRoles(users);
            generateTasks(users);
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
        user.setPicture(uploadPicture("com/company/kanban/demo/", "alice.png"));
        dataManager.saveWithoutReload(user);
        list.add(user);

        user = dataManager.create(User.class);
        user.setUsername("james");
        user.setPassword(createPassword());
        user.setFirstName("James");
        user.setLastName("Wilson");
        user.setPicture(uploadPicture("com/company/kanban/demo/", "james.png"));
        dataManager.saveWithoutReload(user);
        list.add(user);

        user = dataManager.create(User.class);
        user.setUsername("mary");
        user.setPassword(createPassword());
        user.setFirstName("Mary");
        user.setLastName("Jones");
        user.setPicture(uploadPicture("com/company/kanban/demo/", "mary.png"));
        dataManager.saveWithoutReload(user);
        list.add(user);

        user = dataManager.create(User.class);
        user.setUsername("linda");
        user.setPassword(createPassword());
        user.setFirstName("Linda");
        user.setLastName("Evans");
        user.setPicture(uploadPicture("com/company/kanban/demo/", "linda.png"));
        dataManager.saveWithoutReload(user);
        list.add(user);

        user = dataManager.create(User.class);
        user.setUsername("susan");
        user.setPassword(createPassword());
        user.setFirstName("Susan");
        user.setLastName("Baker");
        user.setPicture(uploadPicture("com/company/kanban/demo/", "susan.png"));
        dataManager.saveWithoutReload(user);
        list.add(user);

        user = dataManager.create(User.class);
        user.setUsername("bob");
        user.setPassword(createPassword());
        user.setFirstName("Robert");
        user.setLastName("Taylor");
        user.setPicture(uploadPicture("com/company/kanban/demo/", "bob.png"));
        dataManager.saveWithoutReload(user);
        list.add(user);

        return list;
    }

    private FileRef uploadPicture(String path, String fileName) {
        ClassPathResource resource = new ClassPathResource(path + fileName);
        try (InputStream stream = resource.getInputStream()) {
            return fileStorage.saveStream(fileName, stream);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read resource: " + path + fileName, e);
        }
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

    private void generateTasks(List<User> users) {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);

            if (i <= 1) {
                KanbanTask task1 = dataManager.create(KanbanTask.class);
                task1.setText("Design UI mockups");
                task1.setStatus(TaskStatus.DONE);
                task1.setAssignee(user);
                task1.setDueDate(LocalDate.now());
                task1.setPriority("High");
                task1.setProgress(100);
                task1.setColor("#E63946");
                task1.setTags("design,ui");
                dataManager.saveWithoutReload(task1);

                KanbanTask task2 = dataManager.create(KanbanTask.class);
                task2.setText("Implement backend API");
                task2.setStatus(TaskStatus.IN_PROGRESS);
                task2.setAssignee(user);
                task2.setDueDate(LocalDate.now().plusDays(2));
                task2.setPriority("Medium");
                task2.setProgress(50);
                task2.setColor("#4CAF50");
                task2.setTags("backend,api");
                dataManager.saveWithoutReload(task2);

            } else if (i == 3) {
                KanbanTask task3 = dataManager.create(KanbanTask.class);
                task3.setText("Write unit tests");
                task3.setStatus(TaskStatus.VERIFICATION);
                task3.setAssignee(user);
                task3.setDueDate(LocalDate.now().plusDays(5));
                task3.setPriority("Low");
                task3.setProgress(75);
                task3.setColor("#2196F3");
                task3.setTags("testing");
                dataManager.saveWithoutReload(task3);

            } else if (i == 4) {
                KanbanTask task4 = dataManager.create(KanbanTask.class);
                task4.setText("Deploy to production");
                task4.setStatus(TaskStatus.TODO);
                task4.setAssignee(user);
                task4.setDueDate(LocalDate.now().plusDays(10));
                task4.setPriority("Medium");
                task4.setProgress(0);
                task4.setColor("#FFC107");
                task4.setTags("devops");
                dataManager.saveWithoutReload(task4);
            }
        }
    }
}