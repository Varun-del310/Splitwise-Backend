package com.splitwise.backend.service;

import com.splitwise.backend.dto.AddMembersRequest;
import com.splitwise.backend.dto.GroupCreateRequest;
import com.splitwise.backend.dto.GroupResponse;

public interface GroupService {

    GroupResponse createGroup(GroupCreateRequest request);

    GroupResponse addMembers(Long groupId, AddMembersRequest request);

    GroupResponse getGroup(Long groupId);
}
