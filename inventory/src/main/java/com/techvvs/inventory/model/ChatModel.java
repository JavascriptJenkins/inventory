package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_model")
public class ChatModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "folder_path", nullable = false, unique = true)
    private String folderPath;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTimestamp;

    @Column(name = "updated_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private SystemUserDAO createdByUser;

    @OneToMany(mappedBy = "chatModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Chat> chats = new ArrayList<>();

    @Column(name = "mcp_connector_config", columnDefinition = "TEXT")
    private String mcpConnectorConfig;

    @Column(name = "model_type")
    private String modelType = "local"; // local, external, hybrid

    // Constructors
    public ChatModel() {
        this.createdTimestamp = LocalDateTime.now();
        this.updatedTimestamp = LocalDateTime.now();
    }

    public ChatModel(String name, String description, String folderPath, SystemUserDAO createdByUser) {
        this();
        this.name = name;
        this.description = description;
        this.folderPath = folderPath;
        this.createdByUser = createdByUser;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    public SystemUserDAO getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(SystemUserDAO createdByUser) {
        this.createdByUser = createdByUser;
    }

    public List<Chat> getChats() {
        return chats;
    }

    public void setChats(List<Chat> chats) {
        this.chats = chats;
    }

    public String getMcpConnectorConfig() {
        return mcpConnectorConfig;
    }

    public void setMcpConnectorConfig(String mcpConnectorConfig) {
        this.mcpConnectorConfig = mcpConnectorConfig;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    // Helper methods
    public void addChat(Chat chat) {
        chats.add(chat);
        chat.setChatModel(this);
        this.updatedTimestamp = LocalDateTime.now();
    }

    public void removeChat(Chat chat) {
        chats.remove(chat);
        chat.setChatModel(null);
        this.updatedTimestamp = LocalDateTime.now();
    }

    public String getFullFolderPath() {
        return "./uploads/chatmodel/" + this.folderPath + "/";
    }

    public String getMcpConnectorPath() {
        return "./uploads/chatmodel/" + this.folderPath + "/mcp-connector.dxt";
    }

    public int getChatCount() {
        return chats.size();
    }

    public int getActiveChatCount() {
        return (int) chats.stream().filter(Chat::getIsActive).count();
    }
}

