package com.company.demo.app;

import com.company.demo.entity.WorkspaceRequest;
import io.jmix.core.DataManager;
import io.jmix.core.TimeSource;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ProcessLogService {

    private final DataManager dataManager;
    private final TimeSource timeSource;

    public ProcessLogService(DataManager dataManager, TimeSource timeSource) {
        this.dataManager = dataManager;
        this.timeSource = timeSource;
    }

    public void logWorkspaceDescription(WorkspaceRequest request, String workspaceDescription) {
        request.setProcessLog(getCurrentProcessLog(request) + getTimeStamp() + "Workspace description: " + workspaceDescription);
        dataManager.save(request);
    }

    public void logSoftwarePermissionsGranted(WorkspaceRequest request) {
        request.setProcessLog(getCurrentProcessLog(request) + getTimeStamp() + "Software permissions granted");
        dataManager.save(request);
    }

    private String getTimeStamp() {
        return "[" + timeSource.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + "] ";
    }

    private static String getCurrentProcessLog(WorkspaceRequest request) {
        String processLog = request.getProcessLog();
        if (processLog == null)
            processLog = "";
        else
            processLog += "\n";
        return processLog;
    }
}