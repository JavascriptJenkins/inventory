package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "created_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTimestamp;

    @Column(name = "updated_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_user_id")
    private SystemUserDAO systemUser;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("createdTimestamp ASC")
    @JsonIgnore
    private List<ChatMessage> messages = new ArrayList<>();

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Constructors
    public Chat() {
        this.createdTimestamp = LocalDateTime.now();
        this.updatedTimestamp = LocalDateTime.now();
    }

    public Chat(String title, SystemUserDAO systemUser) {
        this();
        this.title = title;
        this.systemUser = systemUser;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public LocalDateTime getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(LocalDateTime updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public SystemUserDAO getSystemUser() {
        return systemUser;
    }

    public void setSystemUser(SystemUserDAO systemUser) {
        this.systemUser = systemUser;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // Helper methods
    public void addMessage(ChatMessage message) {
        messages.add(message);
        message.setChat(this);
        this.updatedTimestamp = LocalDateTime.now();
    }

    public void removeMessage(ChatMessage message) {
        messages.remove(message);
        message.setChat(null);
        this.updatedTimestamp = LocalDateTime.now();
    }

    public String getLastMessagePreview() {
        if (messages.isEmpty()) {
            return "No messages yet";
        }
        String lastMessage = messages.get(messages.size() - 1).getContent();
        return lastMessage.length() > 50 ? lastMessage.substring(0, 50) + "..." : lastMessage;
    }

    public int getMessageCount() {
        return messages.size();
    }
}
