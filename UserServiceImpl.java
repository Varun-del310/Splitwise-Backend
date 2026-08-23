package com.splitwise.backend.service.impl;

import com.splitwise.backend.dto.UserRequest;
import com.splitwise.backend.dto.UserResponse;
import com.splitwise.backend.entity.User;
import com.splitwise.backend.exception.BadRequestException;
import com.splitwise.backend.exception.ResourceNotFoundException;
import com.splitwise.backend.repository.UserRepository;
import com.splitwise.backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("User name is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("User email is required");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("A user with this email already exists: " + request.getEmail());
        }

        User user = new User(request.getName(), request.getEmail());
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        User user = findUserOrThrow(userId);
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
