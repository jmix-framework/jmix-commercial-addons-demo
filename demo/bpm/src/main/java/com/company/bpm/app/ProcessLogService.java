package com.company.bpm.app;

import com.company.bpm.entity.WorkspaceRequest;
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
        dataManager.saveWithoutReload(request);
    }

    public void logSoftwarePermissionsGranted(WorkspaceRequest request) {
        request.setProcessLog(getCurrentProcessLog(request) + getTimeStamp() + "Software permissions granted");
        dataManager.saveWithoutReload(request);
    }

    private String getTimeStamp() {
        return "[" + timeSource.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] ";
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