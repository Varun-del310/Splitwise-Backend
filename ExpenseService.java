package com.splitwise.backend.service;

import com.splitwise.backend.dto.ExpenseCreateRequest;
import com.splitwise.backend.dto.ExpenseResponse;

import java.util.List;

public interface ExpenseService {

    ExpenseResponse createExpense(Long groupId, ExpenseCreateRequest request);

    List<ExpenseResponse> getExpensesByGroup(Long groupId);
}
