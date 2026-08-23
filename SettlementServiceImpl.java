package com.splitwise.backend.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.splitwise.backend.dto.SettlementRequest;
import com.splitwise.backend.dto.SettlementResponse;
import com.splitwise.backend.dto.UserResponse;
import com.splitwise.backend.entity.Group;
import com.splitwise.backend.entity.Settlement;
import com.splitwise.backend.entity.User;
import com.splitwise.backend.exception.BadRequestException;
import com.splitwise.backend.exception.ResourceNotFoundException;
import com.splitwise.backend.repository.GroupRepository;
import com.splitwise.backend.repository.SettlementRepository;
import com.splitwise.backend.repository.UserRepository;
import com.splitwise.backend.service.SettlementService;

@Service
@Transactional
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public SettlementServiceImpl(SettlementRepository settlementRepository,
                                    GroupRepository groupRepository,
                                    UserRepository userRepository) {
        this.settlementRepository = settlementRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Override
    public SettlementResponse createSettlement(Long groupId, SettlementRequest request) {
        Group group = findGroupOrThrow(groupId);

        if (request.getPaidById() == null || request.getPaidToId() == null) {
            throw new BadRequestException("paidById and paidToId are required");
        }
        if (request.getPaidById().equals(request.getPaidToId())) {
            throw new BadRequestException("paidById and paidToId cannot be the same user");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Settlement amount must be greater than zero");
        }

        Set<Long> groupMemberIds = group.getMembers().stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        User paidBy = findUserOrThrow(request.getPaidById());
        User paidTo = findUserOrThrow(request.getPaidToId());

        if (!groupMemberIds.contains(paidBy.getId()) || !groupMemberIds.contains(paidTo.getId())) {
            throw new BadRequestException("Both users must be members of this group");
        }

        Settlement settlement = new Settlement(group, paidBy, paidTo, request.getAmount());
        Settlement saved = settlementRepository.save(settlement);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlementsByGroup(Long groupId) {
        findGroupOrThrow(groupId);
        return settlementRepository.findByGroupId(groupId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Group findGroupOrThrow(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
    }

    private SettlementResponse toResponse(Settlement settlement) {
        UserResponse paidBy = new UserResponse(
                settlement.getPaidBy().getId(),
                settlement.getPaidBy().getName(),
                settlement.getPaidBy().getEmail());
        UserResponse paidTo = new UserResponse(
                settlement.getPaidTo().getId(),
                settlement.getPaidTo().getName(),
                settlement.getPaidTo().getEmail());

        return new SettlementResponse(
                settlement.getId(),
                settlement.getGroup().getId(),
                paidBy,
                paidTo,
                settlement.getAmount(),
                settlement.getSettledAt());
    }
}
