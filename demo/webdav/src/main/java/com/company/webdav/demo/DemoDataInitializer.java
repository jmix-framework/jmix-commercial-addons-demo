package com.company.webdav.demo;

import com.company.webdav.entity.Article;
import com.company.webdav.entity.Department;
import com.company.webdav.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.SaveContext;
import io.jmix.core.security.Authenticated;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.webdav.entity.WebdavDocument;
import io.jmix.webdav.service.WebdavDocumentsManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class DemoDataInitializer {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorage fileStorage;

    @Autowired
    private WebdavDocumentsManagementService webdavDocumentsManagementService;

    @EventListener
    @Authenticated
    public void onApplicationStarted(ApplicationStartedEvent event) {
        if (!dataManager.load(Department.class).all().maxResults(1).list().isEmpty()) {
            return;
        }
        List<Department> departments = initDepartments();
        initArticles(departments);
        List<User> users = initUsers(departments);
        assignRoles(users);
    }

    private List<Department> initDepartments() {
        Department department;
        List<Department> list = new ArrayList<>();

        department = dataManager.create(Department.class);
        department.setName("Human Resources");
        list.add(dataManager.save(department));

        department = dataManager.create(Department.class);
        department.setName("Marketing");
        list.add(dataManager.save(department));

        department = dataManager.create(Department.class);
        department.setName("Operations");
        list.add(dataManager.save(department));

        return list;
    }

    private void initArticles(List<Department> departments) {
        WebdavDocument webdavDocument;
        Article article;

        webdavDocument = webdavDocumentsManagementService.createVersioningDocumentByFileRef(
                uploadFile("HR_Employee_Onboarding_Guide.docx"));
        article = dataManager.create(Article.class);
        article.setDescription("Employee Onboarding Guide");
        article.setDepartment(departments.get(0));
        article.setWebdavDocument(webdavDocument);
        dataManager.saveWithoutReload(article);

        webdavDocument = webdavDocumentsManagementService.createVersioningDocumentByFileRef(
                uploadFile("HR_Leave_Tracker.xlsx"));
        article = dataManager.create(Article.class);
        article.setDescription("Leave Tracker");
        article.setDepartment(departments.get(0));
        article.setWebdavDocument(webdavDocument);
        dataManager.saveWithoutReload(article);

        webdavDocument = webdavDocumentsManagementService.createVersioningDocumentByFileRef(
                uploadFile("Marketing_Brand_Guidelines.docx"));
        article = dataManager.create(Article.class);
        article.setDescription("Brand Guidelines");
        article.setDepartment(departments.get(1));
        article.setWebdavDocument(webdavDocument);
        dataManager.saveWithoutReload(article);

        webdavDocument = webdavDocumentsManagementService.createVersioningDocumentByFileRef(
                uploadFile("Marketing_Campaign_Performance.xlsx"));
        article = dataManager.create(Article.class);
        article.setDescription("Campaign Performance");
        article.setDepartment(departments.get(1));
        article.setWebdavDocument(webdavDocument);
        dataManager.saveWithoutReload(article);

        webdavDocument = webdavDocumentsManagementService.createVersioningDocumentByFileRef(
                uploadFile("Operations_Inventory_Tracker.xlsx"));
        article = dataManager.create(Article.class);
        article.setDescription("Inventory Tracker");
        article.setDepartment(departments.get(2));
        article.setWebdavDocument(webdavDocument);
        dataManager.saveWithoutReload(article);

        webdavDocument = webdavDocumentsManagementService.createVersioningDocumentByFileRef(
                uploadFile("Operations_Vendor_Management_SOP.docx"));
        article = dataManager.create(Article.class);
        article.setDescription("Vendor Management SOP");
        article.setDepartment(departments.get(2));
        article.setWebdavDocument(webdavDocument);
        dataManager.saveWithoutReload(article);
    }

    private FileRef uploadFile(String fileName) {
        ClassPathResource resource = new ClassPathResource("com/company/webdav/demo/" + fileName);
        try (InputStream stream = resource.getInputStream()) {
            return fileStorage.saveStream(fileName, stream);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read resource: " + fileName, e);
        }
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
        user.setDepartment(departments.get(2));
        saveContext.saving(user);
        list.add(user);
        dataManager.save(saveContext);

        saveContext = new SaveContext();
        user = dataManager.create(User.class);
        user.setUsername("james");
        user.setPassword(createPassword());
        user.setFirstName("James");
        user.setLastName("Wilson");
        user.setDepartment(departments.get(1));
        saveContext.saving(user);
        list.add(user);
        dataManager.save(saveContext);

        return list;
    }

    private String createPassword() {
        return passwordEncoder.encode("1");
    }

    private void assignRoles(List<User> users) {
        for (User user : users) {
            RoleAssignmentEntity roleAssignment;

            roleAssignment = dataManager.create(RoleAssignmentEntity.class);
            roleAssignment.setUsername(user.getUsername());
            roleAssignment.setRoleCode("ui-minimal");
            roleAssignment.setRoleType(RoleAssignmentRoleType.RESOURCE);
            dataManager.saveWithoutReload(roleAssignment);

            roleAssignment = dataManager.create(RoleAssignmentEntity.class);
            roleAssignment.setUsername(user.getUsername());
            roleAssignment.setRoleCode("employee");
            roleAssignment.setRoleType(RoleAssignmentRoleType.RESOURCE);
            dataManager.saveWithoutReload(roleAssignment);
        }
    }
}