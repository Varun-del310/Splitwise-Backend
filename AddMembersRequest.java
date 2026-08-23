package com.splitwise.backend.dto;

import java.util.List;

public class AddMembersRequest {

    private List<Long> memberIds;

    public AddMembersRequest() {
    }

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }
}
