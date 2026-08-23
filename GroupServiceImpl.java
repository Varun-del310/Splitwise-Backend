    package com.splitwise.backend.service.impl;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.stream.Collectors;

    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import com.splitwise.backend.dto.AddMembersRequest;
    import com.splitwise.backend.dto.GroupCreateRequest;
    import com.splitwise.backend.dto.GroupResponse;
    import com.splitwise.backend.dto.UserResponse;
    import com.splitwise.backend.entity.Group;
    import com.splitwise.backend.entity.User;
    import com.splitwise.backend.exception.BadRequestException;
    import com.splitwise.backend.exception.ResourceNotFoundException;
    import com.splitwise.backend.repository.GroupRepository;
    import com.splitwise.backend.repository.UserRepository;
    import com.splitwise.backend.service.GroupService;

    @Service
    @Transactional
    public class GroupServiceImpl implements GroupService {

        private final GroupRepository groupRepository;
        private final UserRepository userRepository;

        public GroupServiceImpl(GroupRepository groupRepository, UserRepository userRepository) {
            this.groupRepository = groupRepository;
            this.userRepository = userRepository;
        }

        @Override
        public GroupResponse createGroup(GroupCreateRequest request) {
            if (request.getName() == null || request.getName().isBlank()) {
                throw new BadRequestException("Group name is required");
            }

            Group group = new Group(request.getName(), request.getDescription());

            if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
                for (Long memberId : request.getMemberIds()) {
                    User user = findUserOrThrow(memberId);
                    group.getMembers().add(user);
                }
            }

            Group saved = groupRepository.save(group);
            return toResponse(saved);
        }

        @Override
        public GroupResponse addMembers(Long groupId, AddMembersRequest request) {
            Group group = findGroupOrThrow(groupId);

            if (request.getMemberIds() == null || request.getMemberIds().isEmpty()) {
                throw new BadRequestException("At least one member id is required");
            }

            for (Long memberId : request.getMemberIds()) {
                User user = findUserOrThrow(memberId);
                group.getMembers().add(user);
            }

            Group saved = groupRepository.save(group);
            return toResponse(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public GroupResponse getGroup(Long groupId) {
            Group group = findGroupOrThrow(groupId);
            return toResponse(group);
        }

        private User findUserOrThrow(Long userId) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        }

        private Group findGroupOrThrow(Long groupId) {
            return groupRepository.findById(groupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        }

        private GroupResponse toResponse(Group group) {
            List<UserResponse> members = new ArrayList<>(group.getMembers()
                    .stream()
                    .map(u -> new UserResponse(u.getId(), u.getName(), u.getEmail()))
                    .collect(Collectors.toList()));

            return new GroupResponse(group.getId(), group.getName(), group.getDescription(), members);
        }
    }
