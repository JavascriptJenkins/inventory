package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="token")
public class TokenDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    Integer id;

    @Column(name="token", length = 1024)
    String token;

    @Column(name="usermetadata")
    String usermetadata;

    @Column(name="tokenused")
    Integer tokenused;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Transient
    String password; // used to pass the password in from the verify phone token html

    @Transient
    String email;

    @JsonProperty
    LocalDateTime updatedtimestamp;
    @JsonProperty
    LocalDateTime createtimestamp;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsermetadata() {
        return usermetadata;
    }

    public void setUsermetadata(String usermetadata) {
        this.usermetadata = usermetadata;
    }

    public Integer getTokenused() {
        return tokenused;
    }

    public void setTokenused(Integer tokenused) {
        this.tokenused = tokenused;
    }

    public LocalDateTime getUpdatedtimestamp() {
        return updatedtimestamp;
    }

    public void setUpdatedtimestamp(LocalDateTime updatedtimestamp) {
        this.updatedtimestamp = updatedtimestamp;
    }

    public LocalDateTime getCreatetimestamp() {
        return createtimestamp;
    }

    public void setCreatetimestamp(LocalDateTime createtimestamp) {
        this.createtimestamp = createtimestamp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}

//    CREATE TABLE token (
//        id INT AUTO_INCREMENT  PRIMARY KEY,
//        token VARCHAR(250) NOT NULL,
//    usermetadata VARCHAR(250) NOT NULL,
//    tokenused INT NOT NULL,
//    updatedtimestamp DATE NOT NULL,
//    createtimestamp DATE NOT NULL
//);
