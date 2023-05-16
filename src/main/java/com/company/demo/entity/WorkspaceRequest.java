package com.company.demo.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "WORKSPACE_REQUEST", indexes = {
        @Index(name = "IDX_WORKSPACEREQUEST_HRMANAGER", columnList = "HR_MANAGER_ID"),
        @Index(name = "IDX_WORKSPACE_REQUEST_EMPLOYEE", columnList = "EMPLOYEE_ID")
})
@Entity
public class WorkspaceRequest {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;

    @Column(name = "DATE_", nullable = false)
    @NotNull
    private LocalDate date;

    @JoinColumn(name = "HR_MANAGER_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User hrManager;

    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User employee;

    @Column(name = "WORK_TYPE", nullable = false, length = 1)
    @NotNull
    private String workType;

    @OrderBy("sortValue")
    @Composition
    @OneToMany(mappedBy = "workspaceRequest")
    private List<SoftwareRequest> softwareRequests;

    @Column(name = "PROCESS_LOG")
    @Lob
    private String processLog;

    public String getProcessLog() {
        return processLog;
    }

    public void setProcessLog(String processLog) {
        this.processLog = processLog;
    }

    public List<SoftwareRequest> getSoftwareRequests() {
        return softwareRequests;
    }

    public void setSoftwareRequests(List<SoftwareRequest> softwareRequests) {
        this.softwareRequests = softwareRequests;
    }

    public WorkType getWorkType() {
        return workType == null ? null : WorkType.fromId(workType);
    }

    public void setWorkType(WorkType workType) {
        this.workType = workType == null ? null : workType.getId();
    }

    public User getEmployee() {
        return employee;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    public User getHrManager() {
        return hrManager;
    }

    public void setHrManager(User hrManager) {
        this.hrManager = hrManager;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}