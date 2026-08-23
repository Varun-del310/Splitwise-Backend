package com.splitwise.backend.service;

import com.splitwise.backend.dto.UserRequest;
import com.splitwise.backend.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserResponse getUser(Long userId);

    List<UserResponse> getAllUsers();
}
