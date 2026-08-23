package com.splitwise.backend.dto;

import java.util.List;

public class GroupResponse {

    private Long id;
    private String name;
    private String description;
    private List<UserResponse> members;

    public GroupResponse() {
    }

    public GroupResponse(Long id, String name, String description, List<UserResponse> members) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.members = members;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public List<UserResponse> getMembers() {
        return members;
    }

    public void setMembers(List<UserResponse> members) {
        this.members = members;
    }
}
