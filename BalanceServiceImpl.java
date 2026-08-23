package com.splitwise.backend.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.splitwise.backend.dto.DebtResponse;
import com.splitwise.backend.dto.GroupBalanceResponse;
import com.splitwise.backend.dto.UserBalance;
import com.splitwise.backend.entity.Expense;
import com.splitwise.backend.entity.ExpenseParticipant;
import com.splitwise.backend.entity.Group;
import com.splitwise.backend.entity.Settlement;
import com.splitwise.backend.entity.User;
import com.splitwise.backend.exception.ResourceNotFoundException;
import com.splitwise.backend.repository.ExpenseRepository;
import com.splitwise.backend.repository.GroupRepository;
import com.splitwise.backend.repository.SettlementRepository;
import com.splitwise.backend.service.BalanceService;

@Service
@Transactional(readOnly = true)
public class BalanceServiceImpl implements BalanceService {

    private static final BigDecimal EPSILON = new BigDecimal("0.01");

    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;

    public BalanceServiceImpl(GroupRepository groupRepository,
                                ExpenseRepository expenseRepository,
                                SettlementRepository settlementRepository) {
        this.groupRepository = groupRepository;
        this.expenseRepository = expenseRepository;
        this.settlementRepository = settlementRepository;
    }

    @Override
    public GroupBalanceResponse getGroupBalances(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Initialize net balance of every group member to zero.
        Map<Long, BigDecimal> netBalance = new LinkedHashMap<>();
        Map<Long, User> usersById = new LinkedHashMap<>();
        for (User member : group.getMembers()) {
            netBalance.put(member.getId(), BigDecimal.ZERO);
            usersById.put(member.getId(), member);
        }

        // Apply each expense: payer is credited the full amount,
        // every participant (including the payer, if applicable) is debited their share.
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        for (Expense expense : expenses) {
            Long paidById = expense.getPaidBy().getId();
            netBalance.merge(paidById, expense.getAmount(), BigDecimal::add);
            usersById.putIfAbsent(paidById, expense.getPaidBy());

            for (ExpenseParticipant participant : expense.getParticipants()) {
                Long participantId = participant.getUser().getId();
                netBalance.merge(participantId, participant.getShareAmount().negate(), BigDecimal::add);
                usersById.putIfAbsent(participantId, participant.getUser());
            }
        }

        // Apply each settlement: the payer's debt decreases (net balance goes up),
        // the receiver's credit decreases (net balance goes down).
        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);
        for (Settlement settlement : settlements) {
            Long paidById = settlement.getPaidBy().getId();
            Long paidToId = settlement.getPaidTo().getId();
            netBalance.merge(paidById, settlement.getAmount(), BigDecimal::add);
            netBalance.merge(paidToId, settlement.getAmount().negate(), BigDecimal::add);
            usersById.putIfAbsent(paidById, settlement.getPaidBy());
            usersById.putIfAbsent(paidToId, settlement.getPaidTo());
        }

        List<UserBalance> balances = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : netBalance.entrySet()) {
            User user = usersById.get(entry.getKey());
            BigDecimal rounded = entry.getValue().setScale(2, RoundingMode.HALF_UP);
            balances.add(new UserBalance(user.getId(), user.getName(), rounded));
        }

        List<DebtResponse> simplifiedDebts = simplifyDebts(netBalance, usersById);

        return new GroupBalanceResponse(groupId, balances, simplifiedDebts);
    }

    /**
     * Reduces the net balances into a minimal list of "who owes whom" transactions
     * using a greedy algorithm: the biggest debtor always pays the biggest creditor
     * until every balance is settled to (near) zero.
     */
    private List<DebtResponse> simplifyDebts(Map<Long, BigDecimal> netBalance, Map<Long, User> usersById) {
        List<Map.Entry<Long, BigDecimal>> creditors = new ArrayList<>();
        List<Map.Entry<Long, BigDecimal>> debtors = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : netBalance.entrySet()) {
            BigDecimal amount = entry.getValue().setScale(2, RoundingMode.HALF_UP);
            if (amount.compareTo(EPSILON) > 0) {
                creditors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), amount));
            } else if (amount.compareTo(EPSILON.negate()) < 0) {
                debtors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), amount.abs()));
            }
        }

        creditors.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        debtors.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<DebtResponse> debts = new ArrayList<>();

        int i = 0;
        int j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            Map.Entry<Long, BigDecimal> debtor = debtors.get(i);
            Map.Entry<Long, BigDecimal> creditor = creditors.get(j);

            BigDecimal settledAmount = debtor.getValue().min(creditor.getValue());

            User fromUser = usersById.get(debtor.getKey());
            User toUser = usersById.get(creditor.getKey());
            debts.add(new DebtResponse(fromUser.getId(), fromUser.getName(),
                    toUser.getId(), toUser.getName(), settledAmount));

            BigDecimal remainingDebtor = debtor.getValue().subtract(settledAmount);
            BigDecimal remainingCreditor = creditor.getValue().subtract(settledAmount);

            debtors.set(i, new AbstractMap.SimpleEntry<>(debtor.getKey(), remainingDebtor));
            creditors.set(j, new AbstractMap.SimpleEntry<>(creditor.getKey(), remainingCreditor));

            if (remainingDebtor.compareTo(EPSILON) <= 0) {
                i++;
            }
            if (remainingCreditor.compareTo(EPSILON) <= 0) {
                j++;
            }
        }

        return debts;
    }
}
