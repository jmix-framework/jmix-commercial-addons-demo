package com.company.aichat.entity;

import io.jmix.aichat.data.AiChatMessage;
import io.jmix.aichat.entity.AiMessageRole;
import io.jmix.core.FileRef;
import io.jmix.core.entity.FileRefConverter;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "CHAT_MESSAGE")
@Entity
public class ChatMessage implements AiChatMessage {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    @Column(name = "ATTACHMENTS", length = 1024)
    @Convert(converter = FileRefConverter.class)
    @OrderColumn(name = "ATTACHMENTS_ORDER")
    @ElementCollection
    @CollectionTable(name = "CHAT_MESSAGE_ATTACHMENTS", joinColumns = @JoinColumn(name = "CHAT_MESSAGE_ID"))
    private List<FileRef> attachments;

    @Column(name = "CONTENT")
    @Lob
    private String content;

    @Column(name = "ROLE")
    private String role;

    @OneToMany(mappedBy = "chatMessage")
    private List<MessageFeedback> feedback;

    public List<MessageFeedback> getFeedback() {
        return feedback;
    }

    public void setFeedback(List<MessageFeedback> feedback) {
        this.feedback = feedback;
    }

    @Override
    public AiMessageRole getRole() {
        return role == null ? null : AiMessageRole.fromId(role);
    }

    public void setRole(AiMessageRole role) {
        this.role = role == null ? null : role.getId();
    }

    @Override
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public List<FileRef> getAttachments() {
        // The AiChatMessage contract requires a non-null list; an empty @ElementCollection
        // reloads from the database as null, so normalize it here.
        return attachments != null ? attachments : List.of();
    }

    public void setAttachments(List<FileRef> attachments) {
        this.attachments = attachments;
    }

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Optional creation timestamp.
     *
     * @return the timestamp, or {@code null} if none
     */
    @Override
    public @Nullable Instant getTime() {
        return createdDate != null ? createdDate.toInstant() : null;
    }
}
