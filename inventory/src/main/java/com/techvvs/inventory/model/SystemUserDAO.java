package com.techvvs.inventory.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.techvvs.inventory.model.metrc.MetrcLicenseVO;
import com.techvvs.inventory.security.Role;
import com.techvvs.inventory.service.metrc.constants.LicenseType;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name="systemuser")
public class SystemUserDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    Integer id;

    @Column(name="password")
    String password;

    @Column(name="name")
    String name;

    @Column(name="tenant")
    String tenant;

    // Many-to-one relationship with Tenant entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", referencedColumnName = "id")
    @JsonIgnore
    private Tenant tenantEntity;

    @Transient
    String password2;

    @Column(name="email")
    String email;

    @Column(name="roles")
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    Role[] roles;

    @Column(name="phone")
    String phone;

    @Column(name="isuseractive")
    Integer isuseractive;

    // OAuth-related fields
    @Column(name="google_id", unique = true)
    String googleId;

    @Column(name="oauth_provider")
    String oauthProvider;

    @Column(name="oauth_email")
    String oauthEmail;

    @Column(name="created_via_oauth")
    Boolean createdViaOauth = false;

    @Column(name="oauth_linked")
    Boolean oauthLinked = false;

    @Column(name="ui_mode")
    String uiMode = "MODERN"; // Default to MODERN UI mode

    // each systemuser may administrate multiple metrc license's from a single TULIP account
    @OneToMany(mappedBy = "systemUserDAO", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<MetrcLicenseVO> metrcLicenseVOS;

    // each systemuser may have multiple chat sessions
    @OneToMany(mappedBy = "systemUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("updatedTimestamp DESC")
    @JsonIgnore
    List<Chat> chats;

    @JsonProperty
    LocalDateTime updatedtimestamp;
    @JsonProperty
    LocalDateTime createtimestamp;


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role[] getRoles() {
        return roles;
    }

    public void setRoles(Role[] roles) {
        this.roles = roles;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getIsuseractive() {
        return isuseractive;
    }

    public void setIsuseractive(Integer isuseractive) {
        this.isuseractive = isuseractive;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant( String tenant) {
        this.tenant = tenant;
    }

    public Tenant getTenantEntity() {
        return tenantEntity;
    }

    public void setTenantEntity(Tenant tenantEntity) {
        this.tenantEntity = tenantEntity;
    }



    public List<Chat> getChats() {
        return chats;
    }

    public void setChats(List<Chat> chats) {
        this.chats = chats;
    }

    // OAuth getters and setters
    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getOauthProvider() {
        return oauthProvider;
    }

    public void setOauthProvider(String oauthProvider) {
        this.oauthProvider = oauthProvider;
    }

    public String getOauthEmail() {
        return oauthEmail;
    }

    public void setOauthEmail(String oauthEmail) {
        this.oauthEmail = oauthEmail;
    }

    public Boolean getCreatedViaOauth() {
        return createdViaOauth;
    }

    public void setCreatedViaOauth(Boolean createdViaOauth) {
        this.createdViaOauth = createdViaOauth;
    }

    public Boolean getOauthLinked() {
        return oauthLinked;
    }

    public void setOauthLinked(Boolean oauthLinked) {
        this.oauthLinked = oauthLinked;
    }

    public String getUiMode() {
        return uiMode;
    }

    public void setUiMode(String uiMode) {
        this.uiMode = uiMode;
    }

    public List<LicenseType> getLicenseTypes() {
        if (metrcLicenseVOS == null) {
            return Collections.emptyList();
        }

        return metrcLicenseVOS.stream()
                .map(MetrcLicenseVO::getLicensetype)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .map(type -> {
                    try {
                        return LicenseType.valueOf(type);
                    } catch (IllegalArgumentException e) {
                        return null; // Skip unrecognized types
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }





}
