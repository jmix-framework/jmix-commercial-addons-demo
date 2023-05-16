package com.company.demo.app;

import com.company.demo.entity.User;
import com.company.demo.entity.WorkspaceRequest;
import io.jmix.notifications.NotificationManager;
import io.jmix.notifications.entity.ContentType;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class NotificationService {

    private final NotificationManager notificationManager;

    public NotificationService(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void notifyAboutPermissions(WorkspaceRequest request, User user) {
        String granted = request.getSoftwareRequests().stream()
                .filter(softwareRequest -> Boolean.TRUE.equals(softwareRequest.getPermissionGranted()))
                .map(softwareRequest -> softwareRequest.getSoftware().getName())
                .collect(Collectors.joining("<li>", "<ul>", "</ul>"));
        notificationManager.createNotification()
                .withSubject("Software access granted")
                .withRecipients(user)
                .toChannelsByNames("in-app")
                .withBody("You have been granted access to the following systems:" + granted)
                .withContentType(ContentType.HTML)
                .send();
    }

    public void notifyAboutWorkspace(User user, String workspaceDescription) {
        notificationManager.createNotification()
                .withSubject("Workspace description")
                .withRecipients(user)
                .toChannelsByNames("in-app")
                .withBody("Your workspace description: " + workspaceDescription)
                .withContentType(ContentType.PLAIN)
                .send();
    }
}