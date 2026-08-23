package com.splitwise.backend.service.impl;

import com.splitwise.backend.dto.ExpenseCreateRequest;
import com.splitwise.backend.dto.ExpenseParticipantResponse;
import com.splitwise.backend.dto.ExpenseResponse;
import com.splitwise.backend.dto.UserResponse;
import com.splitwise.backend.entity.Expense;
import com.splitwise.backend.entity.ExpenseParticipant;
import com.splitwise.backend.entity.Group;
import com.splitwise.backend.entity.User;
import com.splitwise.backend.exception.BadRequestException;
import com.splitwise.backend.exception.ResourceNotFoundException;
import com.splitwise.backend.repository.ExpenseRepository;
import com.splitwise.backend.repository.GroupRepository;
import com.splitwise.backend.repository.UserRepository;
import com.splitwise.backend.service.ExpenseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository,
                                GroupRepository groupRepository,
                                UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ExpenseResponse createExpense(Long groupId, ExpenseCreateRequest request) {
        Group group = findGroupOrThrow(groupId);

        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new BadRequestException("Expense description is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Expense amount must be greater than zero");
        }
        if (request.getPaidById() == null) {
            throw new BadRequestException("paidById is required");
        }
        if (request.getParticipantIds() == null || request.getParticipantIds().isEmpty()) {
            throw new BadRequestException("At least one participant is required");
        }

        Set<Long> groupMemberIds = group.getMembers().stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        User paidBy = findUserOrThrow(request.getPaidById());
        if (!groupMemberIds.contains(paidBy.getId())) {
            throw new BadRequestException("paidBy user is not a member of this group");
        }

        // De-duplicate participant ids while preserving order
        Set<Long> participantIds = new LinkedHashSet<>(request.getParticipantIds());

        List<User> participantUsers = new ArrayList<>();
        for (Long participantId : participantIds) {
            if (!groupMemberIds.contains(participantId)) {
                throw new BadRequestException("Participant with id " + participantId + " is not a member of this group");
            }
            participantUsers.add(findUserOrThrow(participantId));
        }

        Expense expense = new Expense(request.getDescription(), request.getAmount(), group, paidBy);
        expense.setParticipants(buildEqualSplitParticipants(expense, participantUsers, request.getAmount()));

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByGroup(Long groupId) {
        findGroupOrThrow(groupId);
        return expenseRepository.findByGroupId(groupId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Splits the total amount equally among all participants.
     * Because division may not be exact, any rounding remainder (a few cents)
     * is added to the first participant's share so that the sum of all
     * shares always equals the original expense amount exactly.
     */
    private List<ExpenseParticipant> buildEqualSplitParticipants(Expense expense, List<User> participants, BigDecimal amount) {
        int participantCount = participants.size();
        BigDecimal equalShare = amount.divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.HALF_UP);
        BigDecimal totalAssigned = equalShare.multiply(BigDecimal.valueOf(participantCount));
        BigDecimal remainder = amount.subtract(totalAssigned);

        List<ExpenseParticipant> result = new ArrayList<>();
        for (int i = 0; i < participantCount; i++) {
            BigDecimal share = equalShare;
            if (i == 0) {
                share = share.add(remainder);
            }
            result.add(new ExpenseParticipant(expense, participants.get(i), share));
        }
        return result;
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Group findGroupOrThrow(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
    }

    private ExpenseResponse toResponse(Expense expense) {
        UserResponse paidBy = new UserResponse(
                expense.getPaidBy().getId(),
                expense.getPaidBy().getName(),
                expense.getPaidBy().getEmail());

        List<ExpenseParticipantResponse> participants = expense.getParticipants().stream()
                .map(p -> new ExpenseParticipantResponse(p.getUser().getId(), p.getUser().getName(), p.getShareAmount()))
                .collect(Collectors.toList());

        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getGroup().getId(),
                paidBy,
                participants,
                expense.getCreatedAt());
    }
}
