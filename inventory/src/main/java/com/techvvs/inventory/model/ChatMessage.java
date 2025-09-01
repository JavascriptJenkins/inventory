package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_user_message")
    private Boolean isUserMessage;

    @Column(name = "created_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    @JsonIgnore
    private Chat chat;

    // Additional fields for rich response data
    @Column(name = "states", columnDefinition = "TEXT")
    private String states; // JSON array of states

    @Column(name = "endpoints", columnDefinition = "TEXT")
    private String endpoints; // JSON array of endpoints

    @Column(name = "sources", columnDefinition = "TEXT")
    private String sources; // JSON array of sources

    @Column(name = "query")
    private String query;

    // Constructors
    public ChatMessage() {
        this.createdTimestamp = LocalDateTime.now();
    }

    public ChatMessage(String content, Boolean isUserMessage, Chat chat) {
        this();
        this.content = content;
        this.isUserMessage = isUserMessage;
        this.chat = chat;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsUserMessage() {
        return isUserMessage;
    }

    public void setIsUserMessage(Boolean isUserMessage) {
        this.isUserMessage = isUserMessage;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public String getStates() {
        return states;
    }

    public void setStates(String states) {
        this.states = states;
    }

    public String getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(String endpoints) {
        this.endpoints = endpoints;
    }

    public String getSources() {
        return sources;
    }

    public void setSources(String sources) {
        this.sources = sources;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    // Helper methods
    public String getFormattedTimestamp() {
        return createdTimestamp.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public String getSenderName() {
        return isUserMessage ? "You" : "Bot";
    }
}
