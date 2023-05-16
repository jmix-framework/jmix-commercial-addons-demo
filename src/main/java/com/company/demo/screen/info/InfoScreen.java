package com.company.demo.screen.info;

import com.company.demo.entity.User;
import com.company.demo.security.CoordinatorRole;
import com.company.demo.security.FullAccessRole;
import com.company.demo.security.HrManagerRole;
import com.company.demo.security.SystemAdministratorRole;
import io.jmix.core.MetadataTools;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.ui.component.Label;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

@UiController("InfoScreen")
@UiDescriptor("info-screen.xml")
public class InfoScreen extends Screen {
    @Autowired
    private Label<String> infoLab;
    @Autowired
    private CurrentUserSubstitution currentUserSubstitution;
    @Autowired
    private MessageBundle messageBundle;
    @Autowired
    private MetadataTools metadataTools;

    @Subscribe
    public void onInit(InitEvent event) {
        StringBuilder sb = new StringBuilder();

        User user = (User) currentUserSubstitution.getEffectiveUser();
        sb.append(messageBundle.formatMessage("user", metadataTools.getInstanceName(user)));

        String key = "role-";
        user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .forEach(role -> {
                    String message = messageBundle.formatMessage(key + role);
                    if (!message.equals(key + role))
                        sb.append(message);
                });

        sb.append(messageBundle.getMessage("end-note"));

        infoLab.setValue(sb.toString());
    }
}