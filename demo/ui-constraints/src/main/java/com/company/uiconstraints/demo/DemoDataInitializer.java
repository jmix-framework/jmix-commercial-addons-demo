package com.company.uiconstraints.demo;

import com.company.uiconstraints.entity.Customer;
import com.company.uiconstraints.entity.User;
import com.company.uiconstraints.security.EmployeeRole;
import com.company.uiconstraints.security.UiMinimalRole;
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
        if (dataManager.load(User.class).all().maxResults(2).list().size() <= 1) {
            List<User> users = initUsers();
            assignRoles(users);
            initCustomers();
        }
    }

    private List<User> initUsers() {
        User user;
        List<User> list = new ArrayList<>();

        user = dataManager.create(User.class);
        user.setUsername("bob");
        user.setPassword(createPassword());
        user.setFirstName("Robert");
        user.setLastName("Taylor");
        dataManager.saveWithoutReload(user);
        list.add(user);

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

            roleAssignment = dataManager.create(RoleAssignmentEntity.class);
            roleAssignment.setUsername(user.getUsername());
            roleAssignment.setRoleCode(EmployeeRole.CODE);
            roleAssignment.setRoleType(RoleAssignmentRoleType.RESOURCE);
            dataManager.saveWithoutReload(roleAssignment);

            roleAssignment = dataManager.create(RoleAssignmentEntity.class);
            roleAssignment.setUsername(user.getUsername());
            roleAssignment.setRoleCode("adhoc-employee-role");
            roleAssignment.setRoleType(RoleAssignmentRoleType.RESOURCE);
            dataManager.saveWithoutReload(roleAssignment);
        }
    }

    private void initCustomers() {
        Customer customer1 = dataManager.create(Customer.class);
        customer1.setName("Horizon Technologies Inc.");
        customer1.setEmail("contact@horizontech.com");
        customer1.setComments("Enterprise client since 2018. Interested in expanding their license for the new department. Follow up in Q3.");
        dataManager.saveWithoutReload(customer1);

        Customer customer2 = dataManager.create(Customer.class);
        customer2.setName("Pinnacle Solutions Group");
        customer2.setEmail("info@pinnaclesg.com");
        customer2.setComments("Small business client. Had technical issues with the latest update - resolved on 2023-05-15. Regular maintenance contract.");
        dataManager.saveWithoutReload(customer2);

        Customer customer3 = dataManager.create(Customer.class);
        customer3.setName("Evergreen Manufacturing Ltd.");
        customer3.setEmail("support@evergreenmanuf.com");
        customer3.setComments("International client. Requires special pricing considerations. Has multiple deployment environments.");
        dataManager.saveWithoutReload(customer3);
    }

    private String createPassword() {
        return passwordEncoder.encode("1");
    }
}
